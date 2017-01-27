package se.lu.nateko.cp.doi.gui.views

import org.scalajs.dom.console
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.gui.DoiAction
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions.requestNewDoi

class MainView(d: DoiRedux.Dispatcher) {

	val doiViews = scala.collection.mutable.Map.empty[Doi, DoiView]

	private val listElem = ul(cls := "list-unstyled").render

	private val prefixSpan = span(cls := "input-group-addon")(d.getState.prefix + "/").render

	private val suffixInput = input(
		tpe := "text", cls := "form-control",
		onkeyup := (refreshDoiAdder _),
		placeholder := "New DOI suffix"
	).render

	private def addDoi(): Unit = {
		addDoiButton.disabled = true
		d.dispatch(requestNewDoi(suffixInput.value))
	}

	private val addDoiButton = button(
		cls := "btn btn-default",
		tpe := "button",
		onclick := (addDoi _)
	)("Add new DOI").render

	val element = div(id := "main")(
		Bootstrap.basicPanel(
			div(cls := "input-group")(
				prefixSpan,
				suffixInput,
				span(cls := "input-group-btn")(addDoiButton)
			)
		),
		listElem
	)

	def supplyDoiList(dois: Seq[Doi]): Unit = {
		listElem.innerHTML = ""
		doiViews.clear()

		for(doi <- dois) {
			val doiView = new DoiView(doi, d)
			doiViews += ((doi, doiView))
			listElem.appendChild(doiView.element)
		}
	}

	def setSelected(doi: Doi, isSelected: Boolean): Unit = {
		doiViews.get(doi).foreach(_.setSelected(isSelected))
	}

	def supplyInfo(info: DoiInfo): Unit = {
		doiViews.get(info.meta.id).foreach(_.supplyInfo(info))
	}

	private[this] val errorView = new ErrorView(400, 300, d)

	def appendError(msg: String): Unit = errorView.appendError(msg)

	def clearErrors(): Unit = errorView.clearErrors()

	def refreshDoiAdder(): Unit = {
		val state = d.getState

		prefixSpan.textContent = state.prefix
		suffixInput.value = suffixInput.value.toUpperCase

		if(suffixInput.value.isEmpty){
			setError(None)
			addDoiButton.disabled = true
		}else{
			val doi = Doi(state.prefix, suffixInput.value)
			val error = doi.error.orElse{
				if(state.dois.contains(doi) || state.alreadyExists.contains(doi))
					Some("This DOI exists already!")
				else None
			}
			setError(error)
			addDoiButton.disabled = error.isDefined
		}

	}

	private def setError(err: Option[String]): Unit = {
		suffixInput.style.background = err.map(_ => Constants.errorInputBackground).getOrElse("")
		suffixInput.title = err.getOrElse("")
	}

	def resetDoiAdder(): Unit = {
		suffixInput.value = ""
		refreshDoiAdder()
	}

	refreshDoiAdder()
}
