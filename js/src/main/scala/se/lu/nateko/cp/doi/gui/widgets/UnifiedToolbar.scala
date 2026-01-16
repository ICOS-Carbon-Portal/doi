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

	private def stateDotColor = _meta.state match {
		case DoiPublicationState.draft => "#ffc107"
		case DoiPublicationState.registered => "#0d6efd"
		case DoiPublicationState.findable => "#198754"
	}

	private def createStateDot() = span(
		style := s"display: inline-block; width: 8px; height: 8px; border-radius: 50%; background-color: $stateDotColor; margin-right: 6px;"
	).render

	private val stateDropdownButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-outline-secondary dropdown-toggle",
		data("bs-toggle") := "dropdown",
		aria.expanded := "false"
	)(
		createStateDot(),
		_meta.state.toString.capitalize
	).render

	private def createStateMenuItem(action: String, description: String, isCurrent: Boolean, isDisabled: Boolean = false) = {
		if (isCurrent) {
			None
		} else {
			val itemClass = if (isDisabled) "dropdown-item disabled" else "dropdown-item"
			Some(li(a(
				cls := itemClass,
				href := "#",
				data("action") := action
			)(
				div(
					div(cls := "fw-bold")(action),
					div(cls := "small text-muted")(description)
				)
			).render))
		}
	}

	private val stateDropdownMenu = {
		val items = _meta.state match {
			case DoiPublicationState.draft =>
				Seq(
					createStateMenuItem("Register", "Set state to Registered", isCurrent = false),
					createStateMenuItem("Publish", "Set state to Findable", isCurrent = false)
				).flatten
			case DoiPublicationState.registered =>
				Seq(
					createStateMenuItem("Register", "Set state to Registered", isCurrent = true),
					createStateMenuItem("Publish", "Set state to Findable", isCurrent = false)
				).flatten
			case DoiPublicationState.findable =>
				Seq(
					createStateMenuItem("Hide", "Set state to Registered", isCurrent = false),
					createStateMenuItem("Publish", "Set state to Findable", isCurrent = true)
				).flatten
		}
		ul(
			cls := "dropdown-menu state-dropdown-menu",
			style := "min-width: 16rem;"
		)(items: _*).render
	}

	private val stateDropdown = div(
		cls := "dropdown admin-control"
	)(
		stateDropdownButton,
		stateDropdownMenu
	).render

	// Toggle state dropdown manually
	stateDropdownButton.onclick = (_: Event) => {
		stateDropdownMenu.classList.toggle("show")
	}

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
		i(cls := "fa-solid fa-floppy-disk me-1"),
		"Save"
	).render


	private val submitButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-secondary btn-submit edit-control"
	)("Submit for publication").render

	// Delete button
	private val deleteButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-outline-secondary admin-control edit-control"
	)(
		i(cls := "fa-solid fa-trash me-1"),
		"Delete draft"
	).render

	// Action button groups based on state
	private val actionButtons: Div = _meta.state match {
		case DoiPublicationState.draft =>
			div(cls := "d-flex gap-2")(
				button(
					tpe := "button",
					cls := "btn btn-sm btn-secondary btn-submit edit-control"
				)("Submit for publication").render,
				deleteButton,
				updateButton
			).render

	case DoiPublicationState.registered =>
			div(cls := "d-flex gap-2")(
				updateButton
			).render
	case _ =>
			div(cls := "d-flex gap-2")(
				updateButton
			).render
	}

	// Main toolbar structure with custom dropdown styles
	val element: Div = div(
		id := "unified-toolbar",
		cls := "border-bottom py-2 mb-3"
	)(
		tag("style")("""
			.state-dropdown-menu {
				display: none;
				position: absolute;
				min-width: 10rem;
				padding: 0.5rem 0;
				margin: 0.125rem 0 0;
				background-color: #fff;
				border: 1px solid rgba(0,0,0,.15);
				border-radius: 0.25rem;
				box-shadow: 0 0.5rem 1rem rgba(0,0,0,.175);
				list-style: none;
				z-index: 1000;
			}
			.state-dropdown-menu.show {
				display: block;
			}
			.state-dropdown-menu .dropdown-item {
				display: block;
				width: 100%;
				padding: 0.5rem 1rem;
				clear: both;
				font-weight: 400;
				color: #212529;
				text-align: inherit;
				white-space: normal;
				background-color: transparent;
				border: 0;
				cursor: pointer;
				text-decoration: none;
			}
			.state-dropdown-menu .dropdown-item:hover:not(.disabled) {
				background-color: #f8f9fa;
			}
			.state-dropdown-menu .dropdown-item.disabled {
				color: #6c757d;
				cursor: not-allowed;
				opacity: 0.6;
			}
			.state-dropdown-menu .dropdown-item-text {
				display: block;
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
			
			stateDropdown,
			cloneButton,
			actionButtons
		),
		// Inline style for sticky positioning with white background
		style := "position: sticky; top: 0; z-index: 1000; background-color: white;"
	).render
	
	// Close state dropdown when clicking outside
	org.scalajs.dom.document.addEventListener("click", (e: Event) => {
		val target = e.target.asInstanceOf[org.scalajs.dom.Node]
		// Check if click is outside state dropdown by traversing parents
		var node = target
		var isInside = false
		while (node != null && !isInside) {
			if (node.isInstanceOf[org.scalajs.dom.Element]) {
				val elem = node.asInstanceOf[org.scalajs.dom.Element]
				if (elem.classList.contains("dropdown")) {
					isInside = true
				}
			}
			node = node.parentNode
		}
		if (!isInside) {
			val dropdowns = org.scalajs.dom.document.querySelectorAll(".state-dropdown-menu.show")
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
		updateButton.className = "btn btn-sm btn-update-doi edit-control btn-" + (if(enabled) "primary" else "secondary")
	}

	def setSubmitButtonEnabled(enabled: Boolean): Unit = {
		submitButton.disabled = !enabled
	}

	def setUpdateButtonCallback(cb: Event => Unit): Unit = {
		updateButton.onclick = cb
	}

	def setSubmitButtonCallback(cb: Event => Unit): Unit = {
		submitButton.onclick = cb
	}

	def setDeleteButtonCallback(cb: Event => Unit): Unit = {
		deleteButton.onclick = cb
	}

	def updateBadge(state: DoiPublicationState): Unit = {
		_meta = _meta.copy(state = state)
		stateDropdownButton.innerHTML = ""
		stateDropdownButton.appendChild(createStateDot())
		stateDropdownButton.appendChild(org.scalajs.dom.document.createTextNode(state.toString.capitalize))
	}

	def setStateChangeCallback(cb: DoiPublicationState => Unit): Unit = {
		val items = stateDropdownMenu.querySelectorAll(".dropdown-item")
		for (i <- 0 until items.length) {
			val item = items(i).asInstanceOf[org.scalajs.dom.html.Anchor]
			val action = item.getAttribute("data-action")
			item.onclick = (e: Event) => {
				e.preventDefault()
				if (!item.classList.contains("disabled")) {
					stateDropdownMenu.classList.remove("show")
					val newState = action.toLowerCase match {
						case "register" => DoiPublicationState.registered
						case "publish" => DoiPublicationState.findable
						case "hide" => DoiPublicationState.registered
						case _ => _meta.state
					}
					if (newState != _meta.state) {
						cb(newState)
					}
				}
			}
		}
	}
}
