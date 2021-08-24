package se.lu.nateko.cp.doi.gui

import scala.concurrent.Future
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import play.api.libs.json._
import se.lu.nateko.cp.doi.JsonSupport._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
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

	def updateMeta(meta: DoiMeta) = Ajax
		.post(
			"/api/metadata",
			Json.toJson(meta).toString,
			headers = Map("content-type" -> "application/json")
		)
		.recoverWith(recovery("update DOI metadata"))

	def getFreshDoiList: Future[FreshDoiList] = Ajax
		.get(s"/api/metalist")
		.recoverWith(recovery("fetch DOI list from DataCite REST API"))
		.map(parseTo[JsObject])
		.map{jso =>
			val dois = (jso \ "data").as[JsArray].value.map{jsv =>
				(jsv \ "attributes").as[DoiMeta]
			}
			FreshDoiList(dois)
		}

		def delete(doi: Doi) = Ajax
			.delete(s"/api/$doi/")
			.recoverWith(recovery("delete DOI"))

	private def recovery(hint: String): PartialFunction[Throwable, Future[XMLHttpRequest]] = {
		case AjaxException(xhr) =>
			val msg = if(xhr.responseText.isEmpty)
				s"Got HTTP status ${xhr.status} when trying to $hint"
			else s"Error when trying to $hint:\n" + xhr.responseText

			Future.failed(new Exception(msg))
	}
}
