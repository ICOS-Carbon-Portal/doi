package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.Event
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.Date

import scala.util.matching.Regex

class CompositeDateWidget(init: Date, protected val updateCb: Date => Unit) extends EntityWidget[Date]{

	private var _date = init

	private val dateRangeRegex: Regex = """(.*)/(.*)""".r

	private def isRange: Boolean = dateRangeRegex.matches(_date.date)

	private val updateDate = (newD: Date) =>
		_date = newD
		updateCb(newD)

	private def getDateElem =
		if(isRange) new DateRangeWidget(_date, updateDate).element
		else new SingleDateWidget(_date, updateDate).element

	private var dateElem = getDateElem

	private def onSelectChange: Event => Unit = e =>
		val selectElem = e.target.asInstanceOf[org.scalajs.dom.html.Select]
		val range = selectElem.value == "range"
		val split = _date.date.split("/")
		val newDate = if (range && split.nonEmpty) split(0) + "/" else if(range) _date.date + "/" else if(split.nonEmpty) split(0) else ""

		_date = Date(newDate, _date.dateType.filter(_.couldBeRange))

		val newDateElem = getDateElem
		dateElem.parentNode.replaceChild(newDateElem, dateElem)
		dateElem = newDateElem
		updateCb(_date)

	private def dateTypeSelect =
		select(cls := "form-select", onchange := onSelectChange)(
			option(value := "single", if(!isRange) selected else ())("Single date"),
			option(value := "range", if(isRange) selected else ())("Date range")
		)

	val element = div(cls := "row align-items-center")(
		div(cls := "col-md-4 mt-1")(
			dateTypeSelect
		),
		div(cls := "col-md")(
			dateElem
		)
	).render

}
