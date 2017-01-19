package se.lu.nateko.cp.doi.gui

import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

import DoiRedux.ThunkAction
import DoiRedux.Action
import DoiRedux.Dispatcher
import scala.util.Success
import scala.util.Failure
import se.lu.nateko.cp.doi.Doi

import DoiStateUpgrades._
import se.lu.nateko.cp.doi.DoiMeta

object ThunkActions {

	val DoiListRefreshRequest: ThunkAction = implicit d => {
		dispatchFut(Backend.getDoiList.map(FreshDoiList(_)))
	}

	private def fetchInfo(doi: Doi): ThunkAction = implicit d => {
		if(d.getState.isSelected(doi)){
			dispatchFut(Backend.getInfo(doi).map(GotDoiInfo(_)))
		}
	}

	def selectDoiFetchInfo(doi: Doi): ThunkAction = implicit d => {
		d.dispatch(SelectDoi(doi))
		d.dispatch(fetchInfo(doi))
	}

	private def writeMeta(meta: DoiMeta): ThunkAction = implicit d => {
		if(d.getState.isUpdatingMeta(meta.id)){
			dispatchFut(Backend.updateMeta(meta).map(_ => MetaUpdated(meta)))
		}
	}

	def requestMetaUpdate(meta: DoiMeta): ThunkAction = implicit d => {
		d.dispatch(MetaUpdateRequest(meta))
		d.dispatch(writeMeta(meta))
	}

	private def writeTargetUrl(doi: Doi, url: String): ThunkAction = implicit d => {
		if(d.getState.isUpdatingUrl(doi)){
			dispatchFut(Backend.updateUrl(doi, url).map(_ => TargetUrlUpdated(doi, url)))
		}
	}

	def requestTargetUrlUpdate(doi: Doi, url: String): ThunkAction = implicit d => {
		d.dispatch(TargetUrlUpdateRequest(doi, url))
		d.dispatch(writeTargetUrl(doi, url))
	}

	private def dispatchFut[A <: Action](result: Future[A])(implicit d: Dispatcher): Unit = {
		result.onComplete{
			case Success(a) => d.dispatch(a)
			case Failure(err) => d.dispatch(ReportError(err.getMessage))
		}
	}
}