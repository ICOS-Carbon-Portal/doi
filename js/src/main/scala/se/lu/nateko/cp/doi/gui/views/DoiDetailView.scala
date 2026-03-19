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
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaEditorWithSidebar
import se.lu.nateko.cp.doi.gui.widgets.EditorTab
import se.lu.nateko.cp.doi.gui.widgets.UnifiedToolbar
import se.lu.nateko.cp.doi.gui.UserInfo
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.Backend
import se.lu.nateko.cp.doi.gui.ReportError
import se.lu.nateko.cp.doi.gui.ClearLastClonedDoi
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom.Event

class DoiDetailView(metaInit: DoiMeta, d: DoiRedux.Dispatcher, isClone: Boolean = false) {

	private[this] var meta = metaInit

	// Check user status by checking the main-wrapper class
	private val mainWrapper = org.scalajs.dom.document.getElementById("main-wrapper")
	private val isAdmin = mainWrapper != null && mainWrapper.classList.contains("is-admin")
	private val isLoggedIn = mainWrapper != null && mainWrapper.classList.contains("is-logged-in")

	// Logged-in non-admins can edit draft DOIs, admins can edit any DOI
	private val canEdit: Boolean = isAdmin || (isLoggedIn && metaInit.state == DoiPublicationState.draft)

	private val userInfo = UserInfo(isLoggedIn = isLoggedIn, isAdmin = isAdmin, canEdit = canEdit)

	// Users who can edit start with edit tab, otherwise view tab
	private val initialTab = if (canEdit) EditorTab.edit else EditorTab.view
	private var currentTab: EditorTab = initialTab

	private val backToList: Event => Unit = e => {
		e.preventDefault()
		d.dispatch(NavigateToRoute(ListRoute))
	}

	private val title = DoiMetaHelpers.extractTitle(meta)

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
		initialTab,
		userInfo
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

	private val stateClass = if (metaInit.state == DoiPublicationState.draft) "draft-doi" else "published-doi"

	val element = div(
		id := "detail-view",
		cls := s"position-relative $stateClass",
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

		if (canEdit) {
			contentBody.appendChild(metaEditorWithSidebar.element)
			metaEditorWithSidebar.wireToolbarCallbacks()
		} else {
			contentBody.appendChild(metaViewer.element)
		}

		setupStickyHeaderHeight()
	}

	private def setupStickyHeaderHeight(): Unit = {
		val height = stickyHeader.offsetHeight
		val root = org.scalajs.dom.document.documentElement.asInstanceOf[org.scalajs.dom.HTMLElement]
		root.style.setProperty("--sticky-header-height", s"${height}px")
	}

	private var metaViewer: DoiMetaViewer = new DoiMetaViewer(meta, toolbar)

	private def envProvider(): Option[String] = d.getState.activeEnv

	private var metaEditorWithSidebar = new DoiMetaEditorWithSidebar(
		meta,
		updateDoiMeta,
		toolbar,
		envProvider
	)

	private var metaJsonEditor = new DoiJsonEditor(meta, updateDoiMeta, toolbar)

	/** Get the working (unsaved) metadata from the currently active editor tab.
	  * Returns None only when the JSON tab has unparseable content. */
	private def workingMeta: Option[DoiMeta] = currentTab match {
		case EditorTab.edit => Some(metaEditorWithSidebar.currentMeta)
		case EditorTab.json => metaJsonEditor.currentMeta
		case EditorTab.view => Some(meta)
	}

	private def resolveWorkingMeta: Option[DoiMeta] = workingMeta.orElse {
		val stay = !org.scalajs.dom.window.confirm(
			"The JSON content is invalid and cannot be transferred to the editor.\n\n" +
			"Switch anyway? Your unsaved JSON changes will be lost."
		)
		if stay then None else Some(meta)
	}

	private lazy val tabsCb: Map[EditorTab, () => Unit] = Map(
		EditorTab.view -> {() =>
			resolveWorkingMeta match {
				case Some(working) =>
					currentTab = EditorTab.view
					metaViewer = new DoiMetaViewer(working, toolbar)
					contentBody.replaceChildren(metaViewer.element)
					toolbar.setTab(EditorTab.view)
					toolbar.setUpdateButtonEnabled(false)
				case None =>
					toolbar.setTab(currentTab)
			}
		},
		EditorTab.edit -> {() =>
			resolveWorkingMeta match {
				case Some(working) =>
					currentTab = EditorTab.edit
					metaEditorWithSidebar = new DoiMetaEditorWithSidebar(
						working,
						updateDoiMeta,
						toolbar,
						envProvider,
						savedMeta = Some(meta)
					)
					contentBody.replaceChildren(metaEditorWithSidebar.element)
					toolbar.setTab(EditorTab.edit)
					metaEditorWithSidebar.wireToolbarCallbacks()
				case None =>
					toolbar.setTab(currentTab)
			}
		},
		EditorTab.json -> {() =>
			val working = currentTab match {
				case EditorTab.edit => Some(metaEditorWithSidebar.currentMeta)
				case _ => Some(meta)
			}
			working.foreach { w =>
				currentTab = EditorTab.json
				metaJsonEditor = new DoiJsonEditor(w, updateDoiMeta, toolbar)
				contentBody.replaceChildren(metaJsonEditor.element)
				toolbar.setTab(EditorTab.json)
				metaJsonEditor.wireToolbarCallbacks()
			}
		},
	)

	private def updateDoiMeta(updated: DoiMeta): Future[Unit] = {
		Backend.updateMeta(updated, d.getState.activeEnv).flatMap { errorMsg =>
			if (errorMsg.isEmpty) {
				meta = updated

				metaViewer = new DoiMetaViewer(updated, toolbar)
				metaEditorWithSidebar = new DoiMetaEditorWithSidebar(
					updated,
					updateDoiMeta,
					toolbar,
					envProvider
				)
				metaJsonEditor = new DoiJsonEditor(updated, updateDoiMeta, toolbar)

				contentBody.innerHTML = ""
				currentTab match {
					case EditorTab.edit =>
						contentBody.appendChild(metaEditorWithSidebar.element)
						metaEditorWithSidebar.wireToolbarCallbacks()
					case EditorTab.json =>
						contentBody.appendChild(metaJsonEditor.element)
						metaJsonEditor.wireToolbarCallbacks()
					case EditorTab.view =>
						contentBody.appendChild(metaViewer.element)
						toolbar.setUpdateButtonEnabled(false)
				}

				val newTitle = DoiMetaHelpers.extractTitle(updated)
				headerSection.querySelector("h1").textContent = newTitle
				headerSection.querySelector("p").textContent = s"${updated.doi}"

				toolbar.updateBadge(updated.state)

				d.dispatch(DoiUpdated(updated))

				Future.successful(())
			} else {
				d.dispatch(ReportError(errorMsg))
				Future.failed(new Exception(errorMsg))
			}
		}.recoverWith {
			case exc: Throwable =>
				d.dispatch(ReportError(s"Failed to update DOI ${updated.doi}:\n${exc.getMessage}"))
				Future.failed(exc)
		}
	}

	private def showCloneBanner(): Unit = {
		org.scalajs.dom.window.setTimeout(() => {
			cloneBanner.style.display = "block"
			val _ = cloneBanner.offsetHeight
			cloneBanner.style.opacity = "1"
			cloneBanner.style.transform = "translateY(0)"

			org.scalajs.dom.window.setTimeout(() => {
				hideCloneBanner()
			}, 8000)
		}, 100)
	}

	private def hideCloneBanner(): Unit = {
		cloneBanner.style.opacity = "0"
		cloneBanner.style.transform = "translateY(-10px)"
		org.scalajs.dom.window.setTimeout(() => {
			cloneBanner.style.display = "none"
		}, 300)
	}
}
