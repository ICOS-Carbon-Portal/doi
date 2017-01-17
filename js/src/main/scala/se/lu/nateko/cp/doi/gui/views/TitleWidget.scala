package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.meta.Title
import se.lu.nateko.cp.doi.meta.TitleType
import scalatags.JsDom.all._
import org.scalajs.dom.Event

import TitleWidget._

class TitleWidget(init: Title, protected val updateCb: Title => Unit) extends EntityWidget[Title] {

	private[this] var _title = init

	private[this] val titleInput = new TextInputWidget(init.title, v => {
		_title = _title.copy(title = v)
		updateCb(_title)
	})

	private[this] val titleTypeInput = new SelectWidget[TitleType.Value](titleTypeOptions, init.titleType, ttOpt => {
		_title = _title.copy(titleType = ttOpt)
		updateCb(_title)
	})

	private[this] val languageInput = new SelectWidget[String](languageOptions, init.lang, langOpt => {
		_title = _title.copy(lang = langOpt)
		updateCb(_title)
	})

	val element = div(cls := "row")(
		div(cls := "col-md-8")(titleInput.element),
		div(cls := "col-md-2")(titleTypeInput.element),
		div(cls := "col-md-2")(languageInput.element)
	).render
}


object TitleWidget{

	private val titleTypeOptions =
		SelectOption[TitleType.Value](None, "", "Title type") +:
		TitleType.values.toIndexedSeq.map{tt =>
			val ttName = tt.toString
			SelectOption(Some(tt), ttName, ttName)
		}

	private val languageOptions = IndexedSeq(
		SelectOption[String](None, "", "Language"),
		SelectOption(Some("en-uk"), "en-uk", "English (UK)"),
		SelectOption(Some("en-us"), "en-us", "English (US)")
	)
}
