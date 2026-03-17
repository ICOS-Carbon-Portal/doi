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

	private val tocSections: Seq[(String, String, Seq[ValidationSection])] = Seq(
		("toc-required", "Required properties", Seq(
			ValidationSection.DoiTarget,
			ValidationSection.Creators,
			ValidationSection.Titles,
			ValidationSection.Publisher,
			ValidationSection.PublicationYear,
			ValidationSection.ResourceType
		)),
		("toc-recommended", "Recommended properties", Seq(
			ValidationSection.Subjects,
			ValidationSection.Contributors,
			ValidationSection.Dates,
			ValidationSection.RelatedIdentifiers,
			ValidationSection.Rights,
			ValidationSection.Descriptions,
			ValidationSection.Geolocations
		)),
		("toc-optional", "Optional properties", Seq(
			ValidationSection.Formats,
			ValidationSection.Version,
			ValidationSection.Funding
		))
	)

	private val tocContent = ul(cls := "list-unstyled mb-0")(
		tocSections.zipWithIndex.flatMap { case ((sectionId, sectionLabel, items), idx) =>
			val sectionCls = if idx == 0 then "toc-section" else "toc-section mt-3"
			val header = li(cls := sectionCls)(
				a(href := s"#$sectionId", cls := "toc-link toc-section-link")(sectionLabel)
			)
			val entries = items.map { vs =>
				li(cls := "toc-item")(
					a(href := s"#${vs.id}", cls := "toc-link")(vs.label)
				)
			}
			header +: entries
		}.toList: _*
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

	def currentMeta: DoiMeta = metaWidget.currentMeta

	def wireToolbarCallbacks(): Unit = metaWidget.wireToolbarCallbacks()
}
