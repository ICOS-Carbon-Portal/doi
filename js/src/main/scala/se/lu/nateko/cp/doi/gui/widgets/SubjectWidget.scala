package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.meta.Subject
import scalatags.JsDom.all._
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectOption
import se.lu.nateko.cp.doi.gui.widgets.generic.SelectWidget

class SubjectWidget(init: Subject, protected val updateCb: Subject => Unit) extends EntityWidget[Subject]{

	private[this] var _subj = init

	private[this] val subjInput = new TextInputWidget(init.subject, subj => {
		_subj = _subj.copy(subject = subj)
		updateCb(_subj)
	}, required = true)

	val element =
		subjInput.element.render
}
