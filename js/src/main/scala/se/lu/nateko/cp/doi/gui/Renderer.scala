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
		org.scalajs.dom.console.log(s"Rendering route: $route")
		
		route match {
			case InitialRoute =>
				// Should never render this, just a placeholder
				org.scalajs.dom.console.log("Warning: trying to render InitialRoute")
				
			case ListRoute =>
				// Show list view in list-wrapper
				currentDetailView = None
				org.scalajs.dom.console.log("Switching to list view")
				
				val mainWrapper = document.getElementById("main-wrapper")
				var listWrapper = document.getElementById("list-wrapper")
				
				// Check if we're showing detail view (need to restore list structure)
				if (listWrapper == null) {
					org.scalajs.dom.console.log("Restoring list view structure from detail view")
					// Recreate the list view structure with header
					import scalatags.JsDom.all._
					val permissions = mainWrapper.getAttribute("data-permissions")
					mainWrapper.innerHTML = ""
					mainWrapper.appendChild(
						div(
							div(cls := "row")(
								div(cls := "col")(
									div(cls := "page-header")(
										h1("DOI minting")
									)
								)
							),
							div(cls := "row")(
								div(cls := "col")(
									p(raw(permissions))
								)
							),
							hr,
							div(cls := "row")(
								div(cls := "col")(
									div(id := "list-wrapper")
								)
							)
						).render
					)
					listWrapper = document.getElementById("list-wrapper")
				}
				
				val showingDetail = mainWrapper.querySelector("#detail-view") != null
				if (showingDetail || listWrapper.querySelector("#main") == null) {
					org.scalajs.dom.console.log("Rendering list view content")
					listWrapper.innerHTML = ""
					listWrapper.appendChild(mainView.element.render)
					mainWrapper.classList.add("loaded")
					// Update with current data from state (no refetch needed)
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
				// Show detail view, replacing entire main-wrapper content
				val mainWrapper = document.getElementById("main-wrapper")
				state.dois.find(_.doi == doi).orElse {
					// DOI not in cache, fetch it
					Backend.getDoi(doi).foreach {
						case Some(meta) =>
							val detailView = new DoiDetailView(meta, dispatcher)
							currentDetailView = Some(detailView)
							mainWrapper.innerHTML = ""
							mainWrapper.appendChild(detailView.element.render)
							mainWrapper.classList.add("loaded")
							detailView.initialize()
							org.scalajs.dom.window.scrollTo(0, 0)
						case None =>
							mainWrapper.innerHTML = """<div class="alert alert-danger">DOI not found</div>"""
							mainWrapper.classList.add("loaded")
					}(scala.scalajs.concurrent.JSExecutionContext.Implicits.queue)
					None
				}.foreach { meta =>
					// DOI is in cache
					val detailView = new DoiDetailView(meta, dispatcher)
					currentDetailView = Some(detailView)
					mainWrapper.innerHTML = ""
					mainWrapper.appendChild(detailView.element.render)
					mainWrapper.classList.add("loaded")
					detailView.initialize()
					org.scalajs.dom.window.scrollTo(0, 0)
				}
		}
	}

}
