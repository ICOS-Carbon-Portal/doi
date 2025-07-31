package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Rights
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget

class RightsWidget(init: Rights, protected val updateCb: Rights => Unit) extends EntityWidget[Rights] {
	private[this] var _rights = init

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

	private[this] val languageInput = new SelectWidget[String](
		TitleWidget.languageOptions,
		init.lang,
		langOpt => {
			_rights = _rights.copy(lang = langOpt)
			updateCb(_rights)
		}
	)

	val element = div(cls := "row spacyrow")(
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
		div(cls := "col-md-4")(languageInput.element)(paddingBottom := 15)
	).render
}
