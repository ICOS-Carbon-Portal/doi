package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.all._
import SelectWidget._


case class SelectOption[T](value: Option[T], id: String, label: String)

class SelectWidget[T](
	options: IndexedSeq[SelectOption[T]],
	init: Option[T],
	protected val updateCb: Option[T] => Unit,
	required: Boolean = false
) extends EntityWidget[Option[T]] {

	val element: html.Select = select(cls := "form-select flex-grow-0", style := "flex-basis: content")(options.map(optionElem)).render

	def validate(): Unit = {
		val selectedValue = options(element.selectedIndex).value
		highlightError(element, if required && selectedValue.isEmpty then Some("") else None)
	}

	element.onchange = (_: Event) => {
		validate()
		updateCb(options(element.selectedIndex).value)
	}

	element.selectedIndex = options.indexWhere(_.value == init)

	validate()
}

object SelectWidget{

	def optionElem[T](optInfo: SelectOption[T]) = {
		option(value := optInfo.id, if optInfo.value.isEmpty then Seq(disabled, hidden) else Seq.empty[Modifier])(optInfo.label)
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
