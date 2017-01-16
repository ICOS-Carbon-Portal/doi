package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.meta.Title
import se.lu.nateko.cp.doi.meta.TitleType
import scalatags.JsDom.all._
import org.scalajs.dom.Event

import TitleWidget._

class TitleWidget(init: Title, updated: () => Unit, removed: TitleWidget => Unit) extends EntityEditWidget[Title] {

	def entityValue = _title
	private[this] var _title = init

	private[this] val removeButton =
		button(tpe := "button", cls := "btn btn-warning")(
			span(cls := "glyphicon glyphicon-remove")
		).render

	removeButton.onclick = (_: Event) => removed(this)

	private[this] val titleInput =
		input(tpe := "text", cls := "form-control", value := _title.title).render

	titleInput.onkeyup = (_: Event) => {
		_title = _title.copy(title = titleInput.value)
		updated()
	}

	private[this] val titleTypeInput =
		select(name := "Title type", cls := "form-control")(titleTypeOptions).render

	titleTypeInput.onchange = (_: Event) => {
		val idx = titleTypeInput.selectedIndex
		val ttype = if(idx > 0) Some(TitleType(idx - 1)) else None
		_title = _title.copy(titleType = ttype)
		updated()
	}

	private[this] val languageInput =
		select(name := "language", cls := "form-control")(
			option(value := "")("Language"),
			option(value := "en-uk")("en-uk"),
			option(value := "en-us")("en-us")
		).render

	languageInput.onchange = (_: Event) => {
		val langOpt = if(languageInput.selectedIndex > 0) Some(languageInput.value) else None
		_title = _title.copy(lang = langOpt)
		updated()
	}

	def setRemovability(removable: Boolean): Unit = {
		removeButton.disabled = !removable
	}

	val element = Bootstrap.basicPanel(
		div(cls := "row")(
			div(cls := "col-md-7")(titleInput),
			div(cls := "col-md-2")(titleTypeInput),
			div(cls := "col-md-2")(languageInput),
			div(cls := "col-md-1")(removeButton)
		)
	).render
}


object TitleWidget{

	private val titleTypeOptions = option(value := "")("Title type") +:
			TitleType.values.toSeq.map{tt =>
				val ttName = tt.toString
				option(value := ttName)(ttName)
			}
	
}
