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

object Main{

	private[this] implicit val pageMarsh = TemplatePageMarshalling.marshaller

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem()
		import system.dispatcher
		implicit val materializer = ActorMaterializer()

		val config = DoiConfig.getClientConfig
		val http = new PlainJavaDoiHttp(config.symbol, config.password)
		val client = new DoiClient(config, http)

		val route = {
			get{
				pathSingleSlash{
					complete(views.html.DoiPage())
				} ~
				path("develop"){
					complete(views.html.DoiPage(true))
				} ~
				getFromResourceDirectory("")
			} ~
			get{
				path("api" / "list"){
					onSuccess(client.listDois) { dois =>
						complete{
							upickle.default.write(dois)
						}
					}
				}
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

