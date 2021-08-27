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

		case FreshDoiList(dois) => state.copy(dois = dois)

		case SelectDoi(doi) =>
			if(state.isSelected(doi))
				state.copy(selected = None)
			else
				state.withSelected(doi)

		case DoiCloneRequest(meta) => {
			val newDoi = meta.doi.copy(suffix = CoolDoi.makeRandom)

			val newInfo = DoiInfo(
				meta = meta.copy(doi = newDoi, titles = None),
				target = None,
				hasBeenSaved = false
			)
			state.copy(dois = DoiMeta(newDoi) +: state.dois.filter(_.doi != newDoi))
				.withSelected(newDoi)
				.withDoiInfo(newInfo)
		}

		case EmptyDoiCreation(doi) => state.copy(
				dois = DoiMeta(doi) +: state.dois
			)
			.withSelected(doi)
			.withDoiInfo(emptyInfo(doi))

		case ReportError(msg) => state.copy(error = Some(msg))

		case ResetErrors => state.copy(error = None)

		case DeleteDoi(doi) => state

		case DoiDeleted(doi) => state.copy(
			dois = state.dois.filter(_.doi != doi),
			info = state.info.filter(_._1 != doi),
			selected = None
		)

	}

	private def emptyInfo(doi: Doi) = DoiInfo(
		meta = DoiMeta(
			doi = doi,
			state = DoiPublicationState.draft,
			creators = Nil,
			titles = None,
			publisher = None,
			publicationYear = None,
			types = None,
			url = None
		),
		target = None,
		hasBeenSaved = false
	)

}
