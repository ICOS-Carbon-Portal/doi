package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.DoiAction
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.widgets.DoiTargetWidget
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions

class DoiInfoView(init: DoiInfo, d: DoiRedux.Dispatcher) {

	private[this] var _info = init

	private[this] var _metaWidget: DoiMetaWidget = metaWidget

	private def metaWidget: DoiMetaWidget = new DoiMetaWidget(
		_info.meta,
		newMeta => {
			_info = _info.copy(meta = newMeta)
			d.dispatch(ThunkActions.requestMetaUpdate(newMeta))
		},
		refreshMetaWidget
	)

	private def refreshMetaWidget(): Unit = {
		val oldWidget = _metaWidget.element
		_metaWidget = metaWidget
		oldWidget.parentElement.replaceChild(_metaWidget.element, oldWidget)
	}

	private[this] val targetWidget = if(_info.hasBeenSaved){
		new DoiTargetWidget(
			_info.target,
			init.meta.id,
			newTarget => {
				_info = _info.copy(target = Some(newTarget))
				d.dispatch(ThunkActions.requestTargetUrlUpdate(init.meta.id, newTarget))
			}
		).element
	} else div.render

	val element = div(_metaWidget.element, targetWidget).render
}
