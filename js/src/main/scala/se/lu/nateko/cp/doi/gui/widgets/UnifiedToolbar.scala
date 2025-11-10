package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import org.scalajs.dom.html.{Button, Div}
import scala.concurrent.Future

class UnifiedToolbar(
	meta: DoiMeta,
	backToListCb: Event => Unit,
	tabsCb: Map[EditorTab, () => Unit],
	cloneCb: DoiMeta => Unit,
	updater: DoiMeta => Future[Unit],
	deleteCb: Doi => Unit,
	initialTab: EditorTab = EditorTab.view
) {

	private[this] var _meta = meta
	private[this] var currentTab: EditorTab = initialTab

	// Back to list button
	private val backButton = a(
		href := "/",
		cls := "btn btn-sm btn-outline-secondary",
		onclick := backToListCb
	)(
		span(cls := "fas fa-arrow-left me-2"),
		"Back to list"
	).render

	// Tab buttons
	private def makeTabButton(tab: EditorTab, label: String): Button = {
		val btn = button(
			tpe := "button",
			cls := s"btn btn-sm${if (tab == currentTab) " btn-secondary" else " btn-outline-secondary"}",
			onclick := { (_: Event) =>
				currentTab = tab
				updateTabButtons()
				tabsCb(tab)()
			}
		)(label).render
		btn
	}

	private val viewButton = makeTabButton(EditorTab.view, "View")
	private val editButton = makeTabButton(EditorTab.edit, "Edit")
	private val jsonButton = makeTabButton(EditorTab.json, "Edit as JSON")

	private def updateTabButtons(): Unit = {
		viewButton.className = s"btn btn-sm${if (currentTab == EditorTab.view) " btn-secondary" else " btn-outline-secondary"}"
		editButton.className = s"btn btn-sm${if (currentTab == EditorTab.edit) " btn-secondary" else " btn-outline-secondary"} edit-control"
		jsonButton.className = s"btn btn-sm${if (currentTab == EditorTab.json) " btn-secondary" else " btn-outline-secondary"} edit-control"
	}

	// State badge
	private def badgeClasses = "badge " + (_meta.state match {
		case DoiPublicationState.draft => "bg-warning text-dark"
		case DoiPublicationState.registered => "bg-primary"
		case DoiPublicationState.findable => "bg-success"
	})

	private val badgeSpan = span(cls := badgeClasses)(_meta.state.toString.capitalize).render

	// Clone button
	private val cloneButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-outline-secondary edit-control"
	)(
		i(cls := "fa-solid fa-copy me-1"),
		"Clone"
	).render
	cloneButton.onclick = (_: Event) => cloneCb(meta)

	// Action buttons (edit mode)
	private val updateButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-update-doi btn-secondary edit-control",
		disabled := true
	)(
		i(cls := "fa-solid fa-arrows-rotate me-1"),
		"Update"
	).render

	private val updateDropdownToggle = {
		val baseAttrs = Seq(
			tpe := "button",
			cls := s"btn btn-sm btn-update-doi btn-secondary dropdown-toggle dropdown-toggle-split${if (_meta.state != DoiPublicationState.draft) " edit-control" else ""}"
		)
		val attrs = if (_meta.state != DoiPublicationState.draft) baseAttrs :+ (disabled := true) else baseAttrs
		button(attrs: _*)(
			span(cls := "visually-hidden")("Toggle Dropdown")
		).render
	}
	
	// Toggle dropdown menu visibility
	updateDropdownToggle.onclick = (_: Event) => {
		val menu = updateDropdownToggle.nextElementSibling
		if (menu != null && menu.classList.contains("dropdown-menu")) {
			menu.classList.toggle("show")
		}
	}

	private val submitButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-secondary btn-submit edit-control"
	)("Submit for publication").render

	// Dropdown menu items for split button
	private val resetDropdownItem = button(
		tpe := "button",
		cls := "dropdown-item edit-control",
		disabled := true
	)("Reset").render

	private val deleteDropdownItem = button(
		tpe := "button",
		cls := "dropdown-item admin-control edit-control"
	)("Delete").render

	private val publishDropdownItem = button(
		tpe := "button",
		cls := "dropdown-item admin-control edit-control"
	)("Publish").render
	
	// Close dropdown menu
	private def closeDropdown(menu: org.scalajs.dom.Element): Unit = {
		if (menu != null && menu.classList.contains("dropdown-menu")) {
			menu.classList.remove("show")
		}
	}

	// Action button groups based on state
	private val actionButtons: Div = _meta.state match {
		case DoiPublicationState.draft =>
			div(cls := "d-flex gap-2")(
				button(
					tpe := "button",
					cls := "btn btn-sm btn-secondary btn-submit edit-control"
				)("Submit for publication").render,
				div(cls := "btn-group doi-split-button", style := "position: relative;")(
					updateButton,
					updateDropdownToggle,
					ul(cls := "dropdown-menu", style := "position: absolute; top: 100%; right: 0; z-index: 1000;")(
						li(resetDropdownItem),
						li(deleteDropdownItem),
						li(publishDropdownItem)
					)
				)
			).render
		case _ =>
			div(cls := "btn-group doi-split-button", style := "position: relative;")(
				updateButton,
				updateDropdownToggle,
				ul(cls := "dropdown-menu", style := "position: absolute; top: 100%; right: 0; z-index: 1000;")(
					li(resetDropdownItem)
				)
			).render
	}

	// Main toolbar structure with custom dropdown styles
	val element: Div = div(
		id := "unified-toolbar",
		cls := "border-bottom py-2 mb-3"
	)(
		tag("style")("""
			.doi-split-button .dropdown-menu {
				display: none;
				min-width: 10rem;
				padding: 0.5rem 0;
				margin: 0.125rem 0 0;
				background-color: #fff;
				border: 1px solid rgba(0,0,0,.15);
				border-radius: 0.25rem;
				box-shadow: 0 0.5rem 1rem rgba(0,0,0,.175);
				list-style: none;
			}
			.doi-split-button .dropdown-menu.show {
				display: block;
			}
			.doi-split-button .dropdown-item {
				display: block;
				width: 100%;
				padding: 0.25rem 1rem;
				clear: both;
				font-weight: 400;
				color: #212529;
				text-align: inherit;
				white-space: nowrap;
				background-color: transparent;
				border: 0;
				cursor: pointer;
			}
			.doi-split-button .dropdown-item:hover:not(:disabled) {
				background-color: #f8f9fa;
			}
			.doi-split-button .dropdown-item:disabled {
				color: #6c757d;
				cursor: not-allowed;
				opacity: 0.5;
			}
			.doi-split-button .dropdown-toggle-split::after {
				margin-left: 0;
			}
		"""),
		div(cls := "d-flex flex-wrap align-items-center gap-2")(
			// Left section: Back button
			div(cls := "me-2")(backButton),
			
			// Tab buttons
			div(cls := "btn-group me-2 admin-control")(
				viewButton,
				editButton,
				jsonButton
			),
			
			// Spacer to push action buttons to the right
			div(cls := "flex-grow-1"),
			
			div(cls := "me-2")(badgeSpan),

			div(cls := "me-2")(cloneButton),
			
			// Action buttons
			div(cls := "ms-auto")(actionButtons)
		),
		// Inline style for sticky positioning with white background
		style := "position: sticky; top: 0; z-index: 1000; background-color: white;"
	).render
	
	// Close dropdown when clicking outside
	org.scalajs.dom.document.addEventListener("click", (e: Event) => {
		val target = e.target.asInstanceOf[org.scalajs.dom.Node]
		// Check if click is outside split button by traversing parents
		var node = target
		var isInside = false
		while (node != null && !isInside) {
			if (node.isInstanceOf[org.scalajs.dom.Element]) {
				val elem = node.asInstanceOf[org.scalajs.dom.Element]
				if (elem.classList.contains("doi-split-button")) {
					isInside = true
				}
			}
			node = node.parentNode
		}
		if (!isInside) {
			val dropdowns = org.scalajs.dom.document.querySelectorAll(".doi-split-button .dropdown-menu.show")
			for (i <- 0 until dropdowns.length) {
				dropdowns(i).classList.remove("show")
			}
		}
	})

	// Public methods to control the toolbar from outside
	def setTab(tab: EditorTab): Unit = {
		currentTab = tab
		updateTabButtons()
	}

	def setUpdateButtonEnabled(enabled: Boolean): Unit = {
		updateButton.disabled = !enabled
		// For draft DOIs, keep dropdown toggle always enabled to allow access to Delete/Publish
		val dropdownEnabled = enabled || _meta.state == DoiPublicationState.draft
		updateDropdownToggle.disabled = !dropdownEnabled
		updateButton.className = "btn btn-sm btn-update-doi edit-control btn-" + (if(enabled) "primary" else "secondary")
		updateDropdownToggle.className = "btn btn-sm btn-update-doi dropdown-toggle dropdown-toggle-split edit-control btn-" + (if(enabled) "primary" else "secondary")
	}

	def setResetButtonEnabled(enabled: Boolean): Unit = {
		resetDropdownItem.disabled = !enabled
	}

	def setPublishButtonEnabled(enabled: Boolean): Unit = {
		publishDropdownItem.disabled = !enabled
	}

	def setSubmitButtonEnabled(enabled: Boolean): Unit = {
		submitButton.disabled = !enabled
	}

	def setUpdateButtonCallback(cb: Event => Unit): Unit = {
		updateButton.onclick = cb
	}

	def setResetButtonCallback(cb: Event => Unit): Unit = {
		resetDropdownItem.onclick = (e: Event) => {
			closeDropdown(resetDropdownItem.parentElement.parentElement)
			cb(e)
		}
	}

	def setPublishButtonCallback(cb: Event => Unit): Unit = {
		publishDropdownItem.onclick = (e: Event) => {
			closeDropdown(publishDropdownItem.parentElement.parentElement)
			cb(e)
		}
	}

	def setSubmitButtonCallback(cb: Event => Unit): Unit = {
		submitButton.onclick = cb
	}

	def setDeleteButtonCallback(cb: Event => Unit): Unit = {
		deleteDropdownItem.onclick = (e: Event) => {
			closeDropdown(deleteDropdownItem.parentElement.parentElement)
			cb(e)
		}
	}

	def updateBadge(state: DoiPublicationState): Unit = {
		_meta = _meta.copy(state = state)
		badgeSpan.className = badgeClasses
		badgeSpan.textContent = state.toString.capitalize
	}
}
