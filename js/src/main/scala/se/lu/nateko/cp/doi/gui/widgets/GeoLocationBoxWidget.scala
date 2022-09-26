package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta._

class GeoLocationBoxWidget (
	init: GeoLocationBox,
	protected val updateCb: GeoLocationBox => Unit
) extends EntityWidget[GeoLocationBox]{

	private[this] var _geoLocationBox = init

	private val northBoundLatitudeInput = LatLonWidget(init.northBoundLatitude, lat => {
		_geoLocationBox = _geoLocationBox.copy(northBoundLatitude = lat)
		updateCb(_geoLocationBox)
	}, latError, Latitude.apply, "North bound latitude")

	private val southBoundLatitudeInput = LatLonWidget(init.southBoundLatitude, lat => {
		_geoLocationBox = _geoLocationBox.copy(southBoundLatitude = lat)
		updateCb(_geoLocationBox)
	}, latError, Latitude.apply, "South bound latitude")

	private val eastBoundLongitudeInput = LatLonWidget(init.eastBoundLongitude, lon => {
		_geoLocationBox = _geoLocationBox.copy(eastBoundLongitude = lon)
		updateCb(_geoLocationBox)
	}, lonError, Longitude.apply, "East bound longitude")

	private val westBoundLongitudeInput = LatLonWidget(init.westBoundLongitude, lon => {
		_geoLocationBox = _geoLocationBox.copy(westBoundLongitude = lon)
		updateCb(_geoLocationBox)
	}, lonError, Longitude.apply, "West bound longitude")

	val element = div(
		div(cls := "row")(
			div(cls := "col-md-2")(strong("East bound longitude "), small("(WGS-84)")),
			div(cls := "col-md-3")(eastBoundLongitudeInput.element),
			div(cls := "col-md-2")(strong("North bound latitude "), small("(WGS-84)")),
			div(cls := "col-md-3")(northBoundLatitudeInput.element)
		)(paddingBottom := 15),
		div(cls := "row")(
			div(cls := "col-md-2")(strong("South bound latitude "), small("(WGS-84)")),
			div(cls := "col-md-3")(southBoundLatitudeInput.element),
			div(cls := "col-md-2")(strong("West bound longitude "), small("(WGS-84)")),
			div(cls := "col-md-3")(westBoundLongitudeInput.element),
		)
	).render
}
