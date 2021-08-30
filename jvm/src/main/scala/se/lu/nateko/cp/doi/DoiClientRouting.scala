package se.lu.nateko.cp.doi

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import se.lu.nateko.cp.doi.core.DoiClient
import core.JsonSupport.doiMetaFormat
import scala.concurrent.Future

class DoiClientRouting(client: DoiClient) {
	import DoiClientRouting._

	val publicRoute = get{
		path("metalist"){
			onSuccess(client.listDoisMeta) { json =>
				complete(HttpEntity(ContentTypes.`application/json`, json))
			}
		} ~
		pathPrefix(DoiPath){doi =>
			path("metadata"){
				onSuccess(client.getMetadata(doi)){
					case Some(meta) =>
						complete(HttpEntity(ContentTypes.`application/json`, meta))
					case None =>
						complete(StatusCodes.NotFound -> s"No metadata found for DOI $doi")
				}
			} ~
			complete(StatusCodes.NotFound)
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
