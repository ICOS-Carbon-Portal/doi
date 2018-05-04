package se.lu.nateko.cp.doi.gui.views

import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.DoiInfo
import se.lu.nateko.cp.doi.gui.widgets.DoiMetaWidget
import se.lu.nateko.cp.doi.gui.widgets.DoiTargetWidget
import se.lu.nateko.cp.doi.gui.DoiRedux
import se.lu.nateko.cp.doi.gui.ThunkActions
import se.lu.nateko.cp.doi.gui.DoiCloneRequest
import se.lu.nateko.cp.doi.gui.widgets.PublishToProductionWidget

class DoiInfoView(init: DoiInfo, d: DoiRedux.Dispatcher) {

	private[this] val metaWidget = new DoiMetaWidget(
		init.meta,
		newMeta => {
			d.dispatch(ThunkActions.requestMetaUpdate(newMeta, None))
		},
		meta => d.dispatch(DoiCloneRequest(meta))
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

	private[this] val publishWidget = if(
			init.meta.id.prefix == d.getState.prefixes.staging &&
			init.hasBeenSaved
		)
			new PublishToProductionWidget(
				d.getState.prefixes.production,
				init.meta.id,
				newDoi => {
					val urlActionOpt = init.target.map{url =>
						ThunkActions.requestTargetUrlUpdate(newDoi, url)
					}
					d.dispatch(ThunkActions.requestMetaUpdate(init.meta.copy(id = newDoi), urlActionOpt))
				}
			).element
		else div.render

	val element = div(metaWidget.element, targetWidget, publishWidget).render
}
