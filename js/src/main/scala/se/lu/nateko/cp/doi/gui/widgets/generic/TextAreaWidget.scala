package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.{Event, html}
import scalatags.JsDom.all._

class TextAreaWidget(init: String, protected val updateCb: String => Unit)(mods: Modifier*) extends EntityWidget[String] {

	val element: html.TextArea = textarea(cls := "form-control")(mods)(init).render

	element.onkeyup = (_: Event) => updateCb(element.value)

}
