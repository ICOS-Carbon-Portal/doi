package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.meta.Person
import se.lu.nateko.cp.doi.gui.widgets.EditorTab
import se.lu.nateko.cp.doi.gui.widgets.TabWidget

class DoiMetaViewer(meta: DoiMeta, tabsCb: Map[EditorTab.Value, () => Unit], cloneCb: DoiMeta => Unit) {

	private val tabs = new TabWidget(EditorTab.view, tabsCb).element
	private val titles = meta.titles.fold[Seq[String]](Seq())(_.map(_.title).toSeq).map(h2(cls := "fs-3")(_))
	private val subtitleProperties =
		Seq(
			meta.types.map(t => Seq(t.resourceTypeGeneral, t.resourceType).filter(_.nonEmpty).flatten.mkString(" / ")),
			meta.version.map(version => s"version $version"),
			meta.publicationYear.map(year => s"published in $year"),
			meta.publisher.map(publisher => s"by $publisher")
		).filter(_.nonEmpty).flatten
	private val subtitle = p(cls := "text-muted")(subtitleProperties.mkString(" "))
	private val description = meta.descriptions.map(d => p(d.description)).toSeq
	private val subjects = p(meta.subjects.map(subject => span(cls := "badge rounded-pill bg-secondary me-1")(subject.subject)).toSeq)
	private val creators = div(listPeople(meta.creators.toSeq, "Creators"))
	private val contributors = div(listPeople(meta.contributors.toSeq, "Contributors"))
	private val dates = p(meta.dates.map(d => div(s"${d.dateType}: ${d.date}")).toSeq)
	private val doiUrl = "https://doi.org/" + meta.doi
	private val doiLink = p(a(href := doiUrl, target := "_blank")(doiUrl))

	private val cloneButton = button(tpe := "button", cls := "btn btn-secondary edit-control")("Clone").render
	cloneButton.onclick = (_: Event) => cloneCb(meta)

	val element = div(
		tabs,
		titles,
		subtitle,
		description,
		subjects,
		p(
			creators,
			contributors
		),
		dates,
		doiLink,
		cloneButton
	).render

	private def listPeople(list: Seq[Person], label: String): Option[String] = {
		Option(list.map(c => c.name.toString))
			.filter(_.nonEmpty)
			.map(_.mkString(label + ": ", ", ", ""))
	}

}