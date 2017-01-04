package se.lu.nateko.cp.doi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import se.lu.nateko.cp.doi.core.PlainJavaDoiHttp
import se.lu.nateko.cp.doi.core.DoiClient
import se.lu.nateko.cp.doi.meta.ContributorType

object Main{

	private[this] implicit val pageMarsh = TemplatePageMarshalling.marshaller

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem()
		import system.dispatcher
		implicit val materializer = ActorMaterializer()

		val config = DoiConfig.getClientConfig
		//TODO Use a dispatcher appropriate for blocking io operations for PlainJavaDoiHttp
		val http = new PlainJavaDoiHttp(config.symbol, config.password)
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

		val route = {
			pathPrefix("api"){
				get{
					path("list"){
						onSuccess(client.listDois) { dois =>
							complete{
								upickle.default.write(dois)
							}
						}
					} ~
					pathPrefix(DoiPath){doi =>
						path("target"){
							onSuccess(client.getUrl(doi)){url =>
								complete(upickle.default.write(url.toString))
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

