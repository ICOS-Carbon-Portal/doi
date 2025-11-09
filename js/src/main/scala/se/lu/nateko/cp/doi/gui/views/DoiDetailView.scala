package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.DoiMetaViewer
import se.lu.nateko.cp.doi.gui.DoiCloneRequest
import se.lu.nateko.cp.doi.gui.NavigateToRoute
import se.lu.nateko.cp.doi.gui.ListRoute
import se.lu.nateko.cp.doi.gui.DoiJsonEditor
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.widgets.EditorTab
import se.lu.nateko.cp.doi.gui.widgets.UnifiedToolbar
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

	// Check if user is admin by checking the main-wrapper class
	private val isAdmin = {
		val mainWrapper = org.scalajs.dom.document.getElementById("main-wrapper")
		mainWrapper != null && mainWrapper.classList.contains("is-admin")
	}

	// Admins start with edit tab, non-admins with view tab
	private val initialTab = if (isAdmin) EditorTab.edit else EditorTab.view

	private val backToList: Event => Unit = e => {
		e.preventDefault()
		d.dispatch(NavigateToRoute(ListRoute))
	}

	private val title = DoiMetaHelpers.extractTitle(meta)

	private val headerSection = div(cls := "mb-3")(
		h1(cls := "mt-2")(title),
		p(cls := "text-muted", style := "user-select: all;")(s"${meta.doi}")
	).render

	private val contentBody = div().render

	private lazy val toolbar = new UnifiedToolbar(
		meta,
		backToList,
		tabsCb,
		meta => d.dispatch(DoiCloneRequest(meta)),
		updateDoiMeta,
		doi => d.dispatch(ThunkActions.requestDoiDeletion(doi)),
		initialTab
	)

	val element = div(id := "detail-view")(
		headerSection,
		toolbar.element,
		contentBody
	)

	def initialize(): Unit = {
		if (isAdmin) {
			// For admins, start with edit view
			contentBody.appendChild(metaWidget.element)
			metaWidget.wireToolbarCallbacks()
		} else {
			// For non-admins, start with view-only mode
			contentBody.appendChild(metaViewer.element)
		}
	}

	private lazy val metaViewer: DoiMetaViewer = new DoiMetaViewer(meta, toolbar)

	private lazy val metaWidget = {
		val widget = new DoiMetaWidget(
			meta,
			updateDoiMeta,
			toolbar
		)
		widget
	}

	private lazy val metaJsonEditor = new DoiJsonEditor(meta, updateDoiMeta, toolbar)

	private lazy val tabsCb: Map[EditorTab, () => Unit] = Map(
		EditorTab.view -> {() => 
			contentBody.replaceChildren(metaViewer.element)
			toolbar.setTab(EditorTab.view)
			// No special callbacks for view mode
		},
		EditorTab.edit -> {() => 
			contentBody.replaceChildren(metaWidget.element)
			toolbar.setTab(EditorTab.edit)
			metaWidget.wireToolbarCallbacks()
		},
		EditorTab.json -> {() => 
			contentBody.replaceChildren(metaJsonEditor.element)
			toolbar.setTab(EditorTab.json)
			metaJsonEditor.wireToolbarCallbacks()
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
					contentBody.innerHTML = ""
					meta = updated
					contentBody.appendChild(metaWidget.element)
					// Update header with new title and DOI if they changed
					val newTitle = DoiMetaHelpers.extractTitle(updated)
					headerSection.querySelector("h1").textContent = newTitle
					headerSection.querySelector("p").textContent = s"${updated.doi}"
			}
		}))
	}
}
