package se.lu.nateko.cp.doi

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import se.lu.nateko.cp.doi.core.DoiClient
import java.net.URL
import play.api.libs.json._

class DoiClientRouting(client: DoiClient) {
	import DoiClientRouting._
	import JsonSupport._

	val publicRoute = get{
		path("list"){
			onSuccess(client.listDois) { dois =>
				complete{
					Json.toJson(dois).toString
				}
			}
		} ~
		path("metalist"){
			onSuccess(client.listDoisMeta) { json =>
				complete(
					HttpEntity(ContentTypes.`application/json`, json)
				)
			}
		} ~
		pathPrefix(DoiPath){doi =>
			path("exists"){
				onSuccess(client.checkIfKnown(doi)){isKnown =>
					complete(isKnown.toString)
				}
			} ~
			path("target"){
				onSuccess(client.getUrl(doi)){url =>
					complete(StatusCodes.OK -> url.map(_.toString).getOrElse(""))
				}
			} ~
			path("metadata"){
				onSuccess(client.getMetadata(doi)){meta =>
					complete(Json.toJson(meta).toString)
				}
			} ~
			complete(StatusCodes.NotFound)
		} ~
		pathPrefix(Segment / Segment){(prefix, suffix) =>
			complete((StatusCodes.BadRequest, s"Bad DOI: $prefix/$suffix"))
		}
	}

	def writingRoute(authorizer: Doi => Boolean) = post{
		path(DoiPath / "target"){doi =>
			if(authorizer(doi))
				entity(as[String]){url =>
					onSuccess(client.setUrl(doi, new URL(url))){
						complete(StatusCodes.OK)
					}
				}
			else forbid
		} ~
		path("metadata"){
			entity(as[String]){metaStr =>
				val meta = Json.parse(metaStr).as[DoiMeta]
				if(authorizer(meta.id))
					onSuccess(client.postMetadata(meta)){
						complete(StatusCodes.OK)
					}
				else forbid
			}
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
