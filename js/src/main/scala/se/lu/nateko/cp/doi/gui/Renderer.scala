package se.lu.nateko.cp.doi.gui

import DoiRedux._
import se.lu.nateko.cp.doi.gui.views.MainView

import DoiStateUpgrades._
import org.scalajs.dom.URL
import org.scalajs.dom.window

class Renderer(mainView: MainView) extends StateListener {

	def notify(state: State, oldState: State): Unit = {

		if(state.prefix != oldState.prefix) mainView.updateDefaultPrefix()

		if(oldState.error != state.error){
			if(state.error.isDefined)
				mainView.appendError(state.error.get)
			else
				mainView.clearErrors()
		}

	if(state.dois.ne(oldState.dois) || state.dois.isEmpty){
		// If we're in detail view mode, check if we should show the detail view
		state.viewMode match {
			case DetailView(doi) if state.dois.exists(_.doi == doi) =>
				// DOI is now available in the list, show detail view
				mainView.showDetailView(doi)
			case ListView =>
				// In list view, update the list normally
				mainView.supplyDoiList(state.dois, state.isLoading)
				mainView.resetDoiAdder()
			case _ =>
				// DOI not yet available, or other state
				()
		}
	}

		if(state.listMeta != oldState.listMeta){
			mainView.setPagination(state.listMeta)
		}

		if(state.viewMode != oldState.viewMode){
			val currentDoiInUrl = DoiApp.parseDoiFromUrl()
			
			state.viewMode match {
				case DetailView(doi) =>
					mainView.showDetailView(doi)
					// Update URL only if not navigating from history and URL doesn't already match
					if(!DoiApp.isNavigatingFromHistory && !currentDoiInUrl.contains(doi)) {
						val url = new URL(window.location.href)
						val searchQuery = Option(url.searchParams.get("q")).filter(_.nonEmpty)
						DoiApp.updateUrl(Some(doi), searchQuery)
					}
				case ListView =>
					mainView.showListView()
					// Update URL only if not navigating from history and URL still has a DOI
					if(!DoiApp.isNavigatingFromHistory && currentDoiInUrl.isDefined) {
						val url = new URL(window.location.href)
						val searchQuery = Option(url.searchParams.get("q")).filter(_.nonEmpty)
						DoiApp.updateUrl(None, searchQuery)
					}
			}
		}

		// Keep the old setSelected logic for highlighting in list view if needed
		if(state.selected != oldState.selected && state.viewMode == ListView){
			state.selected.foreach(mainView.setSelected(_, true))
			oldState.selected.foreach(mainView.setSelected(_, false))
		}
	}

}
