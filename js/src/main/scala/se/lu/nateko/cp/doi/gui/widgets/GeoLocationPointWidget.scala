package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.GeoLocationPoint
import org.scalajs.dom.HTMLElement
import se.lu.nateko.cp.doi.meta.Latitude
import se.lu.nateko.cp.doi.meta.Longitude

class GeoLocationPointWidget (
	init: GeoLocationPoint,
	protected val updateCb: GeoLocationPoint => Unit
) extends EntityWidget[GeoLocationPoint]{

	private[this] var _point = init

	private val pointLongitudeInput = LongitudeWidget(init.pointLongitude.getOrElse(Longitude("")), str => { // not str
			_point = _point.copy(pointLongitude = Option(str))
			updateCb(_point)
		})

	private val pointLatitudeInput = LatitudeWidget(init.pointLatitude.getOrElse(Latitude("")), str => {
			_point = _point.copy(pointLatitude = Option(str))
			updateCb(_point)
		})

	val element = div(cls := "row spacyrow")(
		div(cls := "col-md-2")(strong("Point longitude")),
		div(cls := "col-md-3")(pointLongitudeInput.element),
		div(cls := "col-md-2")(strong("Point latitude")),
		div(cls := "col-md-3")(pointLatitudeInput.element),
	).render
}
