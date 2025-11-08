package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiListMeta

import scala.collection.Seq

case class DoiState(
	prefix: String,
	dois: Seq[DoiMeta],
	listMeta: Option[DoiListMeta],
	selected: Option[Doi],
	error: Option[String],
	isLoading: Boolean,
	currentRoute: Route
)

object DoiStateUpgrades{

	implicit class SmartDoiState(val state: DoiState) extends AnyVal{

		def withSelected(doi: Doi): DoiState = state.copy(selected = Some(doi))
		def isSelected(doi: Doi): Boolean = state.selected.contains(doi)
		private def withListMeta(lmCopy: DoiListMeta => DoiListMeta): DoiState = state.copy(listMeta = state.listMeta.map(lmCopy))
		def incrementTotal: DoiState = withListMeta(
			lmeta => lmeta.copy(total = lmeta.total + 1)
		)
		def decrementTotal: DoiState = withListMeta(
			lmeta => lmeta.copy(total = lmeta.total - 1)
		)
	}

}
