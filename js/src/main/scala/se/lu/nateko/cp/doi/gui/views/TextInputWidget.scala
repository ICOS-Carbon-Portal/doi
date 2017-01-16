package se.lu.nateko.cp.doi.gui.views

import org.scalajs.dom.Event
import scalatags.JsDom.all._


class TextInputWidget(init: String, protected val updateCb: String => Unit) extends EntityWidget[String] {

	val element = input(tpe := "text", cls := "form-control", value := init).render

	element.onkeyup = (_: Event) => updateCb(element.value)

}
