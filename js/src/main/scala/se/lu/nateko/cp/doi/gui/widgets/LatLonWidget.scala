package se.lu.nateko.cp.doi.gui.widgets

import org.scalajs.dom.HTMLElement
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta._

class LatLonWidget[T <: Latitude | Longitude] (
	init: Option[T],
	protected val updateCb: Option[T] => Unit,
	error: T => Option[String],
	factory: Double => T,
	placeHolder: String
) extends EntityWidget[Option[T]]{

	private[this] var _point = init

	private def validate() = highlightError(longInput.element, _point.fold(Some(s"Missing ${placeHolder.toLowerCase}"))(error))

	private val longInput: TextInputWidget = latitudeTextWidget(_point.fold("")(_.toString), placeHolder)
  
	private def latitudeTextWidget(init: String, placeHolder: String) =
		new TextInputWidget(
			init,
			str => {
				_point = str.toDoubleOption.map(factory)
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
