package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.Longitude
import org.scalajs.dom.HTMLElement

class LongitudeWidget (
	init: Longitude,
	protected val updateCb: Longitude => Unit
) extends EntityWidget[Longitude]{

	private[this] var _point = init

	private def validate() = highlightError(longInput.element, _point.error)

	private val longInput: TextInputWidget = longitudeTextWidget(_point.value.toString, textOpt => _point.copy(value = textOpt), "Point Longitude")
  
	private def longitudeTextWidget(init: String, update: String => Longitude, placeHolder: String) =
		new TextInputWidget(
			init,
			str => {
				_point = update(str)
				validate()
				updateCb(_point)
			},
			placeHolder,
			required = true
		)

	val element = div(cls := "row spacyrow")(
		longInput.element,
	).render
}