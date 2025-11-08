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
		isLoading = true,
		currentRoute = InitialRoute // Start with placeholder to force initial render
	)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView, store)
	store.subscribe(renderer)

	def main(args: Array[String]): Unit = {
		org.scalajs.dom.console.log("DoiApp.main starting")
		
		// Setup router listener
		Router.setupListener { route =>
			org.scalajs.dom.console.log(s"Router listener triggered: $route")
			store.dispatch(NavigateToRoute(route))
		}
		
		store.dispatch(ThunkActions.FetchPrefixInfo)
		
		// Get search query from URL if on list page
		val url = new URL(window.location.href)
		val searchQuery = Option(url.searchParams.get("q")).filter(_.nonEmpty)
		searchQuery.foreach(q => mainView.setSearchQuery(q))
		
		// Fetch list data
		store.dispatch(ThunkActions.DoiListRefreshRequest(searchQuery))
		
		// Trigger initial render based on current route
		val initialRoute = Router.getCurrentRoute
		org.scalajs.dom.console.log(s"Initial route: $initialRoute")
		store.dispatch(NavigateToRoute(initialRoute))
		org.scalajs.dom.console.log("DoiApp.main complete")
	}

}
