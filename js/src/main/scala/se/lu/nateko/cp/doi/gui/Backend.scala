package se.lu.nateko.cp.doi.gui

import scala.concurrent.Future
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import play.api.libs.json._
import se.lu.nateko.cp.doi.JsonSupport._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.PrefixInfo
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.XMLHttpRequest

case class DoiWithTitle(doi: Doi, title: String)

object Backend {

	private def parseTo[T : Reads](xhr: XMLHttpRequest): T = {
		Json.parse(xhr.responseText).as[T]
	}

	def getPrefixInfo: Future[PrefixInfo] = Ajax
		.get("/api/doiprefix")
		.recoverWith(recovery("fetch DOI prefix"))
		.map(parseTo[PrefixInfo])

	def checkIfExists(doi: Doi): Future[Boolean] = Ajax
		.get(s"/api/$doi/exists")
		.recoverWith(recovery("check for DOI existence"))
		.map(_.responseText.toBoolean)

	def getTarget(doi: Doi): Future[Option[String]] = Ajax
		.get(s"/api/$doi/target")
		.recoverWith(recovery("fetch DOI target URL"))
		.map(_.responseText)
		.map(s => if(s.isEmpty) None else Some(s))

	def getMeta(doi: Doi): Future[DoiMeta] = Ajax
		.get(s"/api/$doi/metadata")
		.recoverWith(recovery("fetch DOI metadata"))
		.map(parseTo[DoiMeta])

	def getInfo(doi: Doi): Future[DoiInfo] = Backend.getMeta(doi)
		.zip(Backend.getTarget(doi))
		.map{
			case (meta, target) => DoiInfo(meta, target, true)
		}

	def updateUrl(doi: Doi, url: String) = Ajax
		.post(s"/api/$doi/target", url)
		.recoverWith(recovery("update the target URL"))

	def updateMeta(meta: DoiMeta) = Ajax
		.post("/api/metadata", Json.toJson(meta).toString)
		.recoverWith(recovery("update DOI metadata"))

	def getFreshDoiList: Future[FreshDoiList] = Ajax
		.get("https://api.datacite.org/dois?client-id=snd.icos&page%5Bsize%5D=500")
		.recoverWith(recovery("fetch DOI list from DataCite REST API"))
		.map(parseTo[JsObject])
		.map{jso =>
			val dois = (jso \ "data").as[JsArray].value.map{jsv =>
				val attrs = jsv \ "attributes"
				val title = (attrs \ "titles").as[JsArray].value.headOption
					.map{title => (title \ "title").as[String]}
					.getOrElse("")
				val doiStr = (attrs \ "doi").as[String]
				val idx = doiStr.indexOf('/')
				val doi = Doi(doiStr.substring(0, idx), doiStr.substring(idx + 1).toUpperCase)
				DoiWithTitle(doi, title)
			}
			FreshDoiList(dois)
		}

	private def recovery(hint: String): PartialFunction[Throwable, Future[XMLHttpRequest]] = {
		case AjaxException(xhr) =>
			val msg = if(xhr.responseText.isEmpty)
				s"Got HTTP status ${xhr.status} when trying to $hint"
			else s"Error when trying to $hint:\n" + xhr.responseText

			Future.failed(new Exception(msg))
	}
}
