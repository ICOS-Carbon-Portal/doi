package se.lu.nateko.cp.doi.gui

import org.scalajs.dom

import DoiRedux._
import scala.concurrent.ExecutionContext
import se.lu.nateko.cp.doi.Doi

class Renderer(dispatch: Action => Unit)(implicit ctxt: ExecutionContext) extends StateListener {

	val views = new Views(dispatch)

	def notify(action: Action, state: State, oldState: State): Unit = action match {

		case DoiListRefreshRequest =>
			val listFut = Backend.getDoiList
			listFut.foreach{list => dispatch(FreshDoiList(list))}
			listFut.failed.foreach(reportError)

		case FreshDoiList(dois) => if(dois != oldState.dois){
			val listElem = views.getListElem
			listElem.innerHTML = ""

			for(doi <- dois) {
				listElem.appendChild(views.doiElem(doi, state.selected).render)
			}
		}

		case SelectDoi(doi) =>
			val infoFut = Backend.getInfo(doi)
			infoFut.foreach{info => dispatch(GotDoiInfo(info))}
			infoFut.failed.foreach(reportError)

			val doisToUpdate =
				state.selected.map(_.doi).toList ++
				oldState.selected.map(_.doi).toList

			doisToUpdate.foreach(rerenderDoiElem(_, state.selected))

		case GotDoiInfo(info) =>
			rerenderDoiElem(info.meta.id, state.selected)

		case ReportError(msg) =>
			dom.console.log(msg)
	}

	private def reportError(error: Throwable): Unit = dispatch(ReportError(error.getMessage))


	def rerenderDoiElem(doi: Doi, selected: Option[SelectedDoi]): Unit = {
		for(oldElem <- views.getDoiElem(doi)){
			val newElem = views.doiElem(doi, selected).render
			views.getListElem.replaceChild(newElem, oldElem)
		}
	}

}
