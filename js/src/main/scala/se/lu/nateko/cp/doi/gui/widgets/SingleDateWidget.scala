package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.HTMLElement
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.Date
import se.lu.nateko.cp.doi.meta.DateType

class SingleDateWidget(init: Date, protected val updateCb: Date => Unit) extends EntityWidget[Date] {

	private var _date = init

	private def validate(): Unit = highlightError(dateInput.element, _date.error)

	private val dateInput = new TextInputWidget(_date.date, newDate => {
			_date = _date.copy(date = newDate)
			validate()
			updateCb(_date)
	}, "YYYY-MM-DD", required = true)

	private val dateTypeInput = new SelectWidget[DateType](
		SelectWidget.selectOptions(Some("Date format"), DateType.values),
		_date.dateType,
		dtOpt => {
			val dt = dtOpt
			_date = _date.copy(dateType = dt)
			validate()
			updateCb(_date)
		}
	)

	val element = div(cls := "row")(
			div(cls := "col-md-8")(dateInput.element),
			div(cls := "col-md-4")(dateTypeInput.element)
		).render

	validate()
	
}
