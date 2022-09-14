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
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext

case class DoiMetaUpdateInfo(metaUpdateSuccess: Boolean, cacheInvalidationSuccess: Boolean) {
	override def toString = s"Doi metadata update ${if(metaUpdateSuccess) "succeeded" else "failed"}, cache invalidation ${if(cacheInvalidationSuccess) "succeeded" else "failed"}"
}

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
						onSuccess(client.putMetadata(meta)){//payload => {
							// try cache-inv
							// if cache-inv failed => DoiMetaUpdateInfo(true, false)
							// else DoiMetaUpdateInfo(true, true)

							import meta.doi.{prefix, suffix}
								
							val cacheInvalidationResponse = Http().singleRequest(
								HttpRequest(uri = s"https://${conf.metaHost}/dois/dropCache/$prefix/$suffix", method = HttpMethods.POST)
							)

							onComplete(cacheInvalidationResponse){
								case Success(value) => {
									complete(DoiMetaUpdateInfo(true, true).toString)
								}
								case Failure(ex) => {
									complete(DoiMetaUpdateInfo(true, false).toString)
								}
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
