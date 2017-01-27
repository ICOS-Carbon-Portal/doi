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

		val DoiConfig(clientConf, authConf) = DoiConfig.getConfig

		val authRouting = new AuthRouting(authConf)

		val doiRouting = {
			val http = new AkkaDoiHttp(clientConf.symbol, clientConf.password)
			val client = new DoiClient(clientConf, http)
			new DoiClientRouting(client)
		}


		val exceptionHandler = ExceptionHandler{
			case e: Exception =>
				complete((StatusCodes.InternalServerError, e.getMessage))
		}

		def mainPage(development: Boolean) = {
			authRouting.user{uid =>
				complete(views.html.doi.DoiPage(Some(uid), development))
			} ~
			complete(views.html.doi.DoiPage(None, development))
		}

		val route = handleExceptions(exceptionHandler){
			pathPrefix("api"){
				doiRouting.publicRoute ~
				post{
					authRouting.user{uid =>
						doiRouting.writingRoute
					} ~
					complete((StatusCodes.Unauthorized, "Must be logged in"))
				} ~
				path("doiprefix"){
					get{complete(clientConf.doiPrefix)}
				}
			} ~
			get{
				pathSingleSlash(mainPage(false)) ~
				path("develop")(mainPage(true)) ~
				getFromResourceDirectory("")
			}
		}

		Http().bindAndHandle(route, "127.0.0.1", port = 8079)
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

