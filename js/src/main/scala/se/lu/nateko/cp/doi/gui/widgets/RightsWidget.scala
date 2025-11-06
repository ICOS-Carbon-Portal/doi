package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Rights
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget

class RightsWidget(init: Rights, protected val updateCb: Rights => Unit) extends EntityWidget[Rights] {
	private[this] var _rights = init

	private[this] val ccBy4Button = button(
		cls := "btn btn-secondary",
		tpe := "button",
		onclick := (autofillCcBy4 _)
	)("CC-BY-4.0").render

	private def autofillCcBy4(): Unit = {
		statementInput.element.value = "Creative Commons Attribution 4.0 International"
		urlInput.element.value = "https://creativecommons.org/licenses/by/4.0/"
		schemeUri.element.value = "https://spdx.org/licenses/"
		rightsIdentifier.element.value = "CC-BY-4.0"
		rightsIdentifierScheme.element.value = "SPDX"
		lang.element.value = "eng"
		statementInput.validate()
		urlInput.validate()
		rightsIdentifier.validate()
		_rights = _rights.copy(
			statementInput.element.value,
			Some(urlInput.element.value),
			Some(schemeUri.element.value),
			Some(rightsIdentifier.element.value),
			Some(rightsIdentifierScheme.element.value),
			Some(lang.element.value)
		)
		updateCb(_rights)
	}

	private[this] val ccZeroButton = button(
		cls := "btn btn-secondary",
		tpe := "button",
		onclick := (autofillCcZero _)
	)("CC0-1.0").render

	private def autofillCcZero(): Unit = {
		statementInput.element.value = "Creative Commons Zero v1.0 Universal"
		urlInput.element.value = "https://creativecommons.org/publicdomain/zero/1.0/legalcode"
		schemeUri.element.value = "https://spdx.org/licenses/"
		rightsIdentifier.element.value = "CC0-1.0"
		rightsIdentifierScheme.element.value = "SPDX"
		lang.element.value = "eng"
		statementInput.validate()
		urlInput.validate()
		rightsIdentifier.validate()
		_rights = _rights.copy(
			statementInput.element.value,
			Some(urlInput.element.value),
			Some(schemeUri.element.value),
			Some(rightsIdentifier.element.value),
			Some(rightsIdentifierScheme.element.value),
			Some(lang.element.value)
		)
		updateCb(_rights)
	}

	private[this] val statementInput = new TextInputWidget(init.rights, rs => {
		_rights = _rights.copy(rights = rs)
		updateCb(_rights)
	}, "License name", required = true)

	private[this] val urlInput = new TextInputWidget(init.rightsUri.getOrElse(""), uri => {
		val uriOpt = if (uri.isEmpty) None else Some(uri)
		_rights = _rights.copy(rightsUri = uriOpt)
		updateCb(_rights)
	}, "License URI", required = true)

	private[this] val schemeUri = new TextInputWidget(init.schemeUri.getOrElse(""), scheme => {
		val schemeOpt = if (scheme.isEmpty) None else Some(scheme)
		_rights = _rights.copy(schemeUri = schemeOpt)
		updateCb(_rights)
	}, "Scheme URI", required = false)

	private[this] val rightsIdentifier = new TextInputWidget(init.rightsIdentifier.getOrElse(""), id => {
		val idOpt = if (id.isEmpty) None else Some(id)
		_rights = _rights.copy(rightsIdentifier = idOpt)
		updateCb(_rights)
	}, "License id", required = true) // TODO: change to required false by mapping the value if empty

	private[this] val rightsIdentifierScheme = new TextInputWidget(init.rightsIdentifierScheme.getOrElse(""), idScheme => {
		val idSchemeOpt = if (idScheme.isEmpty) None else Some(idScheme)
		_rights = _rights.copy(rightsIdentifierScheme = idSchemeOpt)
		updateCb(_rights)
	}, "License id scheme", required = false)

	private[this] val lang = new TextInputWidget(init.lang.getOrElse(""), l => {
		val lOpt = if (l.isEmpty) None else Some(l)
		_rights = _rights.copy(lang = lOpt)
		updateCb(_rights)
	}, "Language", required = false)

	val element = div(cls := "row spacyrow")(
		div(cls := "col-md-2")(strong("Autofill")),
		div(cls := "col-md-10")(ccBy4Button, ccZeroButton)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("License name")),
		div(cls := "col-md-10")(statementInput.element)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("License URI")),
		div(cls := "col-md-4")(urlInput.element)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("License id")),
		div(cls := "col-md-4")(rightsIdentifier.element)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("License id scheme")),
		div(cls := "col-md-4")(rightsIdentifierScheme.element)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("Scheme uri")),
		div(cls := "col-md-4")(schemeUri.element)(paddingBottom := 15),
		div(cls := "col-md-2")(strong("Language")),
		div(cls := "col-md-4")(lang.element)(paddingBottom := 15)
	).render
}
