package se.lu.nateko.cp.doi.gui

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import DoiRedux._
import se.lu.nateko.cp.doi.gui.views.MainView

import DoiStateUpgrades._

class Renderer(mainView: MainView) extends StateListener {

	def notify(action: Action, state: State, oldState: State): Unit = {

		if(state.error.isDefined){
			if(oldState.error != state.error) mainView.appendError(state.error.get)
		} else
			mainView.clearErrors()

		if(state.dois != oldState.dois){
			mainView.supplyDoiList(state.dois)
		}

		if(state.selected.map(_.doi) != oldState.selected.map(_.doi)){
			for(selectedDoi <- state.selected){
				mainView.setSelected(selectedDoi.doi, true)
			}
			for(selectedDoi <- oldState.selected){
				mainView.setSelected(selectedDoi.doi, false)
			}
		}

		action match {

			case GotDoiInfo(info) => mainView.supplyInfo(info)
	
			case TargetUrlUpdated(doi, url) =>
				mainView.onUrlUpdated(doi, url)

			case MetaUpdated(meta) =>
				mainView.onMetadataUpdated(meta)

			case _ =>
		}
	}

}
