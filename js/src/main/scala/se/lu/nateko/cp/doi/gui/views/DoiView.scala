package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.gui.DoiAction
import org.scalajs.dom.Event
import org.scalajs.dom.console
import se.lu.nateko.cp.doi.gui.SelectDoi
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.DoiWithTitle

class DoiView(doi: Doi, d: DoiRedux.Dispatcher) {

	private[this] var info: Option[DoiInfo] = None
	private[this] var isSelected = false

	private val doiListIcon = span(cls := doiListIconClass).render

	private def doiListIconClass = "glyphicon glyphicon-triangle-" +
		(if(isSelected) "bottom" else "right")

	private val selectDoi: Event => Unit = e => d.dispatch(ThunkActions.selectDoiFetchInfo(doi))
	private val titleSpan = span(cls := "panel-title")().render

	private val panelBody = div(cls := "panel-body").render

	val panelStyle = {
		val prefs = d.getState.prefixes
		if(doi.prefix == prefs.staging) "warning" else "info"
	}
	val element = div(cls := s"panel panel-$panelStyle")(
		div(cls := "panel-heading", onclick := selectDoi, cursor := "pointer")(
			doiListIcon,
			titleSpan
		),
		panelBody
	).render

	def updateContentVisibility(): Unit = {
		val title = info
			.flatMap( _.meta.titles.headOption)
			.map(_.title)
			.orElse(d.getState.dois.collectFirst{case DoiWithTitle(`doi`, title) => title})
			.map(" | " + _)
			.getOrElse("")
		titleSpan.textContent = s" $doi$title"

		val display = if(isSelected && info.isDefined) "block" else "none"
		panelBody.style.display = display
	}

	def setSelected(is: Boolean): Unit = {
		isSelected = is
		doiListIcon.className = doiListIconClass
		updateContentVisibility()
	}

	def supplyInfo(doiInfo: DoiInfo): Unit = if(!info.contains(doiInfo)){
		info = Some(doiInfo)
		updateContentVisibility()
		panelBody.innerHTML = ""
		val infoView = new DoiInfoView(doiInfo, d)
		panelBody.appendChild(infoView.element)
	}
}
