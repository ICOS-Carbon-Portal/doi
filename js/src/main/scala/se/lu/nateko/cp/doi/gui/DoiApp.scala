package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.document
import org.scalajs.dom.URL
import org.scalajs.dom.window
import se.lu.nateko.cp.doi.gui.views.MainView
import se.lu.nateko.cp.doi.Doi
import org.scalajs.dom.PopStateEvent
import scala.scalajs.js.timers.setTimeout
import scala.concurrent.duration._

object DoiApp {

	val initState = DoiState(
		prefix = "10.18160", //default init value; the actual one is fetched from backend
		dois = Nil,
		listMeta = None,
		selected = None,
		error = None,
		isLoading = true,
		viewMode = ListView
	)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView)
	store.subscribe(renderer)

	// Track if navigation came from popstate to avoid double-updating URL
	var isNavigatingFromHistory = false

	def updateUrl(doi: Option[Doi], searchQuery: Option[String] = None): Unit = {
		val url = new URL(window.location.href)
		// Clear existing params
		url.searchParams.delete("doi")
		if(searchQuery.isEmpty) url.searchParams.delete("q")
		
		// Set new params
		doi.foreach(d => url.searchParams.set("doi", d.toString))
		searchQuery.foreach(q => url.searchParams.set("q", q))
		
		window.history.pushState(null, "", url.toString)
	}

	def parseDoiFromUrl(): Option[Doi] = {
		val url = new URL(window.location.href)
		Option(url.searchParams.get("doi")).filter(_.nonEmpty).flatMap { doiStr =>
			Doi.parse(doiStr).toOption
		}
	}

	def setupHistoryListener(): Unit = {
		window.addEventListener("popstate", (e: PopStateEvent) => {
			isNavigatingFromHistory = true
			parseDoiFromUrl() match {
				case Some(doi) => store.dispatch(SelectDoi(doi))
				case None => store.dispatch(NavigateToList)
			}
			// Reset flag after all subscribers have processed the state change
			setTimeout(0.milliseconds) {
				isNavigatingFromHistory = false
			}
		})
	}

	def main(args: Array[String]): Unit = {

		val mainWrapper = document.getElementById("main-wrapper")
		mainWrapper.appendChild(mainView.element.render)
		val url = new URL(window.location.href)
		val searchQuery = Option(url.searchParams.get("q")).filter(_.nonEmpty)
		searchQuery.map(q => mainView.setSearchQuery(q))

		setupHistoryListener()

		store.dispatch(ThunkActions.FetchPrefixInfo)
		store.dispatch(ThunkActions.DoiListRefreshRequest(searchQuery))

		// Check if there's a DOI in the URL to navigate to
		parseDoiFromUrl().foreach { doi =>
			store.dispatch(SelectDoi(doi))
		}
	}

}
