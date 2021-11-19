package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.document
import org.scalajs.dom.URL
import org.scalajs.dom.window
import se.lu.nateko.cp.doi.gui.views.MainView

object DoiApp {

	val initState = DoiState(
		prefix = "10.18160", //default init value; the actual one is fetched from backend
		dois = Nil,
		listMeta = None,
		selected = None,
		error = None,
		isLoading = true
	)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView)
	store.subscribe(renderer)

	def main(args: Array[String]): Unit = {

		val mainWrapper = document.getElementById("main-wrapper")
		mainWrapper.appendChild(mainView.element.render)
		val url = new URL(window.location.href)
		val searchQuery = Option(url.searchParams.get("q")).filter(_.nonEmpty)
		searchQuery.map(q => mainView.setSearchQuery(q))

		store.dispatch(ThunkActions.FetchPrefixInfo)
		store.dispatch(ThunkActions.DoiListRefreshRequest(searchQuery))
	}

}
