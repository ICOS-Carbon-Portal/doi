package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._

import se.lu.nateko.cp.doi.meta.NameIdentifier
import se.lu.nateko.cp.doi.meta.NameIdentifierScheme
import se.lu.nateko.cp.doi.meta.NameIdentifierScheme._
import se.lu.nateko.cp.doi.gui.widgets.generic._

class NameIdentifierWidget(
	init: NameIdentifier,
	protected val updateCb: NameIdentifier => Unit
) extends EntityWidget[NameIdentifier]{

	private[this] var _nameId = init

	private[this] def validate(): Unit = highlightError(idInput.element, _nameId.error)

	private[this] val idInput = new TextInputWidget(init.nameIdentifier, newId => {
		_nameId = _nameId.copy(nameIdentifier = newId)
		validate()
		updateCb(_nameId)
	}, required = true)

	private[this] val schemeInput = new SelectWidget[NameIdentifierScheme](
		SelectWidget.selectOptions(None)(NameIdentifierScheme.values: _*),
		Some(init.scheme),
		schemeOpt => {
			schemeOpt.foreach{scheme =>
				_nameId = _nameId.copy(scheme = scheme)
				validate()
				updateCb(_nameId)
			}
		}
	)

	val element = div(cls := "row")(
		div(cls := "col-md-6")(idInput.element),
		div(cls := "col-md-3")(schemeInput.element)
	).render

	validate()
}
