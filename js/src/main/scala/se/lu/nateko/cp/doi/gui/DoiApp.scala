package se.lu.nateko.cp.doi.gui

import scala.scalajs.js.JSApp
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom.document

object DoiApp extends JSApp {

	val initState = DoiState(Nil, None)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val views = {
		val renderer = new Renderer(store.dispatch)
		store.subscribe(renderer)
		renderer.views
	}

	def main(): Unit = {
		val mainDiv = document.getElementById("main")
		mainDiv.parentNode.replaceChild(views.mainLayout.render, mainDiv)

		views.refreshDoiList(null)
	}

}
