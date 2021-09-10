package se.lu.nateko.cp.doi.gui

import scala.concurrent.Future
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import play.api.libs.json._
import se.lu.nateko.cp.doi.JsonSupport._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.DoiListPayload
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.XMLHttpRequest

case class DoiWithTitle(doi: Doi, title: String)

object Backend {

	private def parseTo[T : Reads](xhr: XMLHttpRequest): T = {
		Json.parse(xhr.responseText).as[T]
	}

	def getPrefixInfo: Future[String] = Ajax
		.get("/api/doiprefix")
		.recoverWith(recovery("fetch DOI prefix"))
		.map(parseTo[String])

	def updateMeta(meta: DoiMeta): Future[Unit] = Ajax
		.post(
			"/api/metadata",
			Json.toJson(meta).toString,
			headers = Map("content-type" -> "application/json")
		)
		.flatMap(checkResponse200)
		.recoverWith(recovery("update DOI metadata"))

	def getFreshDoiList(query: Option[String], page: Option[Int]): Future[FreshDoiList] = {
		//val startTime = System.currentTimeMillis()
		Ajax
			.get(s"/api/list/?query=${query.getOrElse("")}&page=${page.getOrElse(1)}")
			//.andThen{case _ => println(s"Got list response in ${System.currentTimeMillis() - startTime} ms")}
			.map(parseTo[DoiListPayload])
			.map{pl =>
				val dois = pl.data.map(_.attributes)
				//println(s"Got list response and parsed DOIs in ${System.currentTimeMillis() - startTime} ms")
				FreshDoiList(dois, Some(pl.meta))
			}
			.recoverWith(recovery("fetch DOI list from DataCite REST API"))
	}

	def delete(doi: Doi) = Ajax
		.delete(s"/api/$doi/")
		.recoverWith(recovery("delete DOI"))

	private def recovery[T](hint: String): PartialFunction[Throwable, Future[T]] = {
		case AjaxException(xhr) =>
			val msg = if(xhr.responseText.isEmpty)
				s"Got HTTP status ${xhr.status} when trying to $hint"
			else s"Error when trying to $hint:\n" + xhr.responseText

			Future.failed(new Exception(msg))
	}

	private def checkResponse200(resp: XMLHttpRequest): Future[Unit] =
		if(resp.status == 200) Future.successful(())
		else Future.failed(new Exception(s"Got response ${resp.statusText} from the server"))
}
