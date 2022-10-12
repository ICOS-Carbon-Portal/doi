package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta._

class GeoLocationPointWidget (
	init: GeoLocationPoint,
	protected val updateCb: GeoLocationPoint => Unit
) extends EntityWidget[GeoLocationPoint]{

	private[this] var _point = init

	private val pointLongitudeInput = LatLonWidget(init.pointLongitude, lon => {
			_point = _point.copy(pointLongitude = lon)
			updateCb(_point)
	}, lonError, Longitude.apply, "Point longitude")

	private val pointLatitudeInput = LatLonWidget(init.pointLatitude, lat => {
			_point = _point.copy(pointLatitude = lat)
			updateCb(_point)
	}, latError, Latitude.apply, "Point latitude")

	val element = div(cls := "row spacyrow")(
		div(cls := "col-md-2")(strong("Point latitude "), small("(WGS-84)")),
		div(cls := "col-md-3")(pointLatitudeInput.element),
		div(cls := "col-md-2")(strong("Point longitude "), small("(WGS-84)")),
		div(cls := "col-md-3")(pointLongitudeInput.element),
	).render
}
