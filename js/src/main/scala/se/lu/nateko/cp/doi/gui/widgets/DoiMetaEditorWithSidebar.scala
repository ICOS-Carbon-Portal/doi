package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import scala.concurrent.Future
import org.scalajs.dom.Event
import scala.collection.Seq
import se.lu.nateko.cp.doi.meta.ValidationError
import se.lu.nateko.cp.doi.meta.ValidationSection

class DoiMetaEditorWithSidebar(
	init: DoiMeta,
	updater: DoiMeta => Future[Unit],
	toolbar: UnifiedToolbar,
	envProvider: () => Option[String] = () => None
) {

	private var currentSidebarTab: String = "toc"
	private var currentErrors: Seq[ValidationError] = Seq.empty

	private val errorCountBadge = span(cls := "error-count-badge").render

	private val tocTab = button(
		cls := "sidebar-tab sidebar-tab-active",
		onclick := {(_: Event) => switchToTab("toc")}
	)("Contents").render

	private val errorsTab = button(
		cls := "sidebar-tab",
		onclick := {(_: Event) => switchToTab("errors")}
	)(
		"Errors ",
		errorCountBadge
	).render

	private val tabBar = div(cls := "sidebar-tabs")(tocTab, errorsTab).render

	private val errorListContent = ul(cls := "error-list list-unstyled mb-0").render

	// TODO: Base the TOC on the form content
	private val tocContent = ul(cls := "list-unstyled mb-0")(
		li(cls := "toc-section")(
			a(href := "#toc-required", cls := "toc-link toc-section-link")("Required properties")
		),
		li(cls := "toc-item")(
			a(href := "#toc-doi-target", cls := "toc-link")("DOI target")
		),
		li(cls := "toc-item")(
			a(href := "#toc-creators", cls := "toc-link")("Creators")
		),
		li(cls := "toc-item")(
			a(href := "#toc-titles", cls := "toc-link")("Titles")
		),
		li(cls := "toc-item")(
			a(href := "#toc-publisher", cls := "toc-link")("Publisher")
		),
		li(cls := "toc-item")(
			a(href := "#toc-publication-year", cls := "toc-link")("Publication year")
		),
		li(cls := "toc-item")(
			a(href := "#toc-resource-type", cls := "toc-link")("Resource type")
		),
		li(cls := "toc-section mt-3")(
			a(href := "#toc-recommended", cls := "toc-link toc-section-link")("Recommended properties")
		),
		li(cls := "toc-item")(
			a(href := "#toc-subjects", cls := "toc-link")("Subjects")
		),
		li(cls := "toc-item")(
			a(href := "#toc-contributors", cls := "toc-link")("Contributors")
		),
		li(cls := "toc-item")(
			a(href := "#toc-dates", cls := "toc-link")("Dates")
		),
		li(cls := "toc-item")(
			a(href := "#toc-related-identifiers", cls := "toc-link")("Related identifiers")
		),
		li(cls := "toc-item")(
			a(href := "#toc-rights", cls := "toc-link")("Rights")
		),
		li(cls := "toc-item")(
			a(href := "#toc-descriptions", cls := "toc-link")("Descriptions")
		),
		li(cls := "toc-item")(
			a(href := "#toc-geolocations", cls := "toc-link")("Geolocations")
		),
		li(cls := "toc-section mt-3")(
			a(href := "#toc-optional", cls := "toc-link toc-section-link")("Optional properties")
		),
		li(cls := "toc-item")(
			a(href := "#toc-formats", cls := "toc-link")("Formats")
		),
		li(cls := "toc-item")(
			a(href := "#toc-version", cls := "toc-link")("Version")
		),
		li(cls := "toc-item")(
			a(href := "#toc-funding", cls := "toc-link")("Funding references")
		)
	).render

	private val tocBody = div(cls := "toc-body", style := "display: block;")(tocContent).render
	private val errorBody = div(cls := "toc-body", style := "display: none;")(errorListContent).render

	private val sidebar = div(
		cls := "toc-sidebar-editor"
	)(
		tabBar,
		tocBody,
		errorBody
	).render

	private def switchToTab(tab: String): Unit = {
		currentSidebarTab = tab

		if (tab == "toc") {
			tocTab.classList.add("sidebar-tab-active")
			errorsTab.classList.remove("sidebar-tab-active")
			tocBody.style.display = "block"
			errorBody.style.display = "none"
		} else {
			tocTab.classList.remove("sidebar-tab-active")
			errorsTab.classList.add("sidebar-tab-active")
			tocBody.style.display = "none"
			errorBody.style.display = "block"
		}
	}

	private def updateErrorCountBadge(count: Int): Unit = {
		if (count > 0) {
			errorCountBadge.textContent = count.toString
		} else {
			errorCountBadge.textContent = ""
		}
	}

	private def navigateToSection(sectionId: String)(e: Event): Unit = {
		e.preventDefault()
		val targetElement = org.scalajs.dom.document.getElementById(sectionId)
		if (targetElement != null) {
			targetElement.asInstanceOf[scala.scalajs.js.Dynamic].scrollIntoView(
				scala.scalajs.js.Dynamic.literal(
					behavior = "smooth",
					block = "start"
				)
			)
		}
	}

	private def renderErrors(): Unit = {
		errorListContent.innerHTML = ""

		if (currentErrors.isEmpty) {
			val emptyState = li(cls := "error-empty-state")("No validation errors").render
			errorListContent.appendChild(emptyState)
		} else {
			currentErrors.foreach { err =>
				val errorItem = li(cls := "error-item")(
					a(
						href := s"#${err.section.id}",
						cls := "error-link",
						onclick := navigateToSection(err.section.id)
					)(
						i(cls := "fa-solid fa-triangle-exclamation error-icon"),
						div(cls := "error-content")(
							div(cls := "error-section-name")(err.section.label),
							div(cls := "error-message")(err.message)
						)
					)
				).render
				errorListContent.appendChild(errorItem)
			}
		}

		updateErrorCountBadge(currentErrors.length)
	}

	private def setupTOCLinks(): Unit = {
		val links = sidebar.querySelectorAll(".toc-link")
		for (i <- 0 until links.length) {
			val link = links(i).asInstanceOf[org.scalajs.dom.html.Anchor]
			link.onclick = (e: Event) => {
				e.preventDefault()
				val href = link.getAttribute("href")
				if (href != null && href.startsWith("#")) {
					val targetId = href.substring(1)
					val targetElement = org.scalajs.dom.document.getElementById(targetId)
					if (targetElement != null) {
						targetElement.asInstanceOf[scala.scalajs.js.Dynamic].scrollIntoView(
							scala.scalajs.js.Dynamic.literal(
								behavior = "smooth",
								block = "start"
							)
						)
					}
				}
			}
		}
	}

	private def updateErrors(errors: Seq[ValidationError]): Unit = {
		currentErrors = errors
		renderErrors()

		// Auto-switch to errors tab if there are errors
		if (errors.nonEmpty && currentSidebarTab == "toc") {
			switchToTab("errors")
		} else if (errors.isEmpty && currentSidebarTab == "errors") {
			// Switch back to TOC if all errors are resolved
			switchToTab("toc")
		}
	}

	private val metaWidget = new DoiMetaWidget(init, updater, toolbar, updateErrors, envProvider)

	setupTOCLinks()
	renderErrors() // Initialize with empty state

	val element = div(
		cls := "doi-editor-with-sidebar"
	)(
		div(cls := "editor-content")(metaWidget.element),
		sidebar
	).render

	def wireToolbarCallbacks(): Unit = metaWidget.wireToolbarCallbacks()
}
