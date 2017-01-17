package se.lu.nateko.cp.doi.gui

import org.scalajs.dom

import DoiRedux._
import scala.concurrent.ExecutionContext
import se.lu.nateko.cp.doi.Doi
import scala.util.Success
import scala.util.Failure
import se.lu.nateko.cp.doi.gui.views.DoiView

class Renderer(dispatch: Action => Unit)(implicit ctxt: ExecutionContext) extends StateListener {

	val views = new Views(dispatch)

	val doiViews = scala.collection.mutable.Map.empty[Doi, DoiView]

	def notify(action: Action, state: State, oldState: State): Unit = {

		action match {

		case DoiListRefreshRequest =>
			val listFut = Backend.getDoiList
			listFut.foreach{list => dispatch(FreshDoiList(list))}
			listFut.failed.foreach(reportError)

		case FreshDoiList(dois) => if(dois != oldState.dois){
			val listElem = views.listElem
			listElem.innerHTML = ""
			doiViews.clear()

			for(doi <- dois) {
				val doiView = new DoiView(doi, dispatch)
				doiViews += ((doi, doiView))
				listElem.appendChild(doiView.element)
			}
		}

		case SelectDoi(doi) =>
			if(state.isSelected(doi)){
				doiViews(doi).setSelected(true)
				Backend.getInfo(doi).onComplete{
					case Success(info) =>
						dispatch(GotDoiInfo(info))
					case Failure(err) => reportError(err)
				}
			}else
				doiViews(doi).setSelected(false)

		case GotDoiInfo(info) => doiViews(info.meta.id).supplyInfo(info)

		case TargetUrlUpdateRequest(doi, url) =>
			Backend.updateUrl(doi, url).onComplete{
				case Success(_) =>
					dispatch(TargetUrlUpdated(doi, url))
				case Failure(err) =>
					reportError(err)
			}

		case TargetUrlUpdated(doi, url) =>

		case MetaUpdateRequest(meta) =>

		case ReportError(msg) =>
			dom.console.log(msg)
	}
	}

	private def reportError(error: Throwable): Unit = dispatch(ReportError(error.getMessage))

}
