package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.Event
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.views.Bootstrap
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.Date
import se.lu.nateko.cp.doi.meta.DateType

import scala.util.matching.Regex

class DateWidget(init: Date, protected val updateCb: Date => Unit) extends EntityWidget[Date]{

	private var _date = init

	private val dateRangeRegex: Regex = """(\d{4}-\d\d-\d\d)?/(\d{4}-\d\d-\d\d)?""".r

	private def isRange: Boolean = dateRangeRegex.matches(_date.date)

	private val updateDate = (newD: Date) =>
		_date = newD
		updateCb(newD)

	private def getDateElem =
		if(isRange) new DateRangeWidget(_date, updateDate).element 
		else new SingleDateWidget(_date, updateDate).element

	private var dateElem = getDateElem

	private def changeDateType(range: Boolean): Event => Unit = e => if(isRange != range){
		_date = Date(
					if(range) _date.date + "/" else _date.date.split("/")(0),
					_date.dateType.flatMap(t => Option.when(t.couldBeRange)(t))
					)
		val newDateElem = getDateElem
		dateElem.parentNode.replaceChild(newDateElem, dateElem)
		dateElem = newDateElem
		updateCb(_date)
	}

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
