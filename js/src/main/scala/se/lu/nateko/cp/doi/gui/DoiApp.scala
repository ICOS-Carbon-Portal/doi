package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.document
import se.lu.nateko.cp.doi.gui.views.MainView

object DoiApp {

	val initState = DoiState(
		prefix = "10.18160", //default init value; the actual one is fetched from backend
		dois = Nil,
		selected = None,
		error = None
	)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView)
	store.subscribe(renderer)

	def main(args: Array[String]): Unit = {

		val mainWrapper = document.getElementById("main-wrapper")
		mainWrapper.appendChild(mainView.element.render)

		store.dispatch(ThunkActions.FetchPrefixInfo)
		store.dispatch(ThunkActions.DoiListRefreshRequest)
	}

}
