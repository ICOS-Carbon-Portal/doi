package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.{Event, html}
import scalatags.JsDom.all._


class YearInputWidget(init: Option[Int], protected val updateCb: Option[Int] => Unit) extends EntityWidget[Option[Int]] {

	val element: html.Input = input(
		tpe := "number",
		cls := "form-control",
		value := init.map(_.toString).getOrElse(""),
		min := "1900",
		max := "2100"
	).render

	element.oninput = (_: Event) => {
		val text = element.value.trim
		if text.isEmpty then
			highlightError(element, None)
			updateCb(None)
		else
			val year = text.toInt
			if year < 1900 || year > 2100 then
				highlightError(element, Some("Year must be between 1900 and 2100"))
				updateCb(init)
			else
				highlightError(element, None)
				updateCb(Some(year))
	}

}
