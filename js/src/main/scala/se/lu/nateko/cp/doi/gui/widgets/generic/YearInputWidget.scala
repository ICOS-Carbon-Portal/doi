package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.{Event, html}
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.meta.PublicationYear


class YearInputWidget(init: Option[Int], protected val updateCb: Option[Int] => Unit, required: Boolean = false) extends EntityWidget[Option[Int]] {

	val element: html.Input = input(
		tpe := "number",
		cls := "form-control",
		value := init.map(_.toString).getOrElse(""),
		min := PublicationYear.MinYear.toString,
		max := PublicationYear.MaxYear.toString
	).render

	def validate(): Unit = {
		val text = element.value.trim
		if text.isEmpty then
			highlightError(element, if required then Some("") else None)
		else
			highlightError(element, PublicationYear.error(text.toInt))
	}

	element.oninput = (_: Event) => {
		validate()
		val text = element.value.trim
		if text.isEmpty then
			updateCb(None)
		else
			updateCb(Some(text.toInt))
	}

	validate()

}
