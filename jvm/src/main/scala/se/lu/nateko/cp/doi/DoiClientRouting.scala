package se.lu.nateko.cp.doi

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import se.lu.nateko.cp.doi.core.DoiClient
import scala.concurrent.ExecutionContext
import java.net.URL

class DoiClientRouting(client: DoiClient)(implicit ec: ExecutionContext) {
	import DoiClientRouting._
	import Pickling._

	val publicRoute = get{
		path("list"){
			onSuccess(client.listDois) { dois =>
				complete{
					upickle.default.write(dois)
				}
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
					complete(upickle.default.write(url.map(_.toString)))
				}
			} ~
			path("metadata"){
				onSuccess(client.getMetadata(doi)){meta =>
					complete(upickle.default.write(meta))
				}
			} ~
			complete(StatusCodes.NotFound)
		} ~
		pathPrefix(Segment / Segment){(prefix, suffix) =>
			complete((StatusCodes.BadRequest, s"Bad DOI: $prefix/$suffix"))
		}
	}

	val writingRoute = post{
		path(DoiPath / "target"){doi =>
			entity(as[String]){url =>
				onSuccess(client.setUrl(doi, new URL(url))){
					complete(StatusCodes.OK)
				}
			}
		} ~
		path("metadata"){
			entity(as[String]){metaStr =>
				val meta = upickle.default.read[DoiMeta](metaStr)
				onSuccess(client.postMetadata(meta)){
					complete(StatusCodes.OK)
				}
			}
		}
	}
}

object DoiClientRouting{
	val DoiPath = (Segment / Segment).tflatMap{
		case (prefix, suffix) =>
			val doi = Doi(prefix, suffix)
			doi.error match{
				case None => Some(Tuple1(doi))
				case Some(_) => None
			}
	}
}
