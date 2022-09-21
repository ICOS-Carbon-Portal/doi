package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.Latitude
import org.scalajs.dom.HTMLElement

class LatitudeWidget (
	init: Latitude,
	protected val updateCb: Latitude => Unit
) extends EntityWidget[Latitude]{

	private[this] var _point = init

	private def validate() = highlightError(latInput.element, _point.error)

	private val latInput: TextInputWidget = latitudeTextWidget(_point.value.toString, textOpt => _point.copy(value = textOpt), "Point latitude")
  
	private def latitudeTextWidget(init: String, update: String => Latitude, placeHolder: String) =
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
		latInput.element,
	).render
}
