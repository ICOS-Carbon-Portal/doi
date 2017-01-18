package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Name
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta.PersonalName
import se.lu.nateko.cp.doi.meta.GenericName
import org.scalajs.dom.Event
import org.scalajs.dom.Element
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.views.Bootstrap

class NameWidget(init: Name, protected val updateCb: Name => Unit) extends EntityWidget[Name]{

	private[this] var _name = init

	private def isPersonal: Boolean = _name match{
		case _: PersonalName => true
		case _: GenericName => false
	}

	private def getNameElem: Element = _name match{
		case pn: PersonalName => new PersonalNameWidget(pn, newPn => {
				_name = newPn
				updateCb(_name)
			}).element
		case gn: GenericName => new TextInputWidget(gn.name, newGn => {
				_name = GenericName(newGn)
				updateCb(_name)
			}).element
	}

	private var nameElem = getNameElem

	private def checkedModifier(personal: Boolean) = if(personal == isPersonal) Seq(checked := true) else Nil

	private def changeNameType(personal: Boolean): Event => Unit = e => if(isPersonal != personal){
		_name = if(personal) PersonalName("", "") else GenericName("")
		val newNameElem = getNameElem
		nameElem.parentNode.replaceChild(newNameElem, nameElem)
		nameElem = newNameElem
		updateCb(_name)
	}

	private def nameTypeOption(personal: Boolean) =
		input(tpe := "radio", name := "nameType", onchange := changeNameType(personal))(checkedModifier(personal))

	val element = Bootstrap.basicPanel(
		Bootstrap.propValueRow(strong("Name type"))(
			form(nameTypeOption(true), " Personal name", br, nameTypeOption(false), " Organizational name")
		)(paddingBottom := 15),
		Bootstrap.propValueRow(strong("Name"))(nameElem)
	).render
}
