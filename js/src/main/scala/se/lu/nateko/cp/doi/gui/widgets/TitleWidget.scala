package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Title
import se.lu.nateko.cp.doi.meta.TitleType
import scalatags.JsDom.all._
import TitleWidget._
import se.lu.nateko.cp.doi.gui.widgets.generic._

class TitleWidget(init: Title, protected val updateCb: Title => Unit) extends EntityWidget[Title] {

	private[this] var _title = init

	private[this] val titleInput = new TextInputWidget(init.title, v => {
		_title = _title.copy(title = v)
		updateCb(_title)
	}, "Title", required = true)

	private[this] val titleTypeInput = new SelectWidget[TitleType](
		SelectWidget.selectOptions(Some("Title type"), TitleType.values),
		init.titleType,
		ttOpt => {
			_title = _title.copy(titleType = ttOpt)
			updateCb(_title)
		}
	)

	private[this] val languageInput = new SelectWidget[String](languageOptions, init.lang, langOpt => {
		_title = _title.copy(lang = langOpt)
		updateCb(_title)
	})

	val element = div(cls := "row")(
		div(cls := "col-md-8 spacyrow")(titleInput.element),
		div(cls := "col-md-2 spacyrow")(titleTypeInput.element),
		div(cls := "col-md-2 spacyrow")(languageInput.element)
	).render
}


object TitleWidget{

	val languageOptions = IndexedSeq(
		SelectOption[String](None, "", "Language"),
		SelectOption(Some("en-uk"), "en-uk", "English (UK)"),
		SelectOption(Some("en-us"), "en-us", "English (US)")
	)
}
