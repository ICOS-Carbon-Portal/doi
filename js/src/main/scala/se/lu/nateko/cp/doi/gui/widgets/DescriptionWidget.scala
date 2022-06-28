package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Description
import se.lu.nateko.cp.doi.meta.DescriptionType
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic._

class DescriptionWidget(init: Description, protected val updateCb: Description => Unit) extends EntityWidget[Description] {

	private[this] var _descr = init

	private[this] val descrInput = new TextAreaWidget(init.description, v => {
		_descr = _descr.copy(description = v)
		updateCb(_descr)
	})(overflowY := "scroll", rows := 5)

	descrInput.element.style.setProperty("resize", "none")

	private[this] val descrTypeInput = new SelectWidget[DescriptionType](
		SelectWidget.selectOptions(Some("Description type"), DescriptionType.values),
		Option(init.descriptionType),
		dtOpt => {
			_descr = _descr.copy(descriptionType = dtOpt.getOrElse(null))
			updateCb(_descr)
		}
	)

	private[this] val languageInput = new SelectWidget[String](
		TitleWidget.languageOptions,
		init.lang,
		langOpt => {
			_descr = _descr.copy(lang = langOpt)
			updateCb(_descr)
		}
	)

	val element = div(cls := "row")(
		div(cls := "col-md-9")(descrInput.element),
		div(cls := "col-md-3")(
			div(cls := "row spacyrow", marginBottom := 15)(div(cls := "col-md-12")(descrTypeInput.element)),
			div(cls := "row spacyrow")(div(cls := "col-md-12")(languageInput.element))
		)
	).render
}

