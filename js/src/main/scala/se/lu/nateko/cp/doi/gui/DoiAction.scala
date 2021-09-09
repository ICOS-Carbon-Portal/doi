package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.DoiMeta

import scala.collection.Seq

sealed trait DoiAction

case class GotPrefixInfo(info: String) extends DoiAction

case object StartLoading extends DoiAction
case class FreshDoiList(dois: Seq[DoiMeta]) extends DoiAction

case class SelectDoi(doi: Doi) extends DoiAction

case class DoiCloneRequest(meta: DoiMeta) extends DoiAction

case class DeleteDoi(doi: Doi) extends DoiAction
case class DoiDeleted(doi: Doi) extends DoiAction
case class EmptyDoiCreation(doi: Doi) extends DoiAction

case class ReportError(msg: String) extends DoiAction
case object ResetErrors extends DoiAction
