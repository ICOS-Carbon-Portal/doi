package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi

case class DoiInfo(meta: DoiMeta, target: Option[String])
case class SelectedDoi(doi: Doi, info: Option[DoiInfo] = None)

case class DoiState(dois: Seq[Doi], selected: Option[SelectedDoi]){

	def isSelected(doi: Doi): Boolean = selected.exists(_.doi == doi)

}