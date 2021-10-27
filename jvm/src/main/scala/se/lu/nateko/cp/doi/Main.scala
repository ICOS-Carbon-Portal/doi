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
import spray.json.JsString
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import scala.concurrent.Future
import scala.util.Try

object Main{

	private[this] implicit val pageMarsh = TemplatePageMarshalling.marshaller

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem()
		import system.dispatcher

		val DoiConfig(clientConf, prefixInfo, authConf, admins, emailConfig) = DoiConfig.getConfig

		val authRouting = new AuthRouting(authConf)

		val client = {
			val http = new AkkaDoiHttp(clientConf.symbol, clientConf.password)
			new DoiClient(clientConf, http)
		}
		val doiRouting = new DoiClientRouting(client)

		val emailSender = new Mailer(emailConfig)

		val exceptionHandler = ExceptionHandler{
			case e: Exception =>
				complete((StatusCodes.InternalServerError, e.getMessage))
		}

		def isAdmin(uid: UserId): Boolean = admins.exists(auid => auid.email.equalsIgnoreCase(uid.email))
		def isOptAdmin(uidOpt: Option[UserId]) = uidOpt.fold(false)(isAdmin)

		def mainPage(development: Boolean) = authRouting.userOpt{uidOpt =>
			complete(views.html.doi.DoiPage(uidOpt.isDefined, isOptAdmin(uidOpt), development, authConf.authHost))
		}

		val route = handleExceptions(exceptionHandler){
			pathPrefix("api"){
				doiRouting.publicRoute ~
				post{
					authRouting.user{uid =>
						doiRouting.writingRoute{doiMeta =>
							if(isAdmin(uid)) Future.successful(true)
							else if(doiMeta.event.isDefined || doiMeta.state != DoiPublicationState.draft) Future.successful(false)
							else client.getMetadataParsed(doiMeta.doi).map{
								case Some(currMeta) =>
									currMeta.state == DoiPublicationState.draft
								case None =>
									true
							}
						} ~
						pathPrefix("submit"){
							path(DoiClientRouting.DoiPath){doi =>
								onSuccess(Future(emailSender.send(
									admins.map(_.email).toSeq,
									"DOI submitted for publication",
									views.html.doi.DoiSubmissionEmail(uid, doi).body
								))){
									complete(StatusCodes.OK)
								}
							} ~
							complete(StatusCodes.BadRequest -> "Expected URL path ending in a DOI")
						}
					} ~
					complete((StatusCodes.Unauthorized, "Must be logged in"))
				} ~
				delete{
					authRouting.user{uid =>
						if(isAdmin(uid)) {
							pathPrefix(DoiClientRouting.DoiPath){doi =>
								onSuccess(client.delete(doi)){
									complete(StatusCodes.OK)
								}
							} ~
							complete(StatusCodes.BadRequest -> "Expected URL path ending in a DOI")
						} else complete(StatusCodes.Forbidden -> "Must be admin to delete DOIs")
					} ~
					complete(StatusCodes.Unauthorized -> "Must be logged in")
				} ~
				path("doiprefix"){
					get{complete(JsString(prefixInfo).compactPrint)}
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
						complete(s"""{"email": $email, "isAdmin": ${isOptAdmin(uidOpt)}}""")
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

		Http().newServerAt("127.0.0.1", port = 8079)
			.bindFlow(route)
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
