package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.PrefixInfo

sealed trait DoiAction

case class GotPrefixInfo(info: PrefixInfo) extends DoiAction

case class FreshDoiList(dois: Seq[Doi], lookup: Map[Doi, String]) extends DoiAction

case class SelectDoi(doi: Doi) extends DoiAction

case class GotDoiInfo(info: DoiInfo) extends DoiAction

case class TargetUrlUpdateRequest(doi: Doi, url: String) extends DoiAction
case class TargetUrlUpdated(doi: Doi, url: String) extends DoiAction

case class MetaUpdateRequest(meta: DoiMeta) extends DoiAction
case class DoiCloneRequest(meta: DoiMeta) extends DoiAction
case class MetaUpdated(meta: DoiMeta) extends DoiAction

case class RefuseDoiCreation(doi: Doi) extends DoiAction
case class PermitDoiCreation(doi: Doi) extends DoiAction

case class ReportError(msg: String) extends DoiAction
case object ResetErrors extends DoiAction
