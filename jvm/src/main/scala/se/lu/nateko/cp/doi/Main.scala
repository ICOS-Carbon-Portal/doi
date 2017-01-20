package se.lu.nateko.cp.doi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import se.lu.nateko.cp.doi.core.DoiClient
import se.lu.nateko.cp.doi.meta.ContributorType
import java.net.URL
import akka.http.scaladsl.server.ExceptionHandler

object Main{

	private[this] implicit val pageMarsh = TemplatePageMarshalling.marshaller

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem()
		import system.dispatcher
		implicit val materializer = ActorMaterializer()

		val config = DoiConfig.getClientConfig
		val http = new AkkaDoiHttp(config.symbol, config.password)
		val client = new DoiClient(config, http)

		import Pickling._

		val DoiPath = (Segment / Segment).tflatMap{
			case (prefix, suffix) =>
				val doi = Doi(prefix, suffix)
				doi.error match{
					case None => Some(Tuple1(doi))
					case Some(_) => None
				}
		}

		val exceptionHandler = ExceptionHandler{
			case e: Exception =>
				complete((StatusCodes.InternalServerError, e.getMessage))
		}

		val route = handleExceptions(exceptionHandler){
			pathPrefix("api"){
				get{
					path("doiprefix"){
						complete(config.doiPrefix)
					} ~
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
				} ~ post{
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
			} ~
			get{
				pathSingleSlash{
					complete(views.html.doi.DoiPage())
				} ~
				path("develop"){
					complete(views.html.doi.DoiPage(true))
				} ~
				getFromResourceDirectory("")
			}
		}

		Http().bindAndHandle(route, "127.0.0.1", port = 8099)
			.onSuccess{
				case binding =>
					sys.addShutdownHook{
						val doneFuture = binding.unbind()
							.flatMap(_ => system.terminate())(ExecutionContext.Implicits.global)
						Await.result(doneFuture, 3 seconds)
					}
					system.log.info(s"Started CP DOI service: $binding")
			}
	}
}

