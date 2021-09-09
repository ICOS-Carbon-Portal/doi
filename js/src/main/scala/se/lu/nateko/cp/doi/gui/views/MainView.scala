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
import se.lu.nateko.cp.doi.gui.DoiWithTitle

import scala.collection.Seq
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.ThunkActions

class MainView(d: DoiRedux.Dispatcher) {

	val doiViews = scala.collection.mutable.Map.empty[Doi, DoiView]

	private val listElem = ul(cls := "list-unstyled").render

	private val prefixSpan = span(cls := "input-group-text").render

	private val suffixInput = input(
		tpe := "text", cls := "form-control",
		//disabled := true,
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
		onsearch := (searchDoi _)
	).render

	private def searchDoi(): Unit = {
		d.dispatch(ThunkActions.DoiListRefreshRequest(Some(searchInput.value)))
	}

	private val searchSubmitButton = button(
		cls := "btn btn-secondary",
		onclick := (searchDoi _)
	)("Search")

	private val doiSearchForm = p(cls := "input-group")(
		searchInput,
		searchSubmitButton
	)

	val element = div(id := "main")(
		div(cls := "new-doi-input")(
			p(
				div(cls := "input-group")(
					prefixSpan,
					suffixInput,
					makeSuffixButton,
					addDoiButton
				)
			)
		),
		doiSearchForm,
		listElem
	)

	def updateDefaultPrefix(): Unit = {
		prefixSpan.textContent = getPrefix
	}

	def supplyDoiList(dois: Seq[DoiMeta], isLoading: Boolean): Unit = {
		listElem.innerHTML = ""
		doiViews.clear()

		if(isLoading) {
			listElem.appendChild(h3("Fetching DOI list from DataCite...").render)
			listElem.appendChild(
				div(cls := "progress")(
					div(cls := "progress-bar progress-bar-striped active", role := "progressbar",
						attr("aria-valuenow") := 100, style := "width: 100%"
					)
				).render
			)
		} else if(dois.isEmpty) {
			listElem.appendChild(p("No DOIs found").render)
		} else for(doi <- dois) {
			val doiView = doiViews.getOrElseUpdate(doi.doi, new DoiView(doi, d))
			doiView.updateContentVisibility()
			listElem.appendChild(doiView.element)
		}
	}

	def setSelected(doi: Doi, isSelected: Boolean): Unit = {
		doiViews.get(doi).foreach(_.setSelected(isSelected))
	}

	private[this] val errorView = new ErrorView(400, 300, d)

	def appendError(msg: String): Unit = errorView.appendError(msg)

	def clearErrors(): Unit = errorView.clearErrors()

	def resetDoiAdder(): Unit = {
		suffixInput.value = ""
	}

	updateDefaultPrefix()
}
