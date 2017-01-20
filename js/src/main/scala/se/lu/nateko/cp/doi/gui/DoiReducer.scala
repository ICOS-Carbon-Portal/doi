package se.lu.nateko.cp.doi.gui

import DoiRedux.Reducer
import se.lu.nateko.cp.doi.Doi

import DoiStateUpgrades._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.ResourceType
import se.lu.nateko.cp.doi.meta.ResourceTypeGeneral

object DoiReducer {

	val reducer: Reducer = (action, state) => action match{

		case GotDoiPrefix(prefix) => state.copy(prefix = prefix)

		case FreshDoiList(dois) => state.copy(dois = dois)

		case SelectDoi(doi) =>
			if(state.isSelected(doi))
				state.copy(selected = None)
			else
				state.withSelected(doi)

		case GotDoiInfo(info) => state.withDoiInfo(info)

		case TargetUrlUpdateRequest(doi, _) => state.startUrlUpdate(doi)

		case TargetUrlUpdated(doi, url) => state.updateUrl(doi, url).stopUrlUpdate(doi)

		case MetaUpdateRequest(meta) => state.startMetaUpdate(meta.id)

		case MetaUpdated(meta) => state.updateMeta(meta).stopMetaUpdate(meta.id)

		case RefuseDoiCreation(doi) => state.copy(alreadyExists = Some(doi))

		case PermitDoiCreation(doi) => state.copy(
				alreadyExists = None,
				dois = doi +: state.dois
			)
			.withSelected(doi)
			.withDoiInfo(emptyInfo(doi))

		case ReportError(msg) => state.copy(error = Some(msg))

	}

	private def emptyInfo(doi: Doi) = DoiInfo(
		meta = DoiMeta(
			id = doi,
			creators = Nil,
			titles = Nil,
			publisher = "",
			publicationYear = 2017,
			resourceType = ResourceType("", ResourceTypeGeneral.Text)
		),
		target = None
	)

}
