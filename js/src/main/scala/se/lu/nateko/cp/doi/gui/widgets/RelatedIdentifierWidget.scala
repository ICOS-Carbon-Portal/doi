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
	}, "Related identifier", required = true)

	private[this] val relationTypeInput = new SelectWidget[RelationType](
		SelectWidget.selectOptions(Some("Relation type"), RelationType.values),
		init.relationType,
		rtOpt => {
			_relatedIdentifier = _relatedIdentifier.copy(relationType = rtOpt)
			rtOpt.getOrElse("") match {
				case RelationType.HasMetadata => relatedMetadataDiv.classList.remove("d-none")
				case RelationType.IsMetadataFor => relatedMetadataDiv.classList.remove("d-none")
				case _ => relatedMetadataDiv.classList.add("d-none")
			}
			updateCb(_relatedIdentifier)
		}
	)

	private[this] val relatedIdentifierTypeInput = new SelectWidget[RelatedIdentifierType](
		SelectWidget.selectOptions(Some("Related identifier type"), RelatedIdentifierType.values),
		init.relatedIdentifierType,
		rit => {
			_relatedIdentifier = _relatedIdentifier.copy(relatedIdentifierType = rit)
			updateCb(_relatedIdentifier)
		}
	)

	private[this] val resourceTypeGeneralInput = new SelectWidget[ResourceTypeGeneral](
		SelectWidget.selectOptions(Some("Resource type general"), ResourceTypeGeneral.values),
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
	}, "Related metadata scheme", required = false)

	private[this] val schemeUriInput = new TextInputWidget(init.schemeUri.getOrElse(""), uri => {
		val uriOpt = if (uri.isEmpty) None else Some(uri)
		_relatedIdentifier = _relatedIdentifier.copy(schemeUri = uriOpt)
		updateCb(_relatedIdentifier)
	}, "Scheme uri", required = false)

	private[this] val schemeTypeInput = new TextInputWidget(init.schemeType.getOrElse(""), st => {
		val stOpt = if (st.isEmpty) None else Some(st)
		_relatedIdentifier = _relatedIdentifier.copy(schemeType = stOpt)
		updateCb(_relatedIdentifier)
	}, "Scheme type", required = false)

	var initialRelationType = init.relationType.getOrElse("") match {
		case RelationType.HasMetadata => "row spacyrow"
		case RelationType.IsMetadataFor => "row spacyrow"
		case _ => "row spacyrow d-none"
	}

	private[this] var relatedMetadataDiv = div(cls := initialRelationType)(
		div(cls := "col-md-2")(strong("Metadata scheme")),
		div(cls := "col-md-4")(relatedMetadataSchemeInput.element)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("Scheme uri")),
		div(cls := "col-md-4")(schemeUriInput.element)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("Scheme type")),
		div(cls := "col-md-4")(schemeTypeInput.element)(paddingBottom := 15),
	).render

	val element = div(cls := "row spacyrow")(
		div(cls := "row")(
			div(cls := "col-md-2")(strong("Related identifier")),
			div(cls := "col-md-4")(relatedIdentifierInput.element)(paddingBottom := 15),
			div(cls := "col-md-2")(strong("Relation type")),
			div(cls := "col-md-4")(relationTypeInput.element)(paddingBottom := 15),
			div(cls := "col-md-2")(strong("Identifier type")),
			div(cls := "col-md-4")(relatedIdentifierTypeInput.element)(paddingBottom := 15),
			div(cls := "col-md-2")(strong("Resource type general")),
			div(cls := "col-md-4")(resourceTypeGeneralInput.element)(paddingBottom := 15),
		),
		relatedMetadataDiv
	).render
}
