package se.lu.nateko.cp.doi.gui

import DoiRedux._
import se.lu.nateko.cp.doi.gui.views.MainView
import se.lu.nateko.cp.doi.gui.views.DoiDetailView
import DoiStateUpgrades._
import org.scalajs.dom.document

class Renderer(mainView: MainView, dispatcher: Dispatcher) extends StateListener {

	private var currentDetailView: Option[DoiDetailView] = None

	def notify(state: State, oldState: State): Unit = {
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
			renderRoute(state.currentRoute, state)
		}
	}

	private def renderRoute(route: Route, state: State): Unit = {
		route match {
			case InitialRoute =>
				// Should never render this, just a placeholder
				
			case ListRoute =>
				// Show list view in list-wrapper
				currentDetailView = None
				
				val mainWrapper = document.getElementById("main-wrapper")
				var listWrapper = document.getElementById("list-wrapper")
				
				// Check if we're showing detail view (need to restore list structure)
				if (listWrapper == null) {
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
					listWrapper.innerHTML = ""
					listWrapper.appendChild(mainView.element.render)
					mainWrapper.classList.add("loaded")
					// Update with current data from state (no refetch needed)
					mainView.supplyDoiList(state.dois, state.isLoading)
					mainView.setPagination(state.listMeta)
					// Restore scroll position from history state
					if (showingDetail) {
						Router.getScrollPosition.foreach { scrollY =>
							org.scalajs.dom.window.scrollTo(0, scrollY.toInt)
						}
					}
				} else {
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
