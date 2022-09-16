package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.GeoLocationPoint

class GeoLocationPointWidget (
	init: GeoLocationPoint,
	protected val updateCb: GeoLocationPoint => Unit
) extends EntityWidget[GeoLocationPoint]{

	private[this] var _point = init

	private val pointLongitudeInput = geoLocationPointTextWidget(_point.pointLongitude.fold("")(_.toString),  textOpt => _point.copy(pointLongitude  = textOpt), "Point longitude")
	private val pointLatitudeInput  = geoLocationPointTextWidget(_point.pointLatitude.fold("")(_.toString), textOpt => _point.copy(pointLatitude = textOpt), "Point latitude")

	private def geoLocationPointTextWidget(init: String, update: Option[Double] => GeoLocationPoint, placeHolder: String) =
		new TextInputWidget(
			init,
			str => {
				val pointOpt = if(str.isEmpty) None else Some(str.toDouble)
				_point = update(pointOpt)
				updateCb(_point)
			},
			placeHolder,
			required = true
		)

	val element = div(cls := "row spacyrow")(
		div(cls := "col-md-2")(strong("Point longitude")),
		div(cls := "col-md-3")(pointLongitudeInput.element),
		div(cls := "col-md-2")(strong("Point latitude")),
		div(cls := "col-md-3")(pointLatitudeInput.element),
	).render
}
