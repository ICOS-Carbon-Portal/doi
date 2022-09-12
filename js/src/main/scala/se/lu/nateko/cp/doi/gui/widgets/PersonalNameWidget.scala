package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.PersonalName
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget

class PersonalNameWidget(init: PersonalName, protected val updateCb: PersonalName => Unit) extends EntityWidget[PersonalName]{

	private[this] var _name = init

	private[this] val fnameInput = new TextInputWidget(init.givenName, gn => {
		_name = _name.copy(givenName = gn)
		updateCb(_name)
	}, required = true)

	private[this] val lnameInput = new TextInputWidget(init.familyName, fn => {
		_name = _name.copy(familyName = fn)
		updateCb(_name)
	}, required = true)

	val element = div(cls := "row")(
		div(cls := "col-md-2")(strong("Given name:")),
		div(cls := "col-md-4")(fnameInput.element),
		div(cls := "col-md-2")(strong("Family name:")),
		div(cls := "col-md-4")(lnameInput.element)
	).render
}