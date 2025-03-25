package se.lu.nateko.cp.doi

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import se.lu.nateko.cp.doi.core.DoiClient
import se.lu.nateko.cp.doi.JsonSupport.given
import scala.concurrent.Future
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport.*
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import scala.util.{ Failure, Success }
import play.api.libs.json.{Json, JsArray, JsString, JsValue}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import akka.NotUsed
import scala.util.control.NoStackTrace
import java.util.concurrent.ExecutionException
import java.net.URI

class DoiClientRouting(client: DoiClient, conf: DoiConfig)(using ActorSystem) {
	import DoiClientRouting._

	val publicRoute = get{
		pathPrefix("list"){
			parameters("query".optional, "page".as[Int].optional){ (query, page) =>
				onSuccess(client.listDoisMeta(query, page)) { payload =>
					complete(payload)
				}
			}
		} ~
		pathPrefix(Segment / Segment){(prefix, suffix) =>
			complete((StatusCodes.BadRequest, s"Bad DOI: $prefix/$suffix"))
		}
	}

	def writingRoute(authorizer: DoiMeta => Future[Boolean])(using ExecutionContext) = post{
		path("metadata"){
			entity(as[DoiMeta]){meta =>
				onSuccess(authorizer(meta)){allowed =>
					if(allowed)
						onSuccess(client.putMetadata(meta)){

							import meta.doi.{prefix, suffix}

							val metaHost = meta.url.fold(conf.metaHost)(url => {
								val targetHost = new URI(url).getHost()
								if (targetHost == "citymeta.icos-cp.eu")
									targetHost
								else
									conf.metaHost
							})

							def cacheInvalidationError(exc: Throwable): String = exc match
								case ee: ExecutionException => cacheInvalidationError(ee.getCause)
								case exc: Throwable =>
									s"Cache invalidation for DOI citation on server ${metaHost} for DOI $prefix/$suffix failed\n" +
										s"Error message: ${exc.getMessage}"

							val cacheInvalidationDone: Future[NotUsed] = Http().singleRequest(
									HttpRequest(uri = s"https://${metaHost}/dois/dropCache/$prefix/$suffix", method = HttpMethods.POST)
								).flatMap{resp =>
									if(resp.status.isSuccess) Future.successful(NotUsed)
									else resp.entity.toStrict(3.seconds).flatMap{entity =>
										Future.failed(new Error(entity.data.utf8String) with NoStackTrace)
									}
								}

							onComplete(cacheInvalidationDone){doneTry =>
								val resp: String = doneTry match
									case Success(_) => ""
									case Failure(ex) => cacheInvalidationError(ex)
								complete(resp)
							}
						}
					else forbid
				}
			} ~
			complete(StatusCodes.BadRequest -> "Expected DoiMeta as payload")
		}
	}
}

object DoiClientRouting{

	val forbid = complete(StatusCodes.Forbidden -> "You are not allowed to perform this operation")

	val DoiPath = (Segment / Segment).tflatMap{
		case (prefix, suffix) =>
			val doi = Doi(prefix, suffix)
			doi.error match{
				case None => Some(Tuple1(doi))
				case Some(_) => None
			}
	}
}
