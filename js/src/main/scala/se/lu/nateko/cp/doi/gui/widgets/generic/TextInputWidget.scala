package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.{Event, html}
import scalatags.JsDom.all._

class TextInputWidget(init: String, protected val updateCb: String => Unit, placeHolder: String = "", required: Boolean = false) extends EntityWidget[String] {

	val element: html.Input = input(tpe := "text", cls := "form-control", value := init, placeholder := placeHolder).render

	def validate() = if (required) highlightError(element, if (element.value == "") Some("") else None)

	element.onkeyup = (_: Event) =>  {
		validate()
		updateCb(element.value)
	}

	validate()

}
