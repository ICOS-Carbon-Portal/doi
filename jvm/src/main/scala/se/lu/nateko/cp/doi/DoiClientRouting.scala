package se.lu.nateko.cp.doi

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import se.lu.nateko.cp.doi.core.DoiClient
import core.JsonSupport.doiMetaFormat

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
				onSuccess(client.getMetadata(doi)){meta =>
					complete(HttpEntity(ContentTypes.`application/json`, meta))
				}
			} ~
			complete(StatusCodes.NotFound)
		} ~
		pathPrefix(Segment / Segment){(prefix, suffix) =>
			complete((StatusCodes.BadRequest, s"Bad DOI: $prefix/$suffix"))
		}
	}

	def writingRoute(authorizer: Doi => Boolean) = post{
		path("metadata"){
			entity(as[DoiMeta]){meta =>
				if(authorizer(meta.doi))
					onSuccess(client.putMetadata(meta)){
						complete(StatusCodes.OK)
					}
				else forbid
			} ~
			complete(StatusCodes.BadRequest -> "Expected DoiMeta as payload")
		}
	}

	def deleteRoute(authorizer: Doi => Boolean) = delete {
		pathPrefix(DoiPath){doi =>
			if(authorizer(doi)) {
				onSuccess(client.delete(doi)){
					complete(StatusCodes.OK)
				}
			} else forbid
		}
	}
}

object DoiClientRouting{

	val forbid = complete(StatusCodes.Forbidden -> "You are not allowed to modify this DOI")

	val DoiPath = (Segment / Segment).tflatMap{
		case (prefix, suffix) =>
			val doi = Doi(prefix, suffix)
			doi.error match{
				case None => Some(Tuple1(doi))
				case Some(_) => None
			}
	}
}
