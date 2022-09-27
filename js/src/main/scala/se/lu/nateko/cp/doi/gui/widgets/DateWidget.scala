package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.Date
import scalatags.JsDom.all._
import org.scalajs.dom.html.Input
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectOption
import se.lu.nateko.cp.doi.meta.DateType
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget

class DateWidget(init: Date, protected val updateCb: Date => Unit) extends EntityWidget[Date]{

	private[this] var _date = init

	private[this] val dateInput: Input = new TextInputWidget(init.date, newDate => {
		_date = _date.copy(date = newDate)
		validate()
		updateCb(_date)
	}, "YYYY-MM-DD", required = true).element

	private[this] def validate(): Unit = highlightError(dateInput, _date.error)

	private[this] val dateTypeInput = new SelectWidget[DateType](
		SelectWidget.selectOptions(Some("Date type"), DateType.values),
		Option(init.dateType),
		dtOpt => {
			val dt = dtOpt.getOrElse(null)
			_date = _date.copy(dateType = dt)
			validate()
			updateCb(_date)
		}
	)

	val element = div(cls := "row")(
		div(cls := "col-md-6")(dateInput),
		div(cls := "col-md-3")(dateTypeInput.element)
	).render

	validate()
}
