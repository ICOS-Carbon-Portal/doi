package se.lu.nateko.cp.doi.gui.widgets.generic

import org.scalajs.dom.Event
import scalatags.JsDom.all._
import SelectWidget._

case class SelectOption[T](value: Option[T], id: String, label: String)

class SelectWidget[T](
	options: IndexedSeq[SelectOption[T]],
	init: Option[T],
	protected val updateCb: Option[T] => Unit
) extends EntityWidget[Option[T]] {

	val element = select(cls := "form-control")(options.map(optionElem)).render

	element.onchange = (_: Event) => updateCb(options(element.selectedIndex).value)

	element.selectedIndex = options.indexWhere(_.value == init)
}

object SelectWidget{

	def optionElem[T](optInfo: SelectOption[T]) = {
		option(value := optInfo.id)(optInfo.label)
	}

}
