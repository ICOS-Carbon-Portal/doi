package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import org.scalajs.dom.html.{Button, Div}
import scala.concurrent.Future
import scala.scalajs.js.timers.{setTimeout, clearTimeout}

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
	private[this] var successTimeoutHandle: Option[scala.scalajs.js.timers.SetTimeoutHandle] = None

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

	private val tocButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-outline-secondary toc-toggle-button",
		style := s"display: ${if (initialTab == EditorTab.edit) "block" else "none"};"
	)(
		i(cls := "fa-solid fa-list me-1")
	).render

	private val tocContent = ul(cls := "list-unstyled mb-0")(
		li(cls := "toc-section")(
			a(href := "#toc-required", cls := "toc-link toc-section-link")("Required properties")
		),
		li(cls := "toc-item")(
			a(href := "#toc-doi-target", cls := "toc-link")("DOI target")
		),
		li(cls := "toc-item")(
			a(href := "#toc-creators", cls := "toc-link")("Creators")
		),
		li(cls := "toc-item")(
			a(href := "#toc-titles", cls := "toc-link")("Titles")
		),
		li(cls := "toc-item")(
			a(href := "#toc-publisher", cls := "toc-link")("Publisher")
		),
		li(cls := "toc-item")(
			a(href := "#toc-publication-year", cls := "toc-link")("Publication year")
		),
		li(cls := "toc-item")(
			a(href := "#toc-resource-type", cls := "toc-link")("Resource type")
		),
		li(cls := "toc-section mt-3")(
			a(href := "#toc-recommended", cls := "toc-link toc-section-link")("Recommended properties")
		),
		li(cls := "toc-item")(
			a(href := "#toc-subjects", cls := "toc-link")("Subjects")
		),
		li(cls := "toc-item")(
			a(href := "#toc-contributors", cls := "toc-link")("Contributors")
		),
		li(cls := "toc-item")(
			a(href := "#toc-dates", cls := "toc-link")("Dates")
		),
		li(cls := "toc-item")(
			a(href := "#toc-related-identifiers", cls := "toc-link")("Related identifiers")
		),
		li(cls := "toc-item")(
			a(href := "#toc-rights", cls := "toc-link")("Rights")
		),
		li(cls := "toc-item")(
			a(href := "#toc-descriptions", cls := "toc-link")("Descriptions")
		),
		li(cls := "toc-item")(
			a(href := "#toc-geolocations", cls := "toc-link")("Geolocations")
		),
		li(cls := "toc-section mt-3")(
			a(href := "#toc-optional", cls := "toc-link toc-section-link")("Optional properties")
		),
		li(cls := "toc-item")(
			a(href := "#toc-formats", cls := "toc-link")("Formats")
		),
		li(cls := "toc-item")(
			a(href := "#toc-version", cls := "toc-link")("Version")
		),
		li(cls := "toc-item")(
			a(href := "#toc-funding", cls := "toc-link")("Funding references")
		)
	).render

	private val tocPanel = div(
		id := "toc-panel",
		cls := "toc-panel"
	)(
		div(cls := "toc-header d-flex justify-content-between align-items-center")(
			strong("Contents"),
			button(
				tpe := "button",
				cls := "btn-close btn-sm",
				aria.label := "Close"
			).render
		),
		div(cls := "toc-body")(
			tocContent
		)
	).render

	private val tocButtonContainer = div(cls := "toc-button-container")(
		tocButton,
		tocPanel
	).render

	tocButton.onclick = (_: Event) => {
		tocPanel.classList.toggle("show")
	}

	private val tocCloseButton = tocPanel.querySelector(".btn-close").asInstanceOf[Button]
	tocCloseButton.onclick = (_: Event) => {
		tocPanel.classList.remove("show")
	}

	private def setupTOCLinks(): Unit = {
		val links = tocPanel.querySelectorAll(".toc-link")
		for (i <- 0 until links.length) {
			val link = links(i).asInstanceOf[org.scalajs.dom.html.Anchor]
			link.onclick = (e: Event) => {
				e.preventDefault()
				val href = link.getAttribute("href")
				if (href != null && href.startsWith("#")) {
					val targetId = href.substring(1)
					val targetElement = org.scalajs.dom.document.getElementById(targetId)
					if (targetElement != null) {
						targetElement.asInstanceOf[scala.scalajs.js.Dynamic].scrollIntoView(
							scala.scalajs.js.Dynamic.literal(
								behavior = "smooth",
								block = "start"
							)
						)
						tocPanel.classList.remove("show")
					}
				}
			}
		}
	}

	setupTOCLinks()

	private def updateTabButtons(): Unit = {
		viewButton.className = s"btn btn-sm${if (currentTab == EditorTab.view) " btn-secondary" else " btn-outline-secondary"}"
		editButton.className = s"btn btn-sm${if (currentTab == EditorTab.edit) " btn-secondary" else " btn-outline-secondary"} edit-control"
		jsonButton.className = s"btn btn-sm${if (currentTab == EditorTab.json) " btn-secondary" else " btn-outline-secondary"} edit-control"

		tocButton.style.display = if (currentTab == EditorTab.edit) "block" else "none"
		tocPanel.classList.remove("show")
	}

	private def stateDotClass = _meta.state match {
		case DoiPublicationState.draft => "state-dot state-dot-draft"
		case DoiPublicationState.registered => "state-dot state-dot-registered"
		case DoiPublicationState.findable => "state-dot state-dot-findable"
	}

	private def createStateDot() = span(
		cls := stateDotClass
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
	cloneButton.onclick = (_: Event) => cloneCb(_meta)

	// Action buttons (edit mode)
	private val updateButtonIcon = i(cls := "fa-solid fa-floppy-disk me-1").render
	private val updateButtonText = span("Save").render
	private val updateButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-update-doi btn-secondary edit-control",
		disabled := true
	)(
		updateButtonIcon,
		updateButtonText
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
	deleteButton.onclick = (_: Event) => deleteCb(_meta.doi)

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

	val element = div(
		id := "unified-toolbar",
		cls := "unified-toolbar border-bottom py-2 mb-3",
		style := "position: relative;"
	)(
		div(cls := "d-flex flex-wrap align-items-center gap-2")(
			div(cls := "me-2")(backButton),

			div(cls := "btn-group me-2 admin-control")(
				viewButton,
				editButton,
				jsonButton
			),

			div(cls := "flex-grow-1"),

			stateDropdown,
			cloneButton,
			actionButtons
		),
		tocButtonContainer
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

	def getTocPanel: org.scalajs.dom.html.Div = tocPanel

	def updateTocButtonPosition(): Unit = {
		val toolbarElement = org.scalajs.dom.document.getElementById("unified-toolbar")
		if (toolbarElement != null) {
			val toolbarHeight = toolbarElement.getBoundingClientRect().height
			tocButtonContainer.style.top = s"${toolbarHeight + 30}px"
		}
	}

	def setTab(tab: EditorTab): Unit = {
		currentTab = tab
		updateTabButtons()
	}

	def setUpdateButtonEnabled(enabled: Boolean): Unit = {
		if (enabled) {
			// Clear any pending success animation timeout since we have new changes
			successTimeoutHandle.foreach(clearTimeout)
			successTimeoutHandle = None

			// Reset button icon and text from success state to normal save state
			updateButtonIcon.className = "fa-solid fa-floppy-disk me-1"
			updateButtonText.textContent = "Save"
		}

		updateButton.disabled = !enabled
		updateButton.className = "btn btn-sm btn-update-doi edit-control btn-" + (if(enabled) "primary" else "secondary")
	}

	def showSaveSuccess(): Unit = {
		// Clear any existing timeout
		successTimeoutHandle.foreach(clearTimeout)

		// Change button to success state
		updateButton.disabled = true
		updateButton.className = "btn btn-sm btn-update-doi edit-control btn-success"
		updateButtonIcon.className = "fa-solid fa-check me-1"
		updateButtonText.textContent = "Saved!"

		// Revert to normal state after 3 seconds
		val handle = setTimeout(3000) {
			updateButton.className = "btn btn-sm btn-update-doi edit-control btn-secondary"
			updateButtonIcon.className = "fa-solid fa-floppy-disk me-1"
			updateButtonText.textContent = "Save"
			successTimeoutHandle = None
		}
		successTimeoutHandle = Some(handle)
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
