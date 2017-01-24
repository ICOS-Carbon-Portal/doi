package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import scalatags.JsDom.all._

class TextInputWidget(init: String, protected val updateCb: String => Unit, placeHolder: String = "") extends EntityWidget[String] {

	val element = input(tpe := "text", cls := "form-control", value := init, placeholder := placeHolder).render

	element.onkeyup = (_: Event) => updateCb(element.value)

}
