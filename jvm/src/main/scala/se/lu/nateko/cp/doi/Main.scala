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
		val http = new PlainJavaDoiHttp(config.symbol, config.password)
		val client = new DoiClient(config, http)

		import Pickling._

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
					path("metadata" / Segment / Segment){(prefix, suffix) =>
						val doi = Doi(prefix, suffix)
						println(doi.prefix + " // " + doi.suffix)
						println(config)
						doi.error match{
							case None => onSuccess(client.getMetadata(doi)){meta =>
								complete(upickle.default.write(meta))
							}
							case Some(err) => complete((StatusCodes.BadRequest, err))
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

