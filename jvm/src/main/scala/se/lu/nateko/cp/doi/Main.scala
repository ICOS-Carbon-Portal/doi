package se.lu.nateko.cp.doi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling.ToResponseMarshaller
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
import play.twirl.api.Html
import eu.icoscp.envri.Envri

object Main{

	private given ToResponseMarshaller[Html] = TemplatePageMarshalling.marshaller
	private given Envri = Envri.ICOS

	def main(args: Array[String]): Unit = {

		implicit val system = ActorSystem()
		import system.dispatcher

		val conf = DoiConfig.getConfig

		val authRouting = new AuthRouting(conf.auth)

		val clients: Map[String, DoiClient] = conf.envConfigs.map{ (envName, envConf) =>
			val http = new AkkaDoiHttp(envConf.client.member.symbol, envConf.client.member.password)
			envName -> new DoiClient(envConf.client, http)
		}

		val defaultEnv =
			if clients.contains("test") then "test"
			else clients.keys.head

		val doiRouting = new DoiClientRouting(clients, defaultEnv, conf)

		val envConfigsJson = {
			val envs = conf.envConfigs.keys.toSeq.sorted
			val prefixes = conf.envConfigs.map((name, ec) => s""""$name":"${ec.prefixInfo}"""").mkString("{", ",", "}")
			s"""{"envs":[${envs.map(e => s""""$e"""").mkString(",")}],"default":"$defaultEnv","prefixes":$prefixes}"""
		}

		val emailSender = new Mailer(conf.mailing)

		val exceptionHandler = ExceptionHandler{
			case e: Exception =>
				complete((StatusCodes.InternalServerError, e.getMessage))
		}

		def isAdmin(uid: UserId): Boolean = conf.admins.exists(auid => auid.email.equalsIgnoreCase(uid.email))
		def isOptAdmin(uidOpt: Option[UserId]) = uidOpt.fold(false)(isAdmin)

		def mainPage(isDev: Boolean, doiSuffix: Option[String] = None) = authRouting.userOpt{uidOpt =>
			complete(views.html.doi.DoiPage(uidOpt.isDefined, isOptAdmin(uidOpt), isDev, conf.auth.authHost, doiSuffix))
		}

		def sendEmail(uid: UserId, doi: Doi) = Future(
			emailSender.send(
				conf.admins,
				"DOI submitted for publication",
				views.html.doi.DoiSubmissionEmail(uid, doi).body
			)
		)(using ExecutionContext.Implicits.global)

		val route = handleExceptions(exceptionHandler){
			pathPrefix("api"){
				doiRouting.publicRoute ~
				post{
					authRouting.user{uid =>
						doiRouting.writingRoute{(doiMeta, client) =>
							if(isAdmin(uid)) Future.successful(true)
							else if(doiMeta.event.isDefined || doiMeta.state != DoiPublicationState.draft) Future.successful(false)
							else client.getMetadata(doiMeta.doi).map{
								case Some(currMeta) =>
									currMeta.state == DoiPublicationState.draft
								case None =>
									true
							}
						} ~
						pathPrefix("submit"){
							path(DoiClientRouting.DoiPath){doi =>
								onSuccess(sendEmail(uid, doi)){
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
							doiRouting.deleteRoute
						} else complete(StatusCodes.Forbidden -> "Must be admin to delete DOIs")
					} ~
					complete(StatusCodes.Unauthorized -> "Must be logged in")
				} ~
				path("envconfigs"){
					get{
						complete(HttpEntity(ContentTypes.`application/json`, envConfigsJson))
					}
				}
			} ~
			get{
				pathSingleSlash(mainPage(conf.development)) ~
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
					deleteCookie(conf.auth.authCookieName, domain = conf.auth.authCookieDomain, path = "/"){
						complete(StatusCodes.OK)
					}
				} ~
				getFromResourceDirectory("") ~
				// SPA catch-all - serve index for any /doi/* path (after trying static resources)
				pathPrefix("doi"){
					path(DoiClientRouting.DoiPath){doi =>
						mainPage(conf.development, Some(doi.suffix))
					}
				}
			}
		}

		Http().newServerAt(conf.httpBindInterface, port = conf.httpBindPort)
			.bindFlow(route)
			.onComplete{
				case Success(binding) =>
					sys.addShutdownHook{
						val doneFuture = binding.unbind().map{_ =>
							system.log.info("DOI service down")
						}
						Await.result(doneFuture, 3.seconds)
					}
					system.log.info(s"Started CP DOI service: $binding")

				case Failure(err) =>
					system.log.error(err, "Could not start DOI minting service")
			}
	}
}
