package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.NavigateToList
import se.lu.nateko.cp.doi.gui.DoiMetaViewer
import se.lu.nateko.cp.doi.gui.DoiCloneRequest
import se.lu.nateko.cp.doi.gui.DoiJsonEditor
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.widgets.EditorTab
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.Backend
import se.lu.nateko.cp.doi.gui.ReportError
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom.Event

class DoiDetailView(metaInit: DoiMeta, d: DoiRedux.Dispatcher) {

	private[this] var meta = metaInit

	private val backToList: Event => Unit = e => {
		e.preventDefault()
		d.dispatch(NavigateToList)
	}

	private val headerSection = div(cls := "mb-3")(
		a(href := "#", cls := "btn btn-outline-primary mb-2", onclick := backToList)(
			span(cls := "fas fa-arrow-left mr-2"),
			"Back to list"
		),
		h2(cls := "mt-2")(s"${meta.doi}")
	).render

	private val contentBody = div(cls := "card")(
		div(cls := "card-body")
	).render

	val element = div(id := "main")(
		headerSection,
		contentBody
	)

	def initialize(): Unit = {
		contentBody.querySelector(".card-body").appendChild(metaViewer.element)
	}

	private def metaViewer: DoiMetaViewer = new DoiMetaViewer(meta, tabsCb, meta => d.dispatch(DoiCloneRequest(meta)))

	private def metaWidget = new DoiMetaWidget(
		meta,
		updateDoiMeta,
		tabsCb,
		doi => {
			d.dispatch(ThunkActions.requestDoiDeletion(doi))
		}
	)

	private def metaJsonEditor = new DoiJsonEditor(meta, updateDoiMeta, tabsCb)

	private val tabsCb: Map[EditorTab, () => Unit] = Map(
		EditorTab.view -> {() => 
			val cardBody = contentBody.querySelector(".card-body")
			cardBody.replaceChildren(metaViewer.element)
		},
		EditorTab.edit -> {() => 
			val cardBody = contentBody.querySelector(".card-body")
			cardBody.replaceChildren(metaWidget.element)
		},
		EditorTab.json -> {() => 
			val cardBody = contentBody.querySelector(".card-body")
			cardBody.replaceChildren(metaJsonEditor.element)
		},
	)

	private def updateDoiMeta(updated: DoiMeta): Future[Unit] = {
		val updateDone = Backend.updateMeta(updated)
		
		Future(updateDone.onComplete(s => {
			s match {
				case Failure(exc) =>
					d.dispatch(ReportError(s"Failed to update DOI ${updated.doi}:\n${exc.getMessage}"))
				case Success(s) =>
					if (!s.isEmpty()) d.dispatch(ReportError(s))
					//recreate the DOI metadata widget with the updated metadata
					val cardBody = contentBody.querySelector(".card-body")
					cardBody.innerHTML = ""
					meta = updated
					cardBody.appendChild(metaWidget.element)
					// Update header with new DOI if it changed
					headerSection.querySelector("h2").textContent = s"DOI: ${updated.doi}"
			}
		}))
	}
}
