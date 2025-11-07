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
		isLoading = true
	)
	val store = new DoiRedux.Store(DoiReducer.reducer, initState)

	val mainView = new MainView(store)
	val renderer = new Renderer(mainView)
	store.subscribe(renderer)

	def main(args: Array[String]): Unit = {
		val mainWrapper = document.getElementById("main-wrapper")
		val doiAttr = mainWrapper.getAttribute("data-doi")
		
		store.dispatch(ThunkActions.FetchPrefixInfo)
		
		// Check if we're on a detail page
		if (doiAttr != null && doiAttr.nonEmpty) {
			// Detail page - fetch the specific DOI and render detail view
			Doi.parse(doiAttr).toOption match {
				case Some(doi) =>
					Backend.getDoi(doi).foreach {
						case Some(meta) =>
							val detailView = new DoiDetailView(meta, store)
							mainWrapper.appendChild(detailView.element.render)
							detailView.initialize()
						case None =>
							mainWrapper.innerHTML = s"""<div class="alert alert-danger">DOI not found: $doi</div>"""
					}
				case None =>
					mainWrapper.innerHTML = s"""<div class="alert alert-danger">Invalid DOI: $doiAttr</div>"""
			}
		} else {
			// List page - normal initialization
			mainWrapper.appendChild(mainView.element.render)
			val url = new URL(window.location.href)
			val searchQuery = Option(url.searchParams.get("q")).filter(_.nonEmpty)
			searchQuery.foreach(q => mainView.setSearchQuery(q))
			store.dispatch(ThunkActions.DoiListRefreshRequest(searchQuery))
		}
	}

}
