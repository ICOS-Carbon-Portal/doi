package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import scalatags.JsDom.all._

class TextAreaWidget(init: String, protected val updateCb: String => Unit)(mods: Modifier*) extends EntityWidget[String] {

	val element = textarea(cls := "form-control", value := init)(mods).render

	element.onkeyup = (_: Event) => updateCb(element.value)

}
