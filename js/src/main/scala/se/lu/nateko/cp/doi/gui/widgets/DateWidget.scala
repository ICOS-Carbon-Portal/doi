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
	private val rangeTypeValues = DateType.values.filter(_.couldBeRange)

	private def isRange: Boolean = _date.date match
		case dateRangeRegex(startDate, endDate) => true
		case _ => false

	
	private def getRangeType = if (rangeTypeValues.contains(_date.dateType)) _date.dateType else null

	private def getDateElem =
		if(isRange) new DateRangeWidget(_date, newPn => {
				_date = newPn
				updateCb(_date)
			}, rangeTypeValues).element 
		else new SingleDateWidget(_date, newPn => {
				_date = newPn
				updateCb(_date)
			}, DateType.values).element

	private var dateElem = getDateElem

	private def checkedModifier(range: Boolean) = if(range == isRange) Seq(checked := true) else Nil

	private def changeNameType(range: Boolean): Event => Unit = e => if(isRange != range){
		_date = if(range) Date(s"${_date.date}/", getRangeType) else Date(s"${_date.date.split("/")(0)}", _date.dateType)
		val newNameElem = getDateElem
		dateElem.parentNode.replaceChild(newNameElem, dateElem)
		dateElem = newNameElem
		updateCb(_date)
	}

	private def dateTypeOption(range: Boolean) =
		input(tpe := "radio", name := "nameType", onchange := changeNameType(range))(checkedModifier(range))

	val element = Bootstrap.basicCard(
		Bootstrap.propValueRow(strong("Date format"))(
			form(dateTypeOption(false), " Single date", br, dateTypeOption(true), " Date range")
		)(paddingBottom := 15),
		div(cls := "row")(dateElem)
	).render

}
