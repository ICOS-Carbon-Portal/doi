package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.Award
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget.apply
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget

class AwardWidget(
	init: Award,
	protected val updateCb: Award => Unit
) extends EntityWidget[Award]{

	private[this] var _award = init

	private[this] def validate(): Unit = highlightError(awardUriInput.element, _award.error)

	private val awardTitleInput  = awardTextWidget(_award.awardTitle,  textOpt => _award.copy(awardTitle  = textOpt), "Award title")
	private val awardNumberInput = awardTextWidget(_award.awardNumber, textOpt => _award.copy(awardNumber = textOpt), "Award number")
	private val awardUriInput    = awardTextWidget(_award.awardUri,    textOpt => _award.copy(awardUri    = textOpt), "Award URI")

	private def awardTextWidget(init: Option[String], update: Option[String] => Award, placeHolder: String) =
		new TextInputWidget(
			init.getOrElse(""),
			str => {
				_award = update(Option(str.trim).filterNot(_.isEmpty))
				if (placeHolder == "Award URI") validate()
				updateCb(_award)
			},
			placeHolder
		)

	val element = div(cls := "row spacyrow g-3")(
		div(cls := "col-md-6")(
			label(cls := "form-label")("Award title"),
			div(awardTitleInput.element),
		),
		div(cls := "col-md-6")(
			label(cls := "form-label")("Award number"),
			div(awardNumberInput.element),
		),
		div(cls := "col-md-6")(
			label(cls := "form-label")("Award URI"),
			div(awardUriInput.element),
		),
	).render
}