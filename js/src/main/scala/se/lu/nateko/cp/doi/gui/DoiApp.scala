package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.document
import se.lu.nateko.cp.doi.gui.views.MainView
import se.lu.nateko.cp.doi.PrefixInfo

object DoiApp {

	val initState = DoiState(
		prefixes = PrefixInfo(
			staging = "10.5072",//default init value; the actual one is fetched from backend
			production = "10.18160"//default init value; the actual one is fetched from backend
		),
		dois = Nil,
		info = Map.empty,
		selected = None,
		ioState = IoState(None, None),
		error = None
	)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView)
	store.subscribe(renderer)

	def main(args: Array[String]): Unit = {

		val mainDiv = document.getElementById("main")
		mainDiv.parentNode.replaceChild(mainView.element.render, mainDiv)

		store.dispatch(ThunkActions.FetchPrefixInfo)
		store.dispatch(ThunkActions.DoiListRefreshRequest)
	}

}
