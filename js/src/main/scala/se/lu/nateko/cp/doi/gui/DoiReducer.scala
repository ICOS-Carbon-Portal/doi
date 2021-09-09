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

		case FreshDoiList(dois) => state.copy(dois = dois, isLoading = false)

		case SelectDoi(doi) =>
			if(state.isSelected(doi))
				state.copy(selected = None)
			else
				state.withSelected(doi)

		case DoiCloneRequest(meta) => {
			val newDoi = meta.doi.copy(suffix = CoolDoi.makeRandom)

			state.copy(dois = DoiMeta(newDoi) +: state.dois.filter(_.doi != newDoi))
				.withSelected(newDoi)
		}

		case EmptyDoiCreation(doi) => state.copy(
				dois = DoiMeta(doi) +: state.dois
			)
			.withSelected(doi)

		case ReportError(msg) => state.copy(error = Some(msg))

		case ResetErrors => state.copy(error = None)

		case DeleteDoi(doi) => state

		case DoiDeleted(doi) => state.copy(
			dois = state.dois.filter(_.doi != doi),
			selected = None
		)

	}

}
