package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.HTMLSelectElement
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget
import se.lu.nateko.cp.doi.meta.Date
import se.lu.nateko.cp.doi.meta.DateType

trait DateWidget(init: Date) extends EntityWidget[Date]{

	protected var _date = init

	private val dateRegex = """\d\d\d\d-\d\d-\d\d""".r

	def updateDate(newDate: String, validate: () => Unit) = {
		_date = _date.copy(date = newDate)
		validate()
		updateCb(_date)
	}

	def highlightDateTypeError(input: HTMLSelectElement) = {
		val dateTypeErr =
			if(input.value.nonEmpty) None
			else Some("Date type must be specified for every date")
		highlightError(input, dateTypeErr)
	}

	def highlightDateError(input: HTMLInputElement): Unit =
		val dateErr =
			if(dateRegex.matches(input.value)) None
			else Some("Bad date format, use YYYY-MM-dd")
		highlightError(input, dateErr)

	def getDateTypeInput(init: Option[DateType], values: Array[DateType], validate: () => Unit) = new SelectWidget[DateType](
		SelectWidget.selectOptions(Some("Select type"), values),
		init,
		dtOpt => {
			_date = _date.copy(dateType = dtOpt)
			validate()
			updateCb(_date)
		}
	)
}
