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

	private val listElem = ul(cls := "list-group").render

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

	private val stateFilterSelect = select(
		cls := "form-select",
		onchange := (searchDoi _)
	)(
		option(value := "", selected)("All states"),
		option(value := "findable")("Findable"),
		option(value := "registered")("Registered"),
		option(value := "draft")("Draft")
	).render

	private val searchInput = input(
		tpe := "search", cls := "form-control",
		placeholder := "Search DOI",
		onchange := (searchDoi _)
	).render

	private def searchDoi(): Unit = {
		val stateFilter = if (stateFilterSelect.value.isEmpty) None else Some(stateFilterSelect.value)
		d.dispatch(ThunkActions.DoiListRefreshRequest(Some(searchInput.value), None, stateFilter))
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

	private val paginationElem = div(cls := "mt-3").render

	private val searchCreateControls = div(cls := "d-md-flex justify-content-between")(
		p(
			cls := "d-flex",
			div(cls := "input-group me-2", style := "max-width: 120px")(
				stateFilterSelect
			),
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
		listElem,
		paginationElem
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
		val stateFilter = if (stateFilterSelect.value.isEmpty) None else Some(stateFilterSelect.value)
		d.dispatch(ThunkActions.DoiListRefreshRequest(Some(searchInput.value), Some(page), stateFilter))
	}

	def setPagination(listMeta: Option[DoiListMeta]): Unit = {
		paginationElem.innerHTML = ""
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

			paginationElem.appendChild(
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

	def setSearchQuery(text: String): Unit = {
		searchInput.value = text
	}

	private[this] val errorView = new ErrorView(400, 300, d)

	def appendError(msg: String): Unit = errorView.appendError(msg)

	def clearErrors(): Unit = errorView.clearErrors()

	def resetDoiAdder(): Unit = {
		suffixInput.value = ""
	}

	updateDefaultPrefix()
}
