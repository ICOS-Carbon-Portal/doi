package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._

import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget
import se.lu.nateko.cp.doi.meta.FunderIdentifier
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.FunderIdentifierScheme

class FunderIdentifierWidget(
	init: FunderIdentifier,
	protected val updateCb: FunderIdentifier => Unit
) extends EntityWidget[FunderIdentifier]{

	private var _funderId = init

	private def validate(): Unit = highlightError(idInput.element, _funderId.error)

	private val idInput = new TextInputWidget(init.funderIdentifier.getOrElse(""), newId => {
		val funderIdOpt = if(newId.isEmpty) None else Some(newId)		
		_funderId = _funderId.copy(funderIdentifier = funderIdOpt)
		validate()
		updateCb(_funderId)
	}, "Funder identifier")

	private val schemeInput = new SelectWidget[FunderIdentifierScheme](
		SelectWidget.selectOptions(Some("Funder ID scheme"))(FunderIdentifierScheme.supported: _*),
		init.scheme,
		schemeOpt => {
			_funderId = _funderId.copy(scheme = schemeOpt)
			validate()
			updateCb(_funderId)
		}
	)

	val element = div(cls := "row spacyrow g-3")(
		div(cls := "col-md-6")(
			label(cls := "form-label")("Funder identifier"),
			div(idInput.element),
		),
		div(cls := "col-md-6")(
			label(cls := "form-label")("Scheme"),
			div(schemeInput.element),
		),
	).render
}
