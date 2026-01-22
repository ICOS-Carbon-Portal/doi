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

	private def isGeneric = !isPersonal

	private def getNameElem: Element = _name match{
		case pn: PersonalName => new PersonalNameWidget(pn, newPn => {
				_name = newPn
				updateCb(_name)
			}).element
		case gn: GenericName => new TextInputWidget(gn.name, newGn => {
				_name = GenericName(newGn)
				updateCb(_name)
			}, required = true).element
	}

	private var nameElem = getNameElem

	private def changeNameType(): Event => Unit = e => {
		val selectElem = e.target.asInstanceOf[org.scalajs.dom.html.Select]
		val personal = selectElem.value == "personal"
		if(isPersonal != personal){
			_name = if(personal) PersonalName("", "") else GenericName("")
			val newNameElem = getNameElem
			nameElem.parentNode.replaceChild(newNameElem, nameElem)
			nameElem = newNameElem
			updateCb(_name)
		}
	}

	val element =
		div(
			cls := "row",
			div(
				cls := "col-md-auto",
				select(
					cls := "form-select",
					onchange := changeNameType()
				)(
					if(isPersonal) option(value := "personal", selected := true)("Person") else option(value := "personal")("Person"),
					if(isGeneric) option(value := "organization", selected := true)("Organization") else option(value := "organization")("Organization")
				)
			),
			div(
				cls := "col",
				nameElem
			)
		)
	.render
}
