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
import se.lu.nateko.cp.doi.meta.Award

class FundingWidget(init: FundingReference, protected val updateCb: FundingReference => Unit) extends EntityWidget[FundingReference] {

	private var _fundingRef = init

	private val funderIdsInput = new FunderIdentifierWidget(
		init.funderIdentifier.getOrElse(FunderIdentifier.default),
		id => {
			_fundingRef = _fundingRef.copy(funderIdentifier = Some(id))
			updateCb(_fundingRef)
		}
	)

	private val funderNameInput  = new TextInputWidget(init.funderName.getOrElse(""), str => {
				_fundingRef = _fundingRef.copy(funderName = Option(str.trim).filterNot(_.isEmpty))
				updateCb(_fundingRef)
			},
			"Funder name",
			required = true)
			
	private val awardInput = new AwardWidget(init.award.getOrElse(Award.default), a => {
			_fundingRef = _fundingRef.copy(award = Some(a))
			updateCb(_fundingRef)
		}
	)

	val element = div(cls := "row")(
		div(cls := "row spacyrow")(
			div(cls := "col-md-1")(strong("Funder name")),
			div(cls := "col-md-3")(funderNameInput.element)(paddingBottom := 15),
			div(cls := "col-md-8")(funderIdsInput.element)(paddingBottom := 15)
		),
		awardInput.element
	).render

}
