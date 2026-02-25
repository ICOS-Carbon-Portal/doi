package se.lu.nateko.cp.doi.gui

import DoiRedux._
import se.lu.nateko.cp.doi.gui.views.MainView
import se.lu.nateko.cp.doi.gui.views.DoiDetailView
import se.lu.nateko.cp.doi.gui.views.DoiMetaHelpers
import DoiStateUpgrades._
import org.scalajs.dom.document

class Renderer(mainView: MainView, dispatcher: Dispatcher) extends StateListener {

	private var currentDetailView: Option[DoiDetailView] = None

	def notify(state: State, oldState: State): Unit = {
		if(state.prefix != oldState.prefix || state.envs != oldState.envs || state.activeEnv != oldState.activeEnv)
			mainView.updateEnvSelector(state)

		if(oldState.error != state.error){
			if(state.error.isDefined)
				mainView.appendError(state.error.get)
			else
				mainView.clearError()
		}

		if(oldState.success != state.success){
			if(state.success.isDefined)
				mainView.appendSuccess(state.success.get)
			else
				mainView.clearSuccess()
		}

		if(state.dois.ne(oldState.dois) || state.dois.isEmpty){
			mainView.supplyDoiList(state.dois, state.isLoading)
			mainView.resetDoiAdder()
		}

		if(state.listMeta != oldState.listMeta){
			mainView.setPagination(state.listMeta)
		}

		if(state.currentRoute != oldState.currentRoute) {
			state.currentRoute.foreach(renderRoute(_, state))
		}
	}

	private def renderRoute(route: Route, state: State): Unit = {
		route match {
			case ListRoute =>
				currentDetailView = None

				document.title = DoiMetaHelpers.pageTitle("DOI minting")
				
				val mainWrapper = document.getElementById("main-wrapper")
				var listWrapper = document.getElementById("list-wrapper")
				
				// Check if we're showing detail view and need to restore the DOI list
				if (listWrapper == null) {
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
					listWrapper.appendChild(mainView.element)
					mainWrapper.classList.add("loaded")

					mainView.supplyDoiList(state.dois, state.isLoading)
					mainView.setPagination(state.listMeta)

					if (state.error.isDefined) {
						mainView.appendError(state.error.get)
					} else if (state.success.isDefined) {
						mainView.appendSuccess(state.success.get)
					}

					if (showingDetail) {
						Router.getScrollPosition.foreach { scrollY =>
							org.scalajs.dom.window.scrollTo(0, scrollY.toInt)
						}
					}
				}
				
			case DetailRoute(doi) =>
				val mainWrapper = document.getElementById("main-wrapper")
				val isCloned = state.lastClonedDoi.contains(doi)
				state.dois.find(_.doi == doi).orElse {
					// DOI not in cache, fetch it
					Backend.getDoi(doi, state.activeEnv).foreach {
						case Some(meta) =>
							val detailView = new DoiDetailView(meta, dispatcher, isCloned)
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
					val detailView = new DoiDetailView(meta, dispatcher, isCloned)
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
