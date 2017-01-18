package se.lu.nateko.cp.doi.gui

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import org.scalajs.dom

import DoiRedux._
import se.lu.nateko.cp.doi.gui.views.MainView

import DoiStateUpgrades._

class Renderer(dispatch: Action => Unit)(implicit ctxt: ExecutionContext) extends StateListener {

	private val mainView = new MainView(dispatch)

	val mainLayout = mainView.element.render

	def notify(action: Action, state: State, oldState: State): Unit = {

		if(state.error.isDefined) mainView.fail(state.error.get)
		else action match {

			case DoiListRefreshRequest =>
				dispatchFut(Backend.getDoiList.map(FreshDoiList(_)))

			case FreshDoiList(dois) =>
				if(dois != oldState.dois){
					mainView.supplyDoiList(dois)
				}

			case SelectDoi(doi) =>
				if(state.isSelected(doi)){

					mainView.setSelected(doi, true)
					for(selectedDoi <- oldState.selected){
						mainView.setSelected(selectedDoi.doi, false)
					}

					dispatchFut(Backend.getInfo(doi).map(GotDoiInfo(_)))
				}else
					mainView.setSelected(doi, false)

			case GotDoiInfo(info) => mainView.supplyInfo(info)
	
			case TargetUrlUpdateRequest(doi, url) =>
				if(state.isUpdatingUrl(doi)){
					mainView.onUrlUpdateBegin(doi)
					dispatchFut(Backend.updateUrl(doi, url).map(_ => TargetUrlUpdated(doi, url)))
				}

			case TargetUrlUpdated(doi, url) =>
				mainView.onUrlUpdated(doi, url)

			case MetaUpdateRequest(meta) =>
				if(state.isUpdatingMeta(meta.id)){
					mainView.onMetadataUpdateBegin(meta.id)
					dispatchFut(Backend.updateMeta(meta).map(_ => MetaUpdated(meta)))
				}

			case MetaUpdated(meta) =>
				mainView.onMetadataUpdated(meta)

			case ReportError(_) =>
		}
	}

	private def dispatchFut[A <: Action](result: Future[A]): Unit = {
		result.onComplete{
			case Success(a) => dispatch(a)
			case Failure(err) => dispatch(ReportError(err.getMessage))
		}
	}
}
