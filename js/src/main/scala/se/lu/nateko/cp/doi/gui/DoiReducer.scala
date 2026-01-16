package se.lu.nateko.cp.doi.gui

import DoiRedux.Reducer
import se.lu.nateko.cp.doi.Doi

import DoiStateUpgrades._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.ResourceType
import se.lu.nateko.cp.doi.meta.ResourceTypeGeneral
import se.lu.nateko.cp.doi.CoolDoi
import se.lu.nateko.cp.doi.meta.DoiPublicationState

object DoiReducer {

	val reducer: Reducer = (action, state) => action match{

		case GotPrefixInfo(info) => state.copy(prefix = info)

		case StartLoading => state.copy(isLoading = true)
		case StopLoading => state.copy(isLoading = false)

		case FreshDoiList(dois, listMeta) => state.copy(dois = dois, listMeta = listMeta)

		case NavigateToRoute(route) =>
			Router.navigateTo(route) // This saves scroll position in history.state
			val newState = state.copy(currentRoute = route)
			newState

	case DoiCloneRequest(originalMeta, clonedMeta) => {
		// Navigate to the new cloned DOI
		Router.navigateTo(DetailRoute(clonedMeta.doi))

		val newState = state.copy(
			dois = clonedMeta +: state.dois,
			lastClonedDoi = Some(clonedMeta.doi),
			currentRoute = DetailRoute(clonedMeta.doi)
		).withSelected(clonedMeta.doi)
			.incrementTotal
		newState
	}

		case EmptyDoiCreation(doi) => state.copy(dois = DoiMeta(doi) +: state.dois)
			.incrementTotal
			.withSelected(doi)

		case ReportError(msg) => state.copy(error = Some(msg))

		case ResetErrors => state.copy(error = None)

	case DoiDeleted(doi) => state.copy(
		dois = state.dois.filter(_.doi != doi),
		selected = None
	).decrementTotal

	case DoiUpdated(updatedMeta) => state.copy(
		dois = state.dois.map(meta => if (meta.doi == updatedMeta.doi) updatedMeta else meta)
	)

	case ClearLastClonedDoi => state.copy(lastClonedDoi = None)

	}

}
