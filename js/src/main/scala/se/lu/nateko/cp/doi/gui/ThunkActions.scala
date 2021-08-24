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
		dispatchFut(Backend.getFreshDoiList)
	}

	private def writeMeta(meta: DoiMeta, andThen: Option[ThunkAction]): ThunkAction = implicit d => {
		if(d.getState.isUpdatingMeta(meta.doi)){
			val updatedFut = Backend.updateMeta(meta).map(_ => MetaUpdated(meta))
			dispatchFut(updatedFut)
			updatedFut.foreach{_ => andThen foreach d.dispatch}
		}
	}

	def requestMetaUpdate(meta: DoiMeta, andThen: Option[ThunkAction]): ThunkAction = implicit d => {
		d.dispatch(MetaUpdateRequest(meta))
		d.dispatch(writeMeta(meta, andThen))
	}

	private def dispatchFut(result: Future[Action])(implicit d: Dispatcher): Unit = {
		result.onComplete{
			case Success(a) => d.dispatch(a)
			case Failure(err) => d.dispatch(ReportError(err.getMessage))
		}
	}

	def requestDoiDeletion(doi: Doi): ThunkAction = implicit d => {
		dispatchFut(Backend.delete(doi).map(_ => DoiDeleted(doi)))
	}
}
