package se.lu.nateko.cp.doi.gui

import org.scalajs.dom.window
import org.scalajs.dom.PopStateEvent
import se.lu.nateko.cp.doi.Doi

sealed trait Route
case object InitialRoute extends Route // Placeholder before first navigation
case object ListRoute extends Route
case class DetailRoute(doi: Doi) extends Route

object Router {
	
	def getCurrentRoute: Route = {
		val path = window.location.pathname
		parseRoute(path)
	}
	
	def parseRoute(path: String): Route = {
		// Match /doi/{prefix}/{suffix}
		val detailPattern = """^/doi/(.+)$""".r
		
		path match {
			case "/" => ListRoute
			case detailPattern(doiStr) =>
				Doi.parse(doiStr).toOption match {
					case Some(doi) => DetailRoute(doi)
					case None => ListRoute // Invalid DOI, fallback to list
				}
			case _ => ListRoute
		}
	}
	
	def navigateTo(route: Route, scrollPosition: Option[Double] = None): Unit = {
		val url = routeToUrl(route)
		if (window.location.pathname != url) {
			// Save current scroll position in history state before navigating
			val currentState = scala.scalajs.js.Dynamic.literal(
				"scrollY" -> scrollPosition.getOrElse(window.pageYOffset)
			)
			window.history.replaceState(currentState, "") // Update current entry with scroll pos
			window.history.pushState(null, "", url) // Push new entry
		}
	}
	
	def getScrollPosition: Option[Double] = {
		val state = window.history.state
		if (state != null && !scala.scalajs.js.isUndefined(state)) {
			val scrollY = state.asInstanceOf[scala.scalajs.js.Dynamic].scrollY
			if (!scala.scalajs.js.isUndefined(scrollY)) {
				Some(scrollY.asInstanceOf[Double])
			} else None
		} else None
	}
	
	def routeToUrl(route: Route): String = route match {
		case InitialRoute => "/" // Should never be called
		case ListRoute => "/"
		case DetailRoute(doi) => s"/doi/$doi"
	}
	
	def setupListener(onChange: Route => Unit): Unit = {
		// Handle browser back/forward
		window.addEventListener("popstate", (_: PopStateEvent) => {
			onChange(getCurrentRoute)
		})
	}
}
