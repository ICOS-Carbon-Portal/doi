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

	val element = div(cls := "row")(
		div(cls := "col-md-6")(relatedIdentifierInput.element),
		div(cls := "col-md-6")(relationTypeInput.element),
		div(cls := "col-md-6")(relatedIdentifierTypeInput.element),
		div(cls := "col-md-6")(resourceTypeGeneralInput.element),
		div(cls := "col-md-6")(relatedMetadataSchemeInput.element),
		div(cls := "col-md-6")(schemeUriInput.element),
		div(cls := "col-md-6")(schemeTypeInput.element)
	).render
}
