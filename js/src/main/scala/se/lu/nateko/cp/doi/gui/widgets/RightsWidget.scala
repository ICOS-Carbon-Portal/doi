package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Rights
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget

class RightsWidget(init: Rights, protected val updateCb: Rights => Unit) extends EntityWidget[Rights] {
	private[this] var _rights = init

	private def validate(): Unit = highlightError(idInput.element, _rights.error)

	private[this] val idInput = new SelectWidget[Rights](
		SelectWidget.selectOptions(Some("License"))(Rights.supported: _*),
		Some(init),
		rightsOpt => {
			_rights = _rights.copy(rights = _rights.rights)
			validate()
			updateCb(_rights)
		}
	)

	private[this] val statementInput = new TextInputWidget(init.rights, rs => {
		_rights = _rights.copy(rights = rs)
		updateCb(_rights)
	}, "Rights management statement", required = true)

	val element = div(cls := "row")(
		div(cls := "col-md-6")(statementInput.element),
		).render
}
