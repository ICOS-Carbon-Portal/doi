package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.HTMLElement
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.DateInputWidget
import se.lu.nateko.cp.doi.meta.Date
import se.lu.nateko.cp.doi.meta.DateType

class DateRangeWidget(init: Date, protected val updateCb: Date => Unit) extends DateWidget(init){

	private def validate(): Unit = {
		highlightDateError(startDateInput.element)
		highlightDateError(endDateInput.element)
		highlightDateTypeError(dateTypeInput.element)
	}

	private def splitDate = _date.date.split("/")
	private def getStartDate: String = if(!splitDate.isEmpty) splitDate(0) else ""
	private def getEndDate: String = if(splitDate.length > 1) splitDate(1) else ""

	private val startDateInput = new DateInputWidget(getStartDate, s => updateDate(s"$s/$getEndDate", validate), "YYYY-MM-DD", required = true)
	private val endDateInput = new DateInputWidget(getEndDate, e => updateDate(s"$getStartDate/$e", validate), "YYYY-MM-DD", required = true)
	private val dateTypeInput = getDateTypeInput(_date.dateType, DateType.values.filter(_.couldBeRange), validate)

	val element = div(cls := "input-group")(
		dateTypeInput.element,
		startDateInput.element,
		endDateInput.element
	).render

	validate()

}
