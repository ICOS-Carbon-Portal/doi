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

	private[this] val lang = new TextInputWidget(init.lang.getOrElse(""), l => {
		val lOpt = if (l.isEmpty) None else Some(l)
		_rights = _rights.copy(lang = lOpt)
		updateCb(_rights)
	}, "Language", required = false)

	val element = div(cls := "row")(
		div(cls := "col-md-6")(statementInput.element),
		div(cls := "col-md-6")(urlInput.element),
		div(cls := "col-md-6")(schemeUri.element),
		div(cls := "col-md-6")(rightsIdentifier.element),
		div(cls := "col-md-6")(rightsIdentifierScheme.element),
		div(cls := "col-md-6")(lang.element)
	).render
}
