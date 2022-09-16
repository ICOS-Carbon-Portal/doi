package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.meta.GeoLocationBox

class GeoLocationBoxWidget (
	init: GeoLocationBox,
	protected val updateCb: GeoLocationBox => Unit
) extends EntityWidget[GeoLocationBox]{

	private[this] var _geoLocationBox = init

	private val eastBoundLongitudeInput = geoLocationBoxTextWidget(_geoLocationBox.eastBoundLongitude.fold("")(_.toString),  textOpt => _geoLocationBox.copy(eastBoundLongitude  = textOpt), "East bound longitude")
	private val northBoundLatitudeInput = geoLocationBoxTextWidget(_geoLocationBox.northBoundLatitude.fold("")(_.toString), textOpt => _geoLocationBox.copy(northBoundLatitude = textOpt), "North bound latitude")
	private val southBoundLatitudeInput = geoLocationBoxTextWidget(_geoLocationBox.southBoundLatitude.fold("")(_.toString),    textOpt => _geoLocationBox.copy(southBoundLatitude    = textOpt), "South bound latitude")
	private val westBoundLongitudeInput = geoLocationBoxTextWidget(_geoLocationBox.westBoundLongitude.fold("")(_.toString),    textOpt => _geoLocationBox.copy(westBoundLongitude    = textOpt), "West bound longitude")

	private def geoLocationBoxTextWidget(init: String, update: Option[Double] => GeoLocationBox, placeHolder: String) =
		new TextInputWidget(
			init,
			str => {
				val boxOpt = if(str.isEmpty) None else Some(str.toDouble)
				_geoLocationBox = update(boxOpt)
				updateCb(_geoLocationBox)
			},
			placeHolder,
			required = true
		)

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
