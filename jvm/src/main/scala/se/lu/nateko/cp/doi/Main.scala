package se.lu.nateko.cp.doi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import se.lu.nateko.cp.doi.core.DoiClient
import akka.http.scaladsl.server.ExceptionHandler
import scala.util.{Success, Failure}
import se.lu.nateko.cp.cpauth.core.UserId
import play.api.libs.json.Json
import se.lu.nateko.cp.doi.JsonSupport.prefixInfoFormat

object Main{

	private[this] implicit val pageMarsh = TemplatePageMarshalling.marshaller

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem()
		import system.dispatcher

		val DoiConfig(clientConf, prefixInfo, authConf, admins) = DoiConfig.getConfig

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

		def isAdmin(uidOpt: Option[UserId]): Boolean = uidOpt.map(admins.contains).getOrElse(false)

		def mainPage(development: Boolean) = authRouting.userOpt{uidOpt =>
			complete(views.html.doi.DoiPage(isAdmin(uidOpt), development, authConf.authHost))
		}

		val route = handleExceptions(exceptionHandler){
			pathPrefix("api"){
				doiRouting.publicRoute ~
				post{
					authRouting.user{uid =>
						doiRouting.writingRoute{_ =>
							admins.contains(uid)
						}
					} ~
					complete((StatusCodes.Unauthorized, "Must be logged in"))
				} ~
				path("doiprefix"){
					get{complete(Json.toJson(prefixInfo).toString)}
				}
			} ~
			get{
				pathSingleSlash(mainPage(false)) ~
				path("develop")(mainPage(true)) ~
				path("buildInfo"){
					complete(BuildInfo.toString)
				} ~
				path("whoami"){
					authRouting.userOpt{uidOpt =>
						val email = uidOpt.map(uid => "\"" + uid.email + "\"").getOrElse("null")
						complete(s"""{"email": $email, "isAdmin": ${isAdmin(uidOpt)}}""")
					}
				} ~
				path("logout"){
					deleteCookie(authConf.authCookieName, domain = authConf.authCookieDomain, path = "/"){
						complete(StatusCodes.OK)
					}
				} ~
				getFromResourceDirectory("")
			}
		}

		Http().bindAndHandle(route, "127.0.0.1", port = 8079)
			.onComplete{
				case Success(binding) =>
					sys.addShutdownHook{
						val doneFuture = binding.unbind()
							.flatMap(_ => system.terminate())(ExecutionContext.Implicits.global)
						Await.result(doneFuture, 3 seconds)
					}
					system.log.info(s"Started CP DOI service: $binding")

				case Failure(err) =>
					system.log.error(err, "Could not start DOI minting service")
			}
	}
}
