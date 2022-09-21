package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.GeoLocationBox
import org.scalajs.dom.HTMLElement
import se.lu.nateko.cp.doi.meta.Latitude.apply
import se.lu.nateko.cp.doi.meta.Latitude
import se.lu.nateko.cp.doi.meta.Longitude

class GeoLocationBoxWidget (
	init: GeoLocationBox,
	protected val updateCb: GeoLocationBox => Unit
) extends EntityWidget[GeoLocationBox]{

	private[this] var _geoLocationBox = init

	private val northBoundLatitudeInput = LatitudeWidget(init.northBoundLatitude.getOrElse(Latitude("")), str => { // not str
				_geoLocationBox = _geoLocationBox.copy(northBoundLatitude = Option(str))
				updateCb(_geoLocationBox)
			})

	private val southBoundLatitudeInput = LatitudeWidget(init.southBoundLatitude.getOrElse(Latitude("")), str => {
			_geoLocationBox = _geoLocationBox.copy(southBoundLatitude = Option(str))
			updateCb(_geoLocationBox)
		})

	private val eastBoundLongitudeInput = LongitudeWidget(init.eastBoundLongitude.getOrElse(Longitude("")), str => {
		_geoLocationBox = _geoLocationBox.copy(eastBoundLongitude = Option(str))
		updateCb(_geoLocationBox)
	})

	private val westBoundLongitudeInput = LongitudeWidget(init.westBoundLongitude.getOrElse(Longitude("")), str => {
		_geoLocationBox = _geoLocationBox.copy(westBoundLongitude = Option(str))
		updateCb(_geoLocationBox)
	})

	val element = div(div(cls := "row")(
			div(cls := "col-md-2")(strong("East bound longitude")),
			div(cls := "col-md-3")(eastBoundLongitudeInput.element),
			div(cls := "col-md-2")(strong("North bound latitude")),
			div(cls := "col-md-3")(northBoundLatitudeInput.element))(paddingBottom := 15),
			div(cls := "row")(
			div(cls := "col-md-2")(strong("South bound latitude")),
			div(cls := "col-md-3")(southBoundLatitudeInput.element),
			div(cls := "col-md-2")(strong("West bound longitude")),
			div(cls := "col-md-3")(westBoundLongitudeInput.element),
	)).render
}
