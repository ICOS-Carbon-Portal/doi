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

	private[this] var _funderId = init

	private[this] def validate(): Unit = highlightError(idInput.element, _funderId.error)

	private[this] val idInput = new TextInputWidget(init.funderIdentifier.getOrElse(""), newId => {
		val funderIdOpt = if(newId.isEmpty) None else Some(newId)		
		_funderId = _funderId.copy(funderIdentifier = funderIdOpt)
		validate()
		updateCb(_funderId)
	}, "Funder identifier")

	private[this] val schemeInput = new SelectWidget[FunderIdentifierScheme](
		SelectWidget.selectOptions(Some("Funder id scheme"))(FunderIdentifierScheme.Regexes.keys.toIndexedSeq: _*),
		init.scheme,
		schemeOpt => {
			_funderId = _funderId.copy(scheme = schemeOpt)
			validate()
			updateCb(_funderId)
		})

	val element = 
		div(cls := "row")(
		div(cls := "col-md-6")(idInput.element),
		div(cls := "col-md-6")(schemeInput.element)
	).render
}
