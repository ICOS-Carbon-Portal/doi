package se.lu.nateko.cp.doi.gui.widgets

import se.lu.nateko.cp.doi.meta.Version
import org.scalajs.dom.html.Element
import se.lu.nateko.cp.doi.gui.widgets.generic.EntityWidget
import se.lu.nateko.cp.doi.gui.widgets.generic.TextInputWidget

class VersionWidget(init: Option[Version], protected val updateCb: Option[Version] => Unit) extends EntityWidget[Option[Version]] {

	import VersionWidget.versionRegex

	val element: Element = new TextInputWidget(init.map(_.toString).getOrElse(""), _ match {
		case "" =>
			updateCb(None)
			highlightError(element, None)

		case versionRegex(majorStr, minorStr) =>
			val version = Version(majorStr.toInt, minorStr.toInt)
			updateCb(Some(version))
			highlightError(element, version.error)

		case _ =>
			updateCb(Some(Version(-1, -1)))
			highlightError(element, Some("Bad version format, try N[N].N[N]"))
	}, "N[N].N[N]", required = true).element

}

object VersionWidget{
	private val versionRegex = """^(\d+)\.(\d+)$""".r
}
