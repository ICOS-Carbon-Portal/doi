package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.meta.Person
import se.lu.nateko.cp.doi.gui.widgets.EditorTab
import se.lu.nateko.cp.doi.gui.widgets.TabWidget

class DoiMetaViewer(meta: DoiMeta, tabsCb: Map[EditorTab, () => Unit], cloneCb: DoiMeta => Unit) {

	private val tabs = new TabWidget(EditorTab.view, tabsCb).element
	
	private val subtitleProperties =
		Seq(
			meta.types.map(t => Seq(t.resourceTypeGeneral, t.resourceType).filter(_.nonEmpty).flatten.mkString(" / ")),
			meta.version.map(version => s"version $version"),
			meta.publicationYear.map(year => s"published in $year"),
			meta.publisher.map(publisher => s"by $publisher")
		).filter(_.nonEmpty).flatten
	
	private val subtitle = div(cls := "text-muted mb-4")(
		span(subtitleProperties.mkString(" "))
	)
	
	private val description = if (meta.descriptions.nonEmpty) {
		Seq(
			h5(cls := "mt-4 mb-3")("Description"),
			div(cls := "mb-3")(
				meta.descriptions.map(d => p(cls := "mb-2")(d.description)).toSeq
			)
		)
	} else Seq()
	
	private val subjects = if (meta.subjects.nonEmpty) {
		Seq(
			h5(cls := "mt-4 mb-3")("Subjects"),
			div(cls := "mb-3")(meta.subjects.map(subject => 
				span(cls := "badge rounded-pill bg-secondary me-2 mb-2")(subject.subject)
			).toSeq)
		)
	} else Seq()
	
	private val creators = listPeople(meta.creators.toSeq, "Creators").map { creatorText =>
		Seq(
			h5(cls := "mt-4 mb-3")("Creators"),
			div(cls := "mb-3")(
				span(creatorText.replaceFirst("Creators: ", ""))
			)
		)
	}.getOrElse(Seq())
	
	private val contributors = listPeople(meta.contributors.toSeq, "Contributors").map { contributorText =>
		Seq(
			h5(cls := "mt-4 mb-3")("Contributors"),
			div(cls := "mb-3")(
				span(contributorText.replaceFirst("Contributors: ", ""))
			)
		)
	}.getOrElse(Seq())
	
	private val doiUrl = "https://doi.org/" + meta.doi
	private val dataciteUrl = "https://commons.datacite.org/doi.org/" + meta.doi
	private val fabricaUrl = "https://doi.datacite.org/doi.org/" + meta.doi
	
	private val links = div(cls := "mb-4")(
		div(cls := "mb-2")(
			a(href := doiUrl, target := "_blank", cls := "text-decoration-none")(doiUrl)
		),
		div(cls := "mb-2")(
			a(href := dataciteUrl, target := "_blank", cls := "text-decoration-none")("Commons")
		),
		div(
			a(href := fabricaUrl, target := "_blank", cls := "text-decoration-none")("Fabrica")
		)
	)

	private val cloneButton = button(
		tpe := "button", 
		cls := "btn btn-primary edit-control mt-4"
	)(
		i(cls := "fa-solid fa-copy me-2"),
		"Clone DOI Metadata"
	).render
	cloneButton.onclick = (_: Event) => cloneCb(meta)

	val element = div(
		tabs,
		div(cls := "row")(
			div(cls := "col-md-8")(
				div(cls := "mt-3")(
					subtitle,
					links,
					description,
					subjects,
					creators,
					contributors,
					div(cls := "d-grid gap-2 mt-4 mb-5")(
						cloneButton
					)
				)
			)
		)
	).render

	private def listPeople(list: Seq[Person], label: String): Option[String] = {
		Option(list.map(c => c.name.toString))
			.filter(_.nonEmpty)
			.map(_.mkString(label + ": ", ", ", ""))
	}

}
