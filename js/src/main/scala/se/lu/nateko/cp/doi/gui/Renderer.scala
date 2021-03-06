package se.lu.nateko.cp.doi.gui

import DoiRedux._
import se.lu.nateko.cp.doi.gui.views.MainView

import DoiStateUpgrades._

class Renderer(mainView: MainView) extends StateListener {

	def notify(state: State, oldState: State): Unit = {

		if(state.prefixes != oldState.prefixes) mainView.updateDefaultPrefix()

		if(oldState.error != state.error){
			if(state.error.isDefined)
				mainView.appendError(state.error.get)
			else
				mainView.clearErrors()
		}

		if(state.dois != oldState.dois){
			mainView.supplyDoiList(state.dois)
			mainView.resetDoiAdder()
		}

		val selectedInfo = state.selectedInfo

		if(state.selected != oldState.selected){
			selectedInfo.foreach(mainView.supplyInfo)

			state.selected.foreach(mainView.setSelected(_, true))

			oldState.selected.foreach(mainView.setSelected(_, false))

		}else if(selectedInfo != oldState.selectedInfo){

			selectedInfo.foreach(mainView.supplyInfo)

		}
	}

}
