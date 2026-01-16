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

	// External links for DOI and DataCite
	private val doiUrl = "https://doi.org/" + meta.doi
	private val dataciteUrl = "https://commons.datacite.org/doi.org/" + meta.doi
	private val fabricaUrl = "https://doi.datacite.org/doi.org/" + meta.doi

	private val headerSection = div(cls := "mb-3 pt-3")(
		h1(id := "detail-title", style := "transition: font-size 0.2s ease;")(title),
		div(cls := "d-flex align-items-center gap-2 mb-3")(
			p(cls := "text-muted mb-0", style := "user-select: all;")(s"${meta.doi}"),
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
		headerSection,
		toolbar.element
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
		stickyHeader,
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

		// Add scroll listener to reduce h1 size when header is sticky
		val h1Element = org.scalajs.dom.document.getElementById("detail-title").asInstanceOf[org.scalajs.dom.html.Heading]
		val stickyHeaderElement = org.scalajs.dom.document.getElementById("sticky-header").asInstanceOf[org.scalajs.dom.html.Div]
		val headerOffsetTop = stickyHeaderElement.offsetTop

		org.scalajs.dom.window.addEventListener("scroll", (_: Event) => {
			val scrollY = org.scalajs.dom.window.pageYOffset
			// Only reduce size when the header has reached the top (is stuck)
			if (scrollY >= headerOffsetTop) {
				h1Element.style.fontSize = "1.75rem"
			} else {
				h1Element.style.fontSize = ""
			}
		})
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
}
