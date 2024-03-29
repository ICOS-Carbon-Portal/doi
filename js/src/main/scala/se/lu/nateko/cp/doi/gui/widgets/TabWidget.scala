package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import org.scalajs.dom.Event

enum EditorTab:
	case view, edit, json


class TabWidget(activeTab: EditorTab, cb: Map[EditorTab, () => Unit]) {

	private def isActive(tab: EditorTab) = if (tab == activeTab) " active" else ""

	private val viewButton = button(tpe := "button", cls := "nav-link" ++ isActive(EditorTab.view))("View").render
	viewButton.onclick = (_: Event) => cb(EditorTab.view)()
	private val editButton = button(tpe := "button", cls := "nav-link" ++ isActive(EditorTab.edit))("Edit").render
	editButton.onclick = (_: Event) => cb(EditorTab.edit)()
	private val editJsonButton = button(tpe := "button", cls := "nav-link" ++ isActive(EditorTab.json))("Edit as JSON").render
	editJsonButton.onclick = (_: Event) => cb(EditorTab.json)()

	val element = p(cls := "nav-edit edit-control")(
		ul(cls := "nav nav-tabs")(
			li(cls := "nav-item")(
				viewButton
			),
			li(cls := "nav-item")(
				editButton
			),
			li(cls := "nav-item")(
				editJsonButton
			)
		)
	)
}
