package se.lu.nateko.cp.doi

import scala.concurrent.Future
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue

import Pickling._

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

}