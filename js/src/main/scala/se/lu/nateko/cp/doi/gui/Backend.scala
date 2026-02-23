package se.lu.nateko.cp.doi.gui

import scala.concurrent.Future
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import play.api.libs.json._
import se.lu.nateko.cp.doi.JsonSupport.given
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.DoiListPayload
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits._


object Backend {

	def getEnvConfigs: Future[GotEnvConfigs] = dom
		.fetch("/api/envconfigs")
		.flatMap(checkResponseOk("fetch environment configs"))
		.flatMap(_.text())
		.map{ txt =>
			val json = Json.parse(txt)
			val envs = (json \ "envs").as[Seq[String]]
			val defaultEnv = (json \ "default").as[String]
			val prefixes = (json \ "prefixes").as[Map[String, String]]
			GotEnvConfigs(envs, defaultEnv, prefixes)
		}

	private def envParam(env: Option[String]): String =
		env.map(e => s"env=$e").getOrElse("")

	def updateMeta(meta: DoiMeta, env: Option[String]): Future[String] = dom
		.fetch(
			s"/api/metadata?${envParam(env)}",
			new dom.RequestInit{
				method = dom.HttpMethod.POST
				body = Json.toJson(meta).toString
				headers = new dom.Headers(js.Dictionary(("content-type", "application/json")))
			}
		)
		.flatMap(resp => {
			checkResponseOk("update DOI metadata")(resp)
		})
		.flatMap(_.text())
		.map(s => Json.parse(s).as[String])

	def getFreshDoiList(query: Option[String], page: Option[Int], state: Option[String], env: Option[String]): Future[FreshDoiList] = {
		val stateParam = state.map(s => s"&state=$s").getOrElse("")
		val envP = env.map(e => s"&env=$e").getOrElse("")
		dom
			.fetch(s"/api/list/?query=${query.getOrElse("")}&page=${page.getOrElse(1)}$stateParam$envP")
			.flatMap(checkResponseOk("fetch DOI list from DataCite REST API"))
			.flatMap(_.text())
			.map(s => Json.parse(s).as[DoiListPayload])
			.map{pl =>
				val dois = pl.data.map(_.attributes)
				FreshDoiList(dois, Some(pl.meta))
			}
	}

	def getDoi(doi: Doi, env: Option[String]): Future[Option[DoiMeta]] = dom
		.fetch(s"/api/meta/$doi?${envParam(env)}")
		.flatMap { resp =>
			if (resp.status == 404) Future.successful(None)
			else checkResponseOk(s"fetch DOI $doi")(resp).flatMap(_.text()).map { txt =>
				Some(Json.parse(txt).as[DoiMeta])
			}
		}

	def delete(doi: Doi, env: Option[String]): Future[Unit] = dom
		.fetch(s"/api/$doi/?${envParam(env)}", new dom.RequestInit{method = dom.HttpMethod.DELETE})
		.flatMap(checkResponseOk("delete DOI"))
		.map(_ => ())

	def submitForPublication(doi: Doi, env: Option[String]): Future[Unit] = dom
		.fetch(s"/api/submit/$doi?${envParam(env)}", new dom.RequestInit{method = dom.HttpMethod.POST})
		.flatMap(checkResponseOk("submit DOI for publication"))
		.map(_ => ())

	private def checkResponseOk(hint: String)(resp: dom.Response): Future[dom.Response] =
		if(resp.ok) Future.successful(resp)
		else resp.text().toFuture.map: respTxt =>
			throw new Exception(s"When trying to $hint , got response:\n$respTxt ")
}
