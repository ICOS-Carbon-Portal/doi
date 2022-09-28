package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.HTMLElement
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.Date
import se.lu.nateko.cp.doi.meta.DateType

class DateRangeWidget(init: Date, protected val updateCb: Date => Unit, values: Array[DateType]) extends EntityWidget[Date]{

	private var _date = init

	private def validate(): Unit =
		highlightError(startDateInput.element, _date.error)
		highlightError(endDateInput.element, _date.error)

	private def splitDate = _date.date.split("/")
	private def getStartDate: String = if(!splitDate.isEmpty) splitDate(0) else ""
	private def getEndDate: String = if(splitDate.length > 1) splitDate(1) else ""

	private val startDateInput: TextInputWidget = new TextInputWidget(getStartDate, s => {
		_date = _date.copy(date = s"$s/$getEndDate")
		validate()
		updateCb(_date)
	}, "YYYY-MM-DD", required = true)

	private val endDateInput: TextInputWidget = new TextInputWidget(getEndDate, e => {
		_date = _date.copy(date = s"$getStartDate/$e")
		validate()
		updateCb(_date)
	}, "YYYY-MM-DD", required = true)

	private val dateTypeInput = new SelectWidget[DateType](
		SelectWidget.selectOptions(Some("Date format"), values),
		Option(_date.dateType),
		dtOpt => {
			val dt = dtOpt.getOrElse(null)
			_date = _date.copy(dateType = dt)
			validate()
			updateCb(_date)
		}
	)

	val element = div(cls := "row")(
		div(cls := "col-md-1")(strong("Start date:")),
		div(cls := "col-md-3")(startDateInput.element),
		div(cls := "col-md-1")(strong("End date:")),
		div(cls := "col-md-3")(endDateInput.element),
		div(cls := "col-md-4")(dateTypeInput.element)
		
	).render
}
