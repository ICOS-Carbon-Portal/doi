package se.lu.nateko.cp.doi.gui.views

import org.scalajs.dom.console
import org.scalajs.dom.{Event, KeyboardEvent, html}
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.gui.DoiAction
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.CoolDoi
import se.lu.nateko.cp.doi.gui.EmptyDoiCreation

import scala.collection.Seq
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.DoiListMeta
import org.scalajs.dom.document
import scala.scalajs.js.timers.setTimeout
import scala.concurrent.duration.DurationInt

class MainView(d: DoiRedux.Dispatcher) {

	val doiViews = scala.collection.mutable.Map.empty[Doi, DoiView]
	private var currentDetailView: Option[DoiDetailView] = None

	private val listElem = ul(cls := "list-unstyled").render

	private val prefixSpan = span(cls := "input-group-text").render

	private val suffixInput = input(
		tpe := "text", cls := "form-control",
		placeholder := "New DOI suffix"
	).render

	private def addDoi(): Unit = {
		addDoiButton.disabled = true
		d.dispatch(EmptyDoiCreation(Doi(getPrefix, suffixInput.value)))
	}

	private def genSuffix(): Unit = {
		suffixInput.value = CoolDoi.makeRandom
		addDoiButton.disabled = false
	}

	private def getPrefix = d.getState.prefix

	private val makeSuffixButton = button(
		cls := "btn btn-secondary",
		tpe := "button",
		onclick := (genSuffix _)
	)("Generate suffix").render

	private val addDoiButton = button(
		cls := "btn btn-secondary",
		tpe := "button",
		disabled := true,
		onclick := (addDoi _)
	)("Add new DOI").render

	private val searchInput = input(
		tpe := "search", cls := "form-control",
		placeholder := "Search DOI",
		onchange := (searchDoi _)
	).render

	private def searchDoi(): Unit = {
		d.dispatch(ThunkActions.DoiListRefreshRequest(Some(searchInput.value)))
	}

	private val searchSubmitButton = button(
		cls := "btn btn-secondary",
		onclick := (searchDoi _)
	)("Search")

	private val infoLink = a(
		href := "https://support.datacite.org/docs/api-queries#using-the-query-parameter",
		target := "_blank",
		cls := "icon-link px-3 text-nowrap"
	)("Search documentation")

	private val searchResultsStats = p.render

	private val searchCreateControls = div(cls := "d-md-flex justify-content-between")(
		p(
			cls := "d-flex",
			div(cls := "input-group")(
				searchInput,
				searchSubmitButton
			),
			infoLink
		),
		p(cls := "edit-control")(
			div(cls := "input-group")(
				prefixSpan,
				suffixInput,
				makeSuffixButton,
				addDoiButton
			)
		),
	)

	val element = div(id := "main")(
		searchCreateControls,
		searchResultsStats,
		listElem
	)

	def updateDefaultPrefix(): Unit = {
		prefixSpan.textContent = getPrefix
	}

	def supplyDoiList(dois: Seq[DoiMeta], isLoading: Boolean): Unit = {
		listElem.innerHTML = ""
		doiViews.clear()

		if(dois.isEmpty) {
			if(!isLoading) listElem.appendChild(p("No DOIs found").render)
			else{
				listElem.appendChild(h3("Fetching DOI list from DataCite...").render)
				listElem.appendChild(
					div(cls := "progress")(
						div(cls := "progress-bar progress-bar-striped active", role := "progressbar",
							attr("aria-valuenow") := 100, style := "width: 100%"
						)
					).render
				)
			}
		} else for(doi <- dois) {
			val doiView = doiViews.getOrElseUpdate(doi.doi, new DoiView(doi, d))
			doiView.updateContentVisibility()
			listElem.appendChild(doiView.element)
		}
	}

	def goToPage(event: Event, page: Int) = {
		event.preventDefault()
		d.dispatch(ThunkActions.DoiListRefreshRequest(Some(searchInput.value), Some(page)))
	}

	def setPagination(listMeta: Option[DoiListMeta]): Unit = {
		listMeta.map(listMeta => {
			val previousBtnClasses = "page-item" + (if(listMeta.page == 1) " disabled" else "")
			val nextBtnClasses = "page-item" + (if(listMeta.page >= listMeta.totalPages) " disabled" else "")

			val searchResultsCountText = if (listMeta.total == 1) "DOI" else "DOIs"
			searchResultsStats.innerHTML = ""
			searchResultsStats.appendChild(
				div(cls := "d-flex justify-content-between")(
					span(s"${listMeta.total} $searchResultsCountText"),
					span(s"Page ${listMeta.page}/${listMeta.totalPages}")
				).render
			)

			listElem.appendChild(
				ul(cls := "pagination justify-content-center")(
					li(cls := previousBtnClasses)(
						a(
							cls := "page-link",
							href := "#",
							onclick := {goToPage(_, listMeta.page - 1) },
						)("Previous")
					),
					li(cls := nextBtnClasses)(
						a(
							cls := "page-link",
							href := "#",
							onclick := {goToPage(_, listMeta.page + 1) }
						)("Next")
					)
				).render
			)
		})
	}

	def setSelected(doi: Doi, isSelected: Boolean): Unit = {
		doiViews.get(doi).foreach(_.setSelected(isSelected))
	}

	def setSearchQuery(text: String): Unit = {
		searchInput.value = text
	}

	private[this] val errorView = new ErrorView(400, 300, d)

	def appendError(msg: String): Unit = errorView.appendError(msg)

	def clearErrors(): Unit = errorView.clearErrors()

	def resetDoiAdder(): Unit = {
		suffixInput.value = ""
	}

	def showDetailView(doi: Doi): Unit = {
		// Find the DOI metadata from the current list
		d.getState.dois.find(_.doi == doi).foreach { meta =>
			val detailView = new DoiDetailView(meta, d)
			currentDetailView = Some(detailView)
			
			// Replace the main element content
			val mainWrapper = document.getElementById("main-wrapper")
			mainWrapper.innerHTML = ""
			mainWrapper.appendChild(detailView.element.render)
			detailView.initialize()
			
			// Scroll to top after DOM renders and layout is complete
			org.scalajs.dom.window.requestAnimationFrame { _ =>
				org.scalajs.dom.window.requestAnimationFrame { _ =>
					// Force instant scroll by setting scroll-behavior to auto temporarily
					import org.scalajs.dom.HTMLElement
					val html = org.scalajs.dom.document.documentElement.asInstanceOf[HTMLElement]
					val body = org.scalajs.dom.document.body
					val oldHtmlBehavior = html.style.getPropertyValue("scroll-behavior")
					val oldBodyBehavior = body.style.getPropertyValue("scroll-behavior")
					html.style.setProperty("scroll-behavior", "auto")
					body.style.setProperty("scroll-behavior", "auto")
					org.scalajs.dom.window.scrollTo(0, 0)
					html.style.setProperty("scroll-behavior", oldHtmlBehavior)
					body.style.setProperty("scroll-behavior", oldBodyBehavior)
				}
			}
		}
	}

	def showListView(): Unit = {
		currentDetailView = None
		// Replace with list view
		val mainWrapper = document.getElementById("main-wrapper")
		mainWrapper.innerHTML = ""
		mainWrapper.appendChild(element.render)
		// Refresh the list display
		supplyDoiList(d.getState.dois, d.getState.isLoading)
		setPagination(d.getState.listMeta)
	}

	updateDefaultPrefix()
}
