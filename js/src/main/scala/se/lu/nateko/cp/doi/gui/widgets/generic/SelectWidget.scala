package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.all._
import SelectWidget._


case class SelectOption[T](value: Option[T], id: String, label: String)

class SelectWidget[T](
	options: IndexedSeq[SelectOption[T]],
	init: Option[T],
	protected val updateCb: Option[T] => Unit
) extends EntityWidget[Option[T]] {

	val element: html.Select = select(cls := "form-control")(options.map(optionElem)).render

	element.onchange = (_: Event) => updateCb(options(element.selectedIndex).value)

	element.selectedIndex = options.indexWhere(_.value == init)
}

object SelectWidget{

	def optionElem[T](optInfo: SelectOption[T]) = {
		option(value := optInfo.id)(optInfo.label)
	}

	def selectOptions[T](noValueLabelOpt: Option[String], values: Array[T]): IndexedSeq[SelectOption[T]] =
		selectOptions(noValueLabelOpt)(values*)

	def selectOptions[T](noValueLabelOpt: Option[String])(selectVals: T*): IndexedSeq[SelectOption[T]] = {
		noValueLabelOpt.map(SelectOption[T](None, "", _)).toIndexedSeq ++
		selectVals.map(v =>
			SelectOption(Some(v), v.toString, v.toString)
		)
	}

}
