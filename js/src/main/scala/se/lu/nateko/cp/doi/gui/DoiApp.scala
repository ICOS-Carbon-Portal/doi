package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.document
import org.scalajs.dom.URL
import org.scalajs.dom.window
import se.lu.nateko.cp.doi.gui.views.MainView
import se.lu.nateko.cp.doi.gui.views.DoiDetailView
import se.lu.nateko.cp.doi.Doi
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object DoiApp {

	val initState = DoiState(
		prefix = "10.18160", //default init value; the actual one is fetched from backend
		dois = Nil,
		listMeta = None,
		selected = None,
		error = None,
		success = None,
		isLoading = true,
		currentRoute = InitialRoute
	)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView, store)
	store.subscribe(renderer)

	def main(args: Array[String]): Unit = {
		
		Router.setupListener { route =>
			store.dispatch(NavigateToRoute(route))
		}
		
		store.dispatch(ThunkActions.FetchPrefixInfo)
		
		val url = new URL(window.location.href)
		val searchQuery = Option(url.searchParams.get("q")).filter(_.nonEmpty)
		searchQuery.foreach(q => mainView.setSearchQuery(q))
		
		store.dispatch(ThunkActions.DoiListRefreshRequest(searchQuery))
		
		val initialRoute = Router.getCurrentRoute
		store.dispatch(NavigateToRoute(initialRoute))
	}

}
