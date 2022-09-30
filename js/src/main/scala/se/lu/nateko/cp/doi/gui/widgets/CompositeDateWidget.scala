package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.Event
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.Date
import se.lu.nateko.cp.doi.meta.DateType

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

	private def changeDateType(range: Boolean): Event => Unit = e =>
		val split = _date.date.split("/")
		val newDate = if (range && split.nonEmpty) split(0) + "/" else if(range) _date.date + "/" else if(split.nonEmpty) split(0) else ""

		_date = Date(newDate, _date.dateType.filter(_.couldBeRange))

		val newDateElem = getDateElem
		dateElem.parentNode.replaceChild(newDateElem, dateElem)
		dateElem = newDateElem
		updateCb(_date)

	private def dateTypeOption(range: Boolean) =
		val checkedModifier = if(range == isRange) Seq(checked := true) else Nil
		input(tpe := "radio", name := "dateType", onchange := changeDateType(range))(checkedModifier)

	val element = Bootstrap.basicCard(
		Bootstrap.propValueRow(strong("Date format"))(
			form(dateTypeOption(false), " Single date", br, dateTypeOption(true), " Date range")
		)(paddingBottom := 15),
		div(cls := "row")(dateElem)
	).render

}
