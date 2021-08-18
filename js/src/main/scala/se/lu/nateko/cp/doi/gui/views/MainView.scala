package se.lu.nateko.cp.doi.gui.views

import org.scalajs.dom.console
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.gui.DoiAction
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.CoolDoi
import se.lu.nateko.cp.doi.gui.EmptyDoiCreation
import se.lu.nateko.cp.doi.gui.DoiWithTitle

import scala.collection.Seq
import se.lu.nateko.cp.doi.DoiMeta

class MainView(d: DoiRedux.Dispatcher) {

	val doiViews = scala.collection.mutable.Map.empty[Doi, DoiView]

	private val listElem = ul(cls := "list-unstyled").render

	private val prefixSpan = span(cls := "input-group-addon").render

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
		cls := "btn btn-default",
		tpe := "button",
		onclick := (genSuffix _)
	)("Generate suffix").render

	private val addDoiButton = button(
		cls := "btn btn-default",
		tpe := "button",
		disabled := true,
		onclick := (addDoi _)
	)("Add new DOI").render

	val element = div(id := "main")(
		div(cls := "new-doi-input")(
			Bootstrap.basicPanel(
				div(cls := "input-group")(
					prefixSpan,
					suffixInput,
					span(cls := "input-group-btn")(makeSuffixButton, addDoiButton)
				)
			)
		),
		listElem
	)

	def updateDefaultPrefix(): Unit = {
		prefixSpan.textContent = getPrefix
	}

	def supplyDoiList(dois: Seq[DoiMeta]): Unit = {
		listElem.innerHTML = ""
		doiViews.keys.toSeq.diff(dois).foreach(doiViews.remove)

		for(doi <- dois) {
			val doiView = doiViews.getOrElseUpdate(doi.doi, new DoiView(doi, d))
			doiView.updateContentVisibility()
			listElem.appendChild(doiView.element)
		}
	}

	def setSelected(doi: Doi, isSelected: Boolean): Unit = {
		doiViews.get(doi).foreach(_.setSelected(isSelected))
	}

	def supplyInfo(info: DoiInfo): Unit = {
		doiViews.get(info.meta.doi).foreach(_.supplyInfo(info))
	}

	private[this] val errorView = new ErrorView(400, 300, d)

	def appendError(msg: String): Unit = errorView.appendError(msg)

	def clearErrors(): Unit = errorView.clearErrors()

	def resetDoiAdder(): Unit = {
		suffixInput.value = ""
	}

	updateDefaultPrefix()
}
