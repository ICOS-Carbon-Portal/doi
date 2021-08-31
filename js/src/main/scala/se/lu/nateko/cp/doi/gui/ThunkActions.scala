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
		/* Useful in the future, when fetching will be done many times due to e.g. paging or search
		import scalajs.js.timers.{setTimeout, clearTimeout}
		val handle = setTimeout(1000){
			d.dispatch(FreshDoiList(Nil))
		}
		dispatchFut(Backend.getFreshDoiList.andThen{
			case _ => clearTimeout(handle)
		})
		*/
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
