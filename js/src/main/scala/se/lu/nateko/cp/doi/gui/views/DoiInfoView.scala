package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.DoiAction
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.gui.MetaUpdateRequest
import se.lu.nateko.cp.doi.gui.TargetUrlUpdateRequest
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.widgets.TargetUrlWidget

class DoiInfoView(init: DoiInfo, dispatch: DoiAction => Unit) {

	private[this] var _info = init

	private[this] var _metaWidget: DoiMetaWidget = metaWidget

	private def metaWidget: DoiMetaWidget = new DoiMetaWidget(
		_info.meta,
		newMeta => {
			_info = _info.copy(meta = newMeta)
			dispatch(MetaUpdateRequest(newMeta))
		},
		refreshMetaWidget
	)

	private def refreshMetaWidget(): Unit = {
		val oldWidget = _metaWidget.element
		_metaWidget = metaWidget
		oldWidget.parentElement.replaceChild(_metaWidget.element, oldWidget)
	}

	private[this] val urlWidget = new TargetUrlWidget(
		_info.target,
		newTarget => {
			_info = _info.copy(target = Some(newTarget))
			dispatch(TargetUrlUpdateRequest(_info.meta.id, newTarget))
		}
	)

	def onUrlHasUpdated(): Unit = {
		urlWidget.refreshUrl(_info.target.getOrElse(""))
	}

	def onMetaHasUpdated(): Unit = refreshMetaWidget()

	val doiUrl = "http://doi.org/" + init.meta.id

	val element = div(
		_metaWidget.element,
		Bootstrap.defaultPanel("DOI Target")(
			Bootstrap.basicPanel(
				span(strong("Test the DOI: ")),
				a(href := doiUrl, target := "_blank")(doiUrl)
			),
			urlWidget.element
		)
	).render
}