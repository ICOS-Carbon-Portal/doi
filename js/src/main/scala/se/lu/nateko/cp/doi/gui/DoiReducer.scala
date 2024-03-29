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

		case SelectDoi(doi) =>
			if(state.isSelected(doi))
				state.copy(selected = None)
			else
				state.withSelected(doi)

		case DoiCloneRequest(meta) => {
			val newDoi = meta.doi.copy(suffix = CoolDoi.makeRandom)
			val newMeta = meta.copy(doi = newDoi, titles = None, state = DoiPublicationState.draft)

			state.copy(dois = newMeta +: state.dois)
				.withSelected(newDoi)
				.incrementTotal
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

	}

}
