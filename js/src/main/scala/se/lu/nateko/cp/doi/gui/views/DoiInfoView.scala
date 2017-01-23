package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.widgets.DoiTargetWidget
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions

class DoiInfoView(init: DoiInfo, d: DoiRedux.Dispatcher) {

	private[this] val metaWidget = new DoiMetaWidget(
		init.meta,
		newMeta => {
			d.dispatch(ThunkActions.requestMetaUpdate(newMeta))
		}
	)

	private[this] val targetWidget = if(init.hasBeenSaved){
		new DoiTargetWidget(
			init.target,
			init.meta.id,
			newTarget => {
				d.dispatch(ThunkActions.requestTargetUrlUpdate(init.meta.id, newTarget))
			}
		).element
	} else div.render

	val element = div(metaWidget.element, targetWidget).render
}
