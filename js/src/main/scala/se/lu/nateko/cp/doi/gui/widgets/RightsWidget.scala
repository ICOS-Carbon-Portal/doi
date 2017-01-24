package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Rights
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget

class RightsWidget(init: Rights, protected val updateCb: Rights => Unit) extends EntityWidget[Rights] {

	private[this] var _rights = init

	private[this] val statementInput = new TextInputWidget(init.rights, rs => {
		_rights = _rights.copy(rights = rs)
		updateCb(_rights)
	}, "Rights management statement")

	private[this] val urlInput = new TextInputWidget(init.rightsUri.getOrElse(""), uri => {
		val uriOpt = if(uri.isEmpty) None else Some(uri)
		_rights = _rights.copy(rightsUri = uriOpt)
		updateCb(_rights)
	}, "Licence URI")

	val element = div(cls := "row")(
		div(cls := "col-md-6")(statementInput.element),
		div(cls := "col-md-6")(urlInput.element)
	).render
}
