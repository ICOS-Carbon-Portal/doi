package se.lu.nateko.cp.doi.gui.widgets

import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.meta.DoiPublicationState
import se.lu.nateko.cp.doi.gui.UserInfo
import se.lu.nateko.cp.doi.gui.views.DoiMetaHelpers
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
	initialTab: EditorTab = EditorTab.view,
	userInfo: UserInfo = UserInfo(isLoggedIn = false, isAdmin = false, canEdit = false)
) {

	private[this] var _meta = meta
	private[this] var currentTab: EditorTab = initialTab
	private[this] var successTimeoutHandle: Option[scala.scalajs.js.timers.SetTimeoutHandle] = None

	private val backButton = a(
		href := "/",
		cls := "btn btn-sm btn-outline-secondary",
		onclick := backToListCb
	)(
		span(cls := "fas fa-arrow-left me-2"),
		"Back to list"
	).render

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
		editButton.className = s"btn btn-sm${if (currentTab == EditorTab.edit) " btn-secondary" else " btn-outline-secondary"}"
		jsonButton.className = s"btn btn-sm${if (currentTab == EditorTab.json) " btn-secondary" else " btn-outline-secondary"}"
	}

	private def createStateDot() = span(
		cls := DoiMetaHelpers.stateDotClass(_meta.state)
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

	private val stateDisplay = span(
		cls := "d-inline-flex align-items-center gap-1 px-2 py-1 text-secondary"
	)(
		createStateDot(),
		_meta.state.toString.capitalize
	).render

	stateDropdownButton.onclick = (_: Event) => {
		stateDropdownMenu.classList.toggle("show")
	}

	private val cloneButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-outline-secondary"
	)(
		i(cls := "fa-solid fa-copy me-1"),
		"Clone"
	).render
	cloneButton.onclick = (_: Event) => cloneCb(_meta)

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

	private val submitButtonWrapper = span(
		cls := "d-inline-block",
		title := "Fix validation errors to enable submission",
		data("bs-toggle") := "tooltip",
		data("bs-placement") := "top"
	)(submitButton).render

	private val deleteButton = button(
		tpe := "button",
		cls := "btn btn-sm btn-outline-secondary admin-control edit-control"
	)(
		i(cls := "fa-solid fa-trash me-1"),
		"Delete draft"
	).render
	deleteButton.onclick = (_: Event) => deleteCb(_meta.doi)

	private val actionButtons: Div = _meta.state match {
		case DoiPublicationState.draft =>
			div(cls := "d-flex gap-2")(
				submitButtonWrapper,
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
		cls := "unified-toolbar border-bottom py-2 mb-3"
	)(
		div(cls := "d-flex flex-wrap align-items-center gap-2")(
			div(cls := "me-2")(backButton),

			div(cls := s"btn-group me-2${if (!userInfo.canEdit) " edit-control" else ""}")(
				viewButton,
				editButton,
				jsonButton
			),

			div(cls := "flex-grow-1"),

			if (userInfo.isAdmin) stateDropdown else stateDisplay,
			if (userInfo.isLoggedIn) cloneButton else span().render,
			actionButtons
		)
	).render

	org.scalajs.dom.document.addEventListener("click", (e: Event) => {
		val target = e.target.asInstanceOf[org.scalajs.dom.Node]
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

	def setTab(tab: EditorTab): Unit = {
		currentTab = tab
		updateTabButtons()
	}

	def setUpdateButtonEnabled(enabled: Boolean): Unit = {
		if (enabled) {
			successTimeoutHandle.foreach(clearTimeout)
			successTimeoutHandle = None

			updateButtonIcon.className = "fa-solid fa-floppy-disk me-1"
			updateButtonText.textContent = "Save"
		}

		updateButton.disabled = !enabled
		updateButton.className = "btn btn-sm btn-update-doi edit-control btn-" + (if(enabled) "primary" else "secondary")
	}

	def showSaveSuccess(): Unit = {
		successTimeoutHandle.foreach(clearTimeout)

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
		// Only show tooltip when button is disabled
		if (enabled) {
			submitButtonWrapper.removeAttribute("title")
			submitButtonWrapper.removeAttribute("data-bs-toggle")
		} else {
			submitButtonWrapper.setAttribute("title", "Fix validation errors to enable submission")
			submitButtonWrapper.setAttribute("data-bs-toggle", "tooltip")
		}
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
