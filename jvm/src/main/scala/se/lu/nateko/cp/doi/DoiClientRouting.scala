package se.lu.nateko.cp.doi

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import se.lu.nateko.cp.doi.core.DoiClient
import se.lu.nateko.cp.doi.JsonSupport.given
import scala.concurrent.Future
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport.*
import akka.actor.ActorSystem

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

	def writingRoute(authorizer: DoiMeta => Future[Boolean]) = post{
		path("metadata"){
			entity(as[DoiMeta]){meta =>
				onSuccess(authorizer(meta)){allowed =>
					if(allowed)
						onSuccess(client.putMetadata(meta)){
							//TODO make http call to meta's api for DOI citation cache invalidation
							// use akka's http client call singleHttpRequest
							import meta.doi.{prefix, suffix}
							//pseudocode:
							//Http().singleHttpRequest(uri = s"https://${conf.metaHost}/dois/dropCache/$prefix/$suffix", method = POST)
							complete(StatusCodes.OK)
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
