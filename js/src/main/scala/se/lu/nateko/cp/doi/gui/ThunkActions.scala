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

	val FetchPrefixInfo: ThunkAction = implicit d => {
		dispatchFut(Backend.getPrefixInfo.map(GotPrefixInfo(_)))
	}

	val DoiListRefreshRequest: ThunkAction = implicit d => {
		dispatchFut(Backend.getDoiList.map(FreshDoiList(_)))
	}

	private def fetchInfo(doi: Doi): ThunkAction = implicit d => {
		val state = d.getState
		if(state.isSelected(doi) && state.info.get(doi).map(_.hasBeenSaved).getOrElse(true)){
			//fetching info only if the doi is selected and is now new
			//if the info is absent then the doi is not new (otherwise an incomplete DoiInfo would be there)
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

	def requestNewDoi(suffix: String): ThunkAction = implicit d => {
		val doi = Doi(d.getState.stagingPrefix, suffix.toUpperCase)
		doi.error match{
			case Some(err) => d.dispatch(ReportError(err))
			case None =>
				if(d.getState.dois.contains(doi)) { //already on the list
					d.dispatch(RefuseDoiCreation(doi))
				} else{
					dispatchFut(Backend.checkIfExists(doi).map{
						case true => RefuseDoiCreation(doi)
						case false => PermitDoiCreation(doi)
					})
				}

		}
	}

	private def dispatchFut(result: Future[Action])(implicit d: Dispatcher): Unit = {
		result.onComplete{
			case Success(a) => d.dispatch(a)
			case Failure(err) => d.dispatch(ReportError(err.getMessage))
		}
	}
}
