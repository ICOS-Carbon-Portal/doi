package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.HTMLElement
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.Date
import se.lu.nateko.cp.doi.meta.DateType

class SingleDateWidget(init: Date, protected val updateCb: Date => Unit) extends DateWidget(init) {

	private def validate(): Unit = {
		highlightDateError(dateInput.element)
		highlightDateTypeError(dateTypeInput.element)
	}

	private val dateInput = new TextInputWidget(_date.date, newDate => updateDate(newDate, validate), "YYYY-MM-DD", required = true)
	private val dateTypeInput = getDateTypeInput(_date.dateType, DateType.values, validate)

	val element = div(cls := "row")(
			div(cls := "col-md-8")(dateInput.element),
			div(cls := "col-md-4")(dateTypeInput.element)
		).render

	validate()

}
