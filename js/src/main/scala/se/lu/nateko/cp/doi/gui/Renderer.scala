package se.lu.nateko.cp.doi.gui

import DoiRedux._
import se.lu.nateko.cp.doi.gui.views.MainView
import se.lu.nateko.cp.doi.gui.views.DoiDetailView
import DoiStateUpgrades._
import org.scalajs.dom.document

class Renderer(mainView: MainView, dispatcher: Dispatcher) extends StateListener {

	private var currentDetailView: Option[DoiDetailView] = None

	def notify(state: State, oldState: State): Unit = {
		org.scalajs.dom.console.log("Renderer.notify called")

		if(state.prefix != oldState.prefix) mainView.updateDefaultPrefix()

		if(oldState.error != state.error){
			if(state.error.isDefined)
				mainView.appendError(state.error.get)
			else
				mainView.clearErrors()
		}

		if(state.dois.ne(oldState.dois) || state.dois.isEmpty){
			mainView.supplyDoiList(state.dois, state.isLoading)
			mainView.resetDoiAdder()
		}

		if(state.listMeta != oldState.listMeta){
			mainView.setPagination(state.listMeta)
		}

		// Handle route changes
		if(state.currentRoute != oldState.currentRoute) {
			org.scalajs.dom.console.log(s"Route changed from ${oldState.currentRoute} to ${state.currentRoute}")
			renderRoute(state.currentRoute, state)
		} else {
			org.scalajs.dom.console.log(s"Route unchanged: ${state.currentRoute}")
		}
	}

	private def renderRoute(route: Route, state: State): Unit = {
		val mainWrapper = document.getElementById("main-wrapper")
		org.scalajs.dom.console.log(s"Rendering route: $route")
		
		route match {
			case InitialRoute =>
				// Should never render this, just a placeholder
				org.scalajs.dom.console.log("Warning: trying to render InitialRoute")
				
			case ListRoute =>
				// Show list view
				currentDetailView = None
				org.scalajs.dom.console.log("Switching to list view")
				// Check if we're showing detail view (need to switch) or already on list
				val showingDetail = mainWrapper.querySelector("#detail-view") != null
				if (showingDetail || mainWrapper.querySelector("#main") == null) {
					org.scalajs.dom.console.log("Rendering list view (was showing detail or nothing)")
					mainWrapper.innerHTML = ""
					mainWrapper.appendChild(mainView.element.render)
					// Update with current data
					mainView.supplyDoiList(state.dois, state.isLoading)
					mainView.setPagination(state.listMeta)
					// Restore scroll position from history state
					if (showingDetail) {
						Router.getScrollPosition.foreach { scrollY =>
							org.scalajs.dom.console.log(s"Restoring scroll position from history: $scrollY")
							org.scalajs.dom.window.scrollTo(0, scrollY.toInt)
						}
					}
				} else {
					org.scalajs.dom.console.log("List view already rendered, keeping it")
				}
				
			case DetailRoute(doi) =>
				// Show detail view
				state.dois.find(_.doi == doi).orElse {
					// DOI not in cache, fetch it
					Backend.getDoi(doi).foreach {
						case Some(meta) =>
							val detailView = new DoiDetailView(meta, dispatcher)
							currentDetailView = Some(detailView)
							mainWrapper.innerHTML = ""
							mainWrapper.appendChild(detailView.element.render)
							detailView.initialize()
							org.scalajs.dom.window.scrollTo(0, 0)
						case None =>
							mainWrapper.innerHTML = """<div class="alert alert-danger">DOI not found</div>"""
					}(scala.scalajs.concurrent.JSExecutionContext.Implicits.queue)
					None
				}.foreach { meta =>
					// DOI is in cache
					val detailView = new DoiDetailView(meta, dispatcher)
					currentDetailView = Some(detailView)
					mainWrapper.innerHTML = ""
					mainWrapper.appendChild(detailView.element.render)
					detailView.initialize()
					org.scalajs.dom.window.scrollTo(0, 0)
				}
		}
	}

}
