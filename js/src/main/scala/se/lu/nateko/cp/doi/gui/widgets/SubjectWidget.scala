package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.Subject
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectOption
import se.lu.nateko.cp.doi.meta.SubjectScheme
import se.lu.nateko.cp.doi.meta.SubjectScheme.Dewey
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget

class SubjectWidget(init: Subject, protected val updateCb: Subject => Unit) extends EntityWidget[Subject]{

	private[this] var _subj = init

	private[this] val subjInput = new TextInputWidget(init.subject, subj => {
		_subj = _subj.copy(subject = subj)
		updateCb(_subj)
	}, required = true)

	private[this] val schemeInput = new SelectWidget[SubjectScheme](
		SelectWidget.selectOptions(Some("Subject scheme"))(Dewey),
		init.scheme,
		schemeOpt => {
			_subj = _subj.copy(scheme = schemeOpt)
			updateCb(_subj)
		}
	)

	val element = div(cls := "row")(
		div(cls := "col-md-6")(subjInput.element),
		div(cls := "col-md-3")(schemeInput.element)
	).render
}
