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

	val element = div(cls := "row spacyrow")(
			div(cls := "col-md-1")(strong("Award title")),
			div(cls := "col-md-3")(awardTitleInput.element),
			div(cls := "col-md-8")(div(cls := "row")(
				div(cls := "col-md-2")(strong("Award number")),
				div(cls := "col-md-4")(awardNumberInput.element),
				div(cls := "col-md-2")(strong("Award URI")),
				div(cls := "col-md-4")(awardUriInput.element)
			))
		).render
}