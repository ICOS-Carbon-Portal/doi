package se.lu.nateko.cp.doi.gui

import org.scalajs.dom

import DoiRedux._
import scala.concurrent.ExecutionContext
import se.lu.nateko.cp.doi.Doi
import scala.util.Success
import scala.util.Failure

class Renderer(dispatch: Action => Unit)(implicit ctxt: ExecutionContext) extends StateListener {

	val views = new Views(dispatch)

	def notify(action: Action, state: State, oldState: State): Unit = {

		def rerenderDoi(doi: Doi): Unit = {
			for(oldElem <- views.getDoiElem(doi)){
				val newElem = views.doiElem(doi, state.selected).render
				views.listElem.replaceChild(newElem, oldElem)
			}
		}

		action match {

		case DoiListRefreshRequest =>
			val listFut = Backend.getDoiList
			listFut.foreach{list => dispatch(FreshDoiList(list))}
			listFut.failed.foreach(reportError)

		case FreshDoiList(dois) => if(dois != oldState.dois){
			val listElem = views.listElem
			listElem.innerHTML = ""

			for(doi <- dois) {
				listElem.appendChild(views.doiElem(doi, state.selected).render)
			}
		}

		case SelectDoi(doi) =>
			if(state.isSelected(doi)){
				Backend.getInfo(doi).onComplete{
					case Success(info) =>
						dispatch(GotDoiInfo(info))
						oldState.selected.map(_.doi).foreach(rerenderDoi)
					case Failure(err) => reportError(err)
				}
			}
			rerenderDoi(doi)

		case GotDoiInfo(info) => rerenderDoi(info.meta.id)

		case TargetUrlUpdateRequest(doi, url) =>
			Backend.updateUrl(doi, url).onComplete{
				case Success(_) =>
					dispatch(TargetUrlUpdated(doi, url))
				case Failure(err) =>
					reportError(err)
			}

		case TargetUrlUpdated(doi, url) => rerenderDoi(doi)

		case ReportError(msg) =>
			dom.console.log(msg)
	}
	}

	private def reportError(error: Throwable): Unit = dispatch(ReportError(error.getMessage))

}
