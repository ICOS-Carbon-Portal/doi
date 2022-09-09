package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.html.Element
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.MultiEntitiesEditWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextAreaWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.Description
import se.lu.nateko.cp.doi.meta.DescriptionType
import se.lu.nateko.cp.doi.meta.FunderIdentifier
import se.lu.nateko.cp.doi.meta.FunderIdentifierScheme
import se.lu.nateko.cp.doi.meta.FundingReference
import se.lu.nateko.cp.doi.meta.GenericName

class FundingWidget(init: FundingReference, protected val updateCb: FundingReference => Unit) extends EntityWidget[FundingReference] {

	private var _fundingRef = init

	private[this] def validate(): Unit = highlightError(awardUriInput.element, _fundingRef.error)

	private val funderIdsInput = new FunderIdentifierWidget(
		init.funderIdentifier.getOrElse(FunderIdentifier.default),
		id => {
			_fundingRef = _fundingRef.copy(funderIdentifier = Some(id))
			updateCb(_fundingRef)
		}
	)

	private val funderNameInput  = fundTextWidget(init.funderName,  textOpt => _fundingRef.copy(funderName  = textOpt), "Funder name")
	private val awardTitleInput  = fundTextWidget(init.awardTitle,  textOpt => _fundingRef.copy(awardTitle  = textOpt), "Award title")
	private val awardNumberInput = fundTextWidget(init.awardNumber, textOpt => _fundingRef.copy(awardNumber = textOpt), "Award number")
	private val awardUriInput    = fundTextWidget(init.awardUri,    textOpt => _fundingRef.copy(awardUri    = textOpt), "Award URI")

	private def fundTextWidget(init: Option[String], update: Option[String] => FundingReference, placeHolder: String) =
		new TextInputWidget(
			init.getOrElse(""),
			str => {
				_fundingRef = update(Option(str.trim).filterNot(_.isEmpty))
				if (placeHolder == "Award URI") validate()
				updateCb(_fundingRef)
			},
			placeHolder
		)

	val element = div(cls := "row")(
		div(cls := "row spacyrow")(
			div(cls := "col-md-1")(strong("Funder name*:")),
			div(cls := "col-md-3")(funderNameInput.element)(paddingBottom := 15),
			div(cls := "col-md-8")(funderIdsInput.element)(paddingBottom := 15)
		),
		div(cls := "row spacyrow")(
			div(cls := "col-md-1")(strong("Award title:")),
			div(cls := "col-md-3")(awardTitleInput.element),
			div(cls := "col-md-8")(div(cls := "row")(
				div(cls := "col-md-2")(strong("Award number:")),
				div(cls := "col-md-4")(awardNumberInput.element),
				div(cls := "col-md-2")(strong("Award URI:")),
				div(cls := "col-md-4")(awardUriInput.element)
			))
		)
	).render

}
