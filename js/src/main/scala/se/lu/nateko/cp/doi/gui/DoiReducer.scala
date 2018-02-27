package se.lu.nateko.cp.doi.gui

import DoiRedux.Reducer
import se.lu.nateko.cp.doi.Doi

import DoiStateUpgrades._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.ResourceType
import se.lu.nateko.cp.doi.meta.ResourceTypeGeneral
import se.lu.nateko.cp.doi.CoolDoi

object DoiReducer {

	val reducer: Reducer = (action, state) => action match{

		case GotPrefixInfo(info) => state.copy(prefixes = info)

		case FreshDoiList(dois, lookup) => state.copy(dois = dois, titleLookup = lookup)

		case SelectDoi(doi) =>
			if(state.isSelected(doi))
				state.copy(selected = None)
			else
				state.withSelected(doi)

		case GotDoiInfo(info) => state.withDoiInfo(info)

		case TargetUrlUpdateRequest(doi, _) => state.startUrlUpdate(doi)

		case TargetUrlUpdated(doi, url) => state.updateUrl(doi, url).stopUrlUpdate(doi)

		case MetaUpdateRequest(meta) => state.startMetaUpdate(meta.id)

		case DoiCloneRequest(meta) => {
			val newDoi = Doi(state.prefixes.staging, CoolDoi.makeRandom)
			val newInfo = DoiInfo(
				meta = meta.copy(id = newDoi, titles = Nil),
				target = None,
				hasBeenSaved = false
			)
			state.copy(dois = newDoi +: state.dois)
				.withSelected(newDoi)
				.withDoiInfo(newInfo)
		}

		case MetaUpdated(meta) => state.updateMeta(meta).stopMetaUpdate(meta.id)

		case RefuseDoiCreation(doi) => state.copy(alreadyExists = Some(doi))

		case PermitDoiCreation(doi) => state.copy(
				alreadyExists = None,
				dois = doi +: state.dois
			)
			.withSelected(doi)
			.withDoiInfo(emptyInfo(doi))

		case ReportError(msg) => state.copy(error = Some(msg))

		case ResetErrors => state.copy(error = None)

	}

	private def emptyInfo(doi: Doi) = DoiInfo(
		meta = DoiMeta(
			id = doi,
			creators = Nil,
			titles = Nil,
			publisher = "",
			publicationYear = 0,
			resourceType = ResourceType("", null)
		),
		target = None,
		hasBeenSaved = false
	)

}
