package se.lu.nateko.cp.doi

import scala.scalajs.js.JSApp
import org.scalajs.dom
import dom.document
import org.scalajs.dom.raw.Event
import org.scalajs.dom.ext.Ajax
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta.Title
import se.lu.nateko.cp.doi.meta.TitleType
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import FrontendViews._

object DoiApp extends JSApp {

	private[this] val doiListId = "doilist"
	private var currentlySelected: Doi = null

	def main(): Unit = {
		val mainDiv = document.getElementById("main")
		mainDiv.parentNode.replaceChild(mainLayout.render, mainDiv)

		refreshDoiList(null)
	}

	def mainLayout = div(id := "main")(
		div(cls := "page-header")(
			h1("Carbon Portal DOI minting service")
		),
		basicPanel(
			button(cls := "btn btn-default", onclick := refreshDoiList)("Refresh DOI list")
		),
		ul(cls := "list-unstyled", id := doiListId)
	)

	def getDoiList: Future[Seq[Doi]] = Ajax
		.get("/api/list")
		.map(req => upickle.default.read[Seq[Doi]](req.responseText))

	val refreshDoiList: dom.Event => Unit = e => getDoiList
		.map(repopulateDoiList)
		.failed.foreach{err =>
			dom.console.log(err.getMessage)
		}

	def repopulateDoiList(doiList: Seq[Doi]): Unit = {
		val listElem = getListElem
		listElem.innerHTML = ""

		for(doi <- doiList) {
			listElem.appendChild(doiElem(doi).render)
		}
	}

	def doiElem(doi: Doi) = div(
		cls := "panel panel-default",
		id := doi.toString
	)(
		div(cls := "panel-heading", onclick := selectDoi(doi))(
			doiListIcon(doi),
			span(" " + doi.toString)
		)
	)

	def selectDoi(doi: Doi): dom.Event => Unit = e => {
		if(doi == currentlySelected) currentlySelected = null
		else if(currentlySelected == null) currentlySelected = doi
		else{
			val oldSelected = currentlySelected
			currentlySelected = doi
			rerenderDoiElem(oldSelected)
		}
		rerenderDoiElem(doi)
	}

	def rerenderDoiElem(doi: Doi): Unit = {
		val oldElem = document.getElementById(doi.toString)
		val newElem = doiElem(doi).render
		getListElem.replaceChild(newElem, oldElem)
	}

	def doiListIcon(doi: Doi) = {
		val iconClass = "glyphicon glyphicon-triangle-" +
			(if(doi == currentlySelected) "bottom" else "right")
		span(cls := iconClass)
	}

	def getListElem = document.getElementById(doiListId)
}


