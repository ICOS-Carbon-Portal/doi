package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.meta.Person
import se.lu.nateko.cp.doi.gui.widgets.UnifiedToolbar

class DoiMetaViewer(meta: DoiMeta, toolbar: UnifiedToolbar) {
	
	private val subtitleProperties =
		Seq(
			meta.types.map(t => Seq(t.resourceTypeGeneral, t.resourceType).filter(_.nonEmpty).flatten.mkString(" / ")),
			meta.version.map(version => s"version $version"),
			meta.publicationYear.map(year => s"published in $year"),
			meta.publisher.map(publisher => s"by $publisher")
		).filter(_.nonEmpty).flatten
	
	private val subtitle = if (subtitleProperties.nonEmpty) {
	div(cls := "text-muted small mb-4")(
		meta.types.map(t => 
			div(cls := "mb-1")(
				strong("Type: "),
				Seq(t.resourceTypeGeneral, t.resourceType).filter(_.nonEmpty).flatten.mkString(" / ")
			)
		).toSeq,
		meta.version.map(v => div(cls := "mb-1")(strong("Version: "), v.toString)).toSeq,
		meta.publicationYear.map(y => div(cls := "mb-1")(strong("Published: "), y)).toSeq,
		meta.publisher.map(p => div(cls := "mb-1")(strong("Publisher: "), p)).toSeq
	)
} else div()
	
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
			div(cls := "mb-3 d-flex flex-wrap gap-2")(
				meta.subjects.map(subject => 
					span(cls := "badge rounded-pill bg-secondary")(subject.subject)
				).toSeq
			)
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
	
	val element = div(
		div(cls := "row")(
			div(cls := "col-md-8")(
				div(cls := "mt-3")(
					subtitle,
					description,
					subjects,
					creators,
					contributors
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
