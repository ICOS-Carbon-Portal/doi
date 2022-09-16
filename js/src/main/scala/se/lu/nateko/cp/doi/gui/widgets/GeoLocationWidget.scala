package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.GeoLocationWidget.GeoLocationBoxInput
import se.lu.nateko.cp.doi.gui.widgets.GeoLocationWidget.GeoLocationPointInput
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.MultiEntitiesEditWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.MultiStringsWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget.apply
import se.lu.nateko.cp.doi.meta.GeoLocation
import se.lu.nateko.cp.doi.meta.GeoLocationBox
import se.lu.nateko.cp.doi.meta.GeoLocationPoint

class GeoLocationWidget(init: GeoLocation, protected val updateCb: GeoLocation => Unit) extends EntityWidget[GeoLocation]{

	private[this] var _geoLocation = init

	private[this] val placeInput = new TextInputWidget(init.geoLocationPlace.getOrElse(""), place => {
		val placeOpt = if(place.isEmpty) None else Some(place)
		_geoLocation = _geoLocation.copy(geoLocationPlace = placeOpt)
		updateCb(_geoLocation)
	}, "Geolocation place")

	private[this] val geoLocationPointInput = new GeoLocationPointInput(_geoLocation.geoLocationPoint.fold(Seq())(elem => Seq(elem)), points => {
		val pointOpt = if(points.isEmpty) None else Some(points(0))
		_geoLocation = _geoLocation.copy(geoLocationPoint = pointOpt)
		updateCb(_geoLocation)
	})

	private[this] val geoLocationBoxInput = new GeoLocationBoxInput(_geoLocation.geoLocationBox.fold(Seq())(elem => Seq(elem)), boxes => {
		val boxOpt = if(boxes.isEmpty) None else Some(boxes(0))
		_geoLocation = _geoLocation.copy(geoLocationBox = boxOpt)
		updateCb(_geoLocation)
	})

	val element = div(
		div(cls := "row")(div(cls := "col-md-2")(strong("Geolocation place")),
		div(cls := "col-md-6")(placeInput.element)(paddingBottom := 15)),
		geoLocationPointInput.element,
		geoLocationBoxInput.element
	).render
}

object GeoLocationWidget{

	class GeoLocationPointInput(init: Seq[GeoLocationPoint], updateCb: collection.Seq[GeoLocationPoint] => Unit) extends
		MultiEntitiesEditWidget[GeoLocationPoint, GeoLocationPointWidget](init, updateCb)("Geolocation point", maxAmount = 1){

		protected def makeWidget(value: GeoLocationPoint, updateCb: GeoLocationPoint => Unit) =
			new GeoLocationPointWidget(value, updateCb)

		protected def defaultValue = GeoLocationPoint(None, None)
	}

	class GeoLocationBoxInput(init: Seq[GeoLocationBox], updateCb: collection.Seq[GeoLocationBox] => Unit) extends
		MultiEntitiesEditWidget[GeoLocationBox, GeoLocationBoxWidget](init, updateCb)("Geolocation box", maxAmount = 1){

		protected def makeWidget(value: GeoLocationBox, updateCb: GeoLocationBox => Unit) =
			new GeoLocationBoxWidget(value, updateCb)

		protected def defaultValue = GeoLocationBox(None, None, None, None)
	}

}
