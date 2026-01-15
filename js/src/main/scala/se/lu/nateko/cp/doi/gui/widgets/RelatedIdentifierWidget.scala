package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._

import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget

import se.lu.nateko.cp.doi.meta.ResourceTypeGeneral

import se.lu.nateko.cp.doi.meta.RelatedIdentifier
import se.lu.nateko.cp.doi.meta.RelatedIdentifierType
import se.lu.nateko.cp.doi.meta.RelationType
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget

class RelatedIdentifierWidget(init: RelatedIdentifier, protected val updateCb: RelatedIdentifier => Unit) extends EntityWidget[RelatedIdentifier] {
	private[this] var _relatedIdentifier = init

	private[this] val relatedIdentifierInput = new TextInputWidget(init.relatedIdentifier, ri => {
		_relatedIdentifier = _relatedIdentifier.copy(relatedIdentifier = ri)
		updateCb(_relatedIdentifier)
	}, required = true)

	private[this] val relationTypeInput = new SelectWidget[RelationType](
		SelectWidget.selectOptions(Some("Relation type"), RelationType.values),
		init.relationType,
		rtOpt => {
			_relatedIdentifier = _relatedIdentifier.copy(relationType = rtOpt)
			updateRelatedMetadataVisibility(rtOpt)
			updateCb(_relatedIdentifier)
		}
	)

	private[this] val relatedIdentifierTypeInput = new SelectWidget[RelatedIdentifierType](
		SelectWidget.selectOptions(Some("Select type"), RelatedIdentifierType.values),
		init.relatedIdentifierType,
		rit => {
			_relatedIdentifier = _relatedIdentifier.copy(relatedIdentifierType = rit)
			updateCb(_relatedIdentifier)
		}
	)

	private[this] val resourceTypeGeneralInput = new SelectWidget[ResourceTypeGeneral](
		SelectWidget.selectOptions(Some("Select type"), ResourceTypeGeneral.values),
		init.resourceTypeGeneral,
		rtgOpt => {
			_relatedIdentifier = _relatedIdentifier.copy(resourceTypeGeneral = rtgOpt)
			updateCb(_relatedIdentifier)
		}
	)

	private[this] val relatedMetadataSchemeInput = new TextInputWidget(init.relatedMetadataScheme.getOrElse(""), rmsi => {
		val rmsiOpt = if (rmsi.isEmpty) None else Some(rmsi)
		_relatedIdentifier = _relatedIdentifier.copy(relatedMetadataScheme = rmsiOpt)
		updateCb(_relatedIdentifier)
	}, required = false)

	private[this] val schemeUriInput = new TextInputWidget(init.schemeUri.getOrElse(""), uri => {
		val uriOpt = if (uri.isEmpty) None else Some(uri)
		_relatedIdentifier = _relatedIdentifier.copy(schemeUri = uriOpt)
		updateCb(_relatedIdentifier)
	}, required = false)

	private[this] val schemeTypeInput = new TextInputWidget(init.schemeType.getOrElse(""), st => {
		val stOpt = if (st.isEmpty) None else Some(st)
		_relatedIdentifier = _relatedIdentifier.copy(schemeType = stOpt)
		updateCb(_relatedIdentifier)
	}, required = false)

	private def shouldShowMetadata(relationType: Option[RelationType]): Boolean = relationType match
		case Some(RelationType.HasMetadata) | Some(RelationType.IsMetadataFor) => true
		case _ => false

	private def getInitialClass = if (shouldShowMetadata(init.relationType)) "col-md-6" else "col-md-6 d-none"

	private[this] val metadataSchemeCol = div(cls := getInitialClass)(
		label(cls := "form-label")("Metadata scheme"),
		div(relatedMetadataSchemeInput.element),
	).render

	private[this] val schemeUriCol = div(cls := getInitialClass)(
		label(cls := "form-label")("Scheme uri"),
		div(schemeUriInput.element),
	).render

	private[this] val schemeTypeCol = div(cls := getInitialClass)(
		label(cls := "form-label")("Scheme type"),
		div(schemeTypeInput.element),
	).render

	private def updateRelatedMetadataVisibility(relationType: Option[RelationType]): Unit = {
		val shouldShow = shouldShowMetadata(relationType)
		val cols = Seq(metadataSchemeCol, schemeUriCol, schemeTypeCol)

		if (shouldShow) {
			cols.foreach(_.classList.remove("d-none"))
		} else {
			cols.foreach(_.classList.add("d-none"))
		}
	}

	val element = div(cls := "row spacyrow g-3")(
		div(cls := "col-md-6")(
			label(cls := "form-label")("Related identifier"),
			div(relatedIdentifierInput.element),
		),
		div(cls := "col-md-6")(
			label(cls := "form-label")("Relation type"),
			div(relationTypeInput.element),
		),
		div(cls := "col-md-6")(
			label(cls := "form-label")("Identifier type"),
			div(relatedIdentifierTypeInput.element),
		),
		div(cls := "col-md-6")(
			label(cls := "form-label")("Resource type general"),
			div(resourceTypeGeneralInput.element),
		),
		metadataSchemeCol,
		schemeUriCol,
		schemeTypeCol,
	).render
}
