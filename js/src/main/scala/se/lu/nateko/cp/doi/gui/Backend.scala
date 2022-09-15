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

	def getPrefixInfo: Future[String] = dom
		.fetch("/api/doiprefix")
		.flatMap(checkResponseOk("fetch DOI prefix"))
		.flatMap(_.text())

	def updateMeta(meta: DoiMeta): Future[String] = dom
		.fetch(
			"/api/metadata",
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

	def getFreshDoiList(query: Option[String], page: Option[Int]): Future[FreshDoiList] = {
		//val startTime = System.currentTimeMillis()
		dom
			.fetch(s"/api/list/?query=${query.getOrElse("")}&page=${page.getOrElse(1)}")
			//.andThen{case _ => println(s"Got list response in ${System.currentTimeMillis() - startTime} ms")}
			.flatMap(checkResponseOk("fetch DOI list from DataCite REST API"))
			.flatMap(_.text())
			.map(s => Json.parse(s).as[DoiListPayload])
			.map{pl =>
				val dois = pl.data.map(_.attributes)
				//println(s"Got list response and parsed DOIs in ${System.currentTimeMillis() - startTime} ms")
				FreshDoiList(dois, Some(pl.meta))
			}
	}

	def delete(doi: Doi): Future[Unit] = dom
		.fetch(s"/api/$doi/", new dom.RequestInit{method = dom.HttpMethod.DELETE})
		.flatMap(checkResponseOk("delete DOI"))
		.map(_ => ())

	def submitForPublication(doi: Doi): Future[Unit] = dom
		.fetch(s"/api/submit/$doi", new dom.RequestInit{method = dom.HttpMethod.POST})
		.flatMap(checkResponseOk("submit DOI for publication"))
		.map(_ => ())

	private def checkResponseOk(hint: String)(resp: dom.Response): Future[dom.Response] =
		if(resp.ok) Future.successful(resp)
		else Future.failed(new Exception(s"Got response ${resp.statusText} when trying to $hint"))
}
