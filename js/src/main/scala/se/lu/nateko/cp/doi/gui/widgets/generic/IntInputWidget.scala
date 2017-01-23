package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import scalatags.JsDom.all._


class IntInputWidget(init: Int, protected val updateCb: Int => Unit) extends EntityWidget[Int] {

	val element = input(tpe := "text", cls := "form-control", value := init.toString).render

	element.onkeyup = (_: Event) => {
		try{
			val newVal = element.value.toInt
			highlightError(element, None)
			updateCb(newVal)
		}catch{
			case _: Throwable =>
				highlightError(element, Some("Not a valid integer"))
				updateCb(init)
		}
	}

}
