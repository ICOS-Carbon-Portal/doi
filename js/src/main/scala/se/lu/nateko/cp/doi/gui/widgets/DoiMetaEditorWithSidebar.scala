package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.DoiMeta
import scala.concurrent.Future
import org.scalajs.dom.Event

class DoiMetaEditorWithSidebar(
	init: DoiMeta,
	updater: DoiMeta => Future[Unit],
	toolbar: UnifiedToolbar
) {

	private val metaWidget = new DoiMetaWidget(init, updater, toolbar)

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

	private val sidebar = div(
		cls := "toc-sidebar-editor"
	)(
		div(cls := "toc-body")(tocContent)
	).render

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

	setupTOCLinks()

	val element = div(
		cls := "doi-editor-with-sidebar"
	)(
		div(cls := "editor-content")(metaWidget.element),
		sidebar
	).render

	def wireToolbarCallbacks(): Unit = metaWidget.wireToolbarCallbacks()
}
