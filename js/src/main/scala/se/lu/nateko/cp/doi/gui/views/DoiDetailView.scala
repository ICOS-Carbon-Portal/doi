package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.DoiMetaViewer
import se.lu.nateko.cp.doi.gui.DoiCloneRequest
import se.lu.nateko.cp.doi.gui.DoiUpdated
import se.lu.nateko.cp.doi.gui.NavigateToRoute
import se.lu.nateko.cp.doi.gui.ListRoute
import se.lu.nateko.cp.doi.gui.DoiJsonEditor
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.widgets.EditorTab
import se.lu.nateko.cp.doi.gui.widgets.UnifiedToolbar
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.Backend
import se.lu.nateko.cp.doi.gui.ReportError
import se.lu.nateko.cp.doi.gui.ClearLastClonedDoi
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom.Event

class DoiDetailView(metaInit: DoiMeta, d: DoiRedux.Dispatcher, isClone: Boolean = false) {

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

	// External links for DOI and DataCite
	private val doiUrl = "https://doi.org/" + meta.doi
	private val dataciteUrl = "https://commons.datacite.org/doi.org/" + meta.doi
	private val fabricaUrl = "https://doi.datacite.org/doi.org/" + meta.doi

	private val contentBody = div().render

	private lazy val toolbar = new UnifiedToolbar(
		meta,
		backToList,
		tabsCb,
		meta => d.dispatch(ThunkActions.requestDoiClone(meta)),
		updateDoiMeta,
		doi => d.dispatch(ThunkActions.requestDoiDeletion(doi)),
		initialTab
	)

	private val cloneBanner = div(
		id := "clone-banner",
		cls := "alert alert-info",
		role := "alert",
		style := "display: none; transition: opacity 0.3s ease, transform 0.3s ease; transform: translateY(-10px); opacity: 0;"
	)(
		i(cls := "fa-solid fa-copy me-2"),
		strong("Editing Clone: "),
		"This is a cloned draft. You are now editing a new copy with a different DOI."
	).render

	private val headerSection = div(cls := "mb-3 pt-3")(
		h1(id := "detail-title", cls := "fs-2", attr("title"):= title)(title),
		div(cls := "d-flex align-items-center gap-2 mb-3")(
			p(cls := "text-muted mb-0")(s"${meta.doi}"),
			span(cls := "text-muted")("|"),
			a(href := doiUrl, target := "_blank", cls := "link-secondary")("DOI"),
			span(cls := "text-muted")("|"),
			a(href := dataciteUrl, target := "_blank", cls := "link-secondary")("Commons"),
			span(cls := "text-muted")("|"),
			a(href := fabricaUrl, target := "_blank", cls := "link-secondary")("Fabrica")
		)
	).render

	private val stickyHeader = div(
		id := "sticky-header",
		style := "position: sticky; top: 0; z-index: 1000; background-color: white;"
	)(
		cloneBanner,
		headerSection,
		toolbar.element
	).render

	val element = div(
		id := "detail-view",
		cls := "position-relative",
		style := "transition: opacity 0.5s ease; opacity: 1;"
	)(
		stickyHeader,
		contentBody
	)

	def initialize(): Unit = {
		// Update page title to include DOI suffix
		org.scalajs.dom.document.title = DoiMetaHelpers.pageTitle(meta.doi.suffix)

		// Show clone banner if this is a cloned DOI
		if (isClone) {
			showCloneBanner()
			// Clear the clone flag after showing the banner
			d.dispatch(ClearLastClonedDoi)
		}

		if (isAdmin) {
			// For admins, start with edit view
			contentBody.appendChild(metaWidget.element)
			metaWidget.wireToolbarCallbacks()
		} else {
			// For non-admins, start with view-only mode
			contentBody.appendChild(metaViewer.element)
		}

		toolbar.updateTocButtonPosition()
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
			toolbar.setUpdateButtonEnabled(false)
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
					// Update external link hrefs
					val links = headerSection.querySelectorAll("a[target='_blank']")
					if (links.length >= 3) {
						links(0).asInstanceOf[org.scalajs.dom.html.Anchor].href = s"https://doi.org/${updated.doi}"
						links(1).asInstanceOf[org.scalajs.dom.html.Anchor].href = s"https://commons.datacite.org/doi.org/${updated.doi}"
						links(2).asInstanceOf[org.scalajs.dom.html.Anchor].href = s"https://doi.datacite.org/doi.org/${updated.doi}"
					}
					// Update toolbar badge
					toolbar.updateBadge(updated.state)
					// Update the list view with the new state
					d.dispatch(DoiUpdated(updated))
			}
		}))
	}

	private def showCloneBanner(): Unit = {
		// Delay slightly to ensure DOM is fully rendered
		org.scalajs.dom.window.setTimeout(() => {
			// Show the clone banner with animation
			cloneBanner.style.display = "block"
			// Force a reflow to ensure the transition works
			val _ = cloneBanner.offsetHeight
			// Trigger the animation
			cloneBanner.style.opacity = "1"
			cloneBanner.style.transform = "translateY(0)"

			// Auto-dismiss after 8 seconds
			org.scalajs.dom.window.setTimeout(() => {
				hideCloneBanner()
			}, 8000)
		}, 100)
	}

	private def hideCloneBanner(): Unit = {
		cloneBanner.style.opacity = "0"
		cloneBanner.style.transform = "translateY(-10px)"
		// Wait for transition to complete before hiding
		org.scalajs.dom.window.setTimeout(() => {
			cloneBanner.style.display = "none"
		}, 300)
	}
}
