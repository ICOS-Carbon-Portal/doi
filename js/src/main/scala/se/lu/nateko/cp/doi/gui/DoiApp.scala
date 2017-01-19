package se.lu.nateko.cp.doi.gui

import scala.scalajs.js.JSApp
import org.scalajs.dom.document
import se.lu.nateko.cp.doi.gui.views.MainView

object DoiApp extends JSApp {

	val initState = DoiState(Nil, None, IoState(None, None), None)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView)
	store.subscribe(renderer)

	def main(): Unit = {
		val mainDiv = document.getElementById("main")
		mainDiv.parentNode.replaceChild(mainView.element.render, mainDiv)

		store.dispatch(ThunkActions.DoiListRefreshRequest)
	}

}
