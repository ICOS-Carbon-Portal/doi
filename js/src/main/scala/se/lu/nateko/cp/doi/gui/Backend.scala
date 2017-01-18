package se.lu.nateko.cp.doi.gui

import scala.concurrent.Future
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import se.lu.nateko.cp.doi.Pickling._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.XMLHttpRequest

object Backend {

	def getDoiList: Future[Seq[Doi]] = Ajax
		.get("/api/list")
		.map(req => upickle.default.read[Seq[Doi]](req.responseText))

	def getTarget(doi: Doi): Future[Option[String]] = Ajax
		.get(s"/api/$doi/target")
		.map(req => upickle.default.read[Option[String]](req.responseText))

	def getMeta(doi: Doi): Future[DoiMeta] = Ajax
		.get(s"/api/$doi/metadata")
		.map(req => upickle.default.read[DoiMeta](req.responseText))

	def getInfo(doi: Doi): Future[DoiInfo] = Backend.getMeta(doi)
		.zip(Backend.getTarget(doi))
		.map{
			case (meta, target) => DoiInfo(meta, target)
		}

	def updateUrl(doi: Doi, url: String) = Ajax
		.post(s"/api/$doi/target", url)
		.recoverWith(recovery)

	def updateMeta(meta: DoiMeta) = Ajax
		.post("/api/metadata", upickle.default.write(meta))
		.recoverWith(recovery)

	private val recovery: PartialFunction[Throwable, Future[XMLHttpRequest]] = {
		case AjaxException(xhr) =>
			val msg = if(xhr.responseText.isEmpty)
				s"Got HTTP status ${xhr.status} when trying to update the target URL"
			else xhr.responseText

			Future.failed(new Exception(msg))
	}
}
