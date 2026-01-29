package se.lu.nateko.cp.doi.gui

import se.lu.nateko.cp.doi.DoiMeta
import scalatags.JsDom.all._
import org.scalajs.dom.Event
import se.lu.nateko.cp.doi.meta.Person
import se.lu.nateko.cp.doi.meta.RelatedIdentifierType
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

	private val alternativeTitles = meta.titles.filter(_.length > 1).map { titles =>
		Seq(
			h5(cls := "mt-4 mb-3")("Alternative Titles"),
			div(cls := "mb-3")(
				titles.tail.map { t =>
					div(cls := "mb-1")(
						t.titleType.map(tt => span(cls := "text-muted me-2")(s"[$tt]")).toSeq,
						span(t.title)
					)
				}.toSeq
			)
		)
	}.getOrElse(Seq())

	private val url = meta.url.map { u =>
		Seq(
			h5(cls := "mt-4 mb-3")("Target URL"),
			div(cls := "mb-3")(
				a(href := u, target := "_blank", cls := "link-secondary")(u)
			)
		)
	}.getOrElse(Seq())

	private val dates = if (meta.dates.nonEmpty) {
		Seq(
			h5(cls := "mt-4 mb-3")("Dates"),
			div(cls := "mb-3")(
				meta.dates.map { d =>
					div(cls := "mb-1")(
						d.dateType.map(dt => strong(s"$dt: ")).getOrElse(strong("Date: ")),
						span(d.date)
					)
				}.toSeq
			)
		)
	} else Seq()

	private val formats = if (meta.formats.nonEmpty) {
		Seq(
			h5(cls := "mt-4 mb-3")("Formats"),
			div(cls := "mb-3 d-flex flex-wrap gap-2")(
				meta.formats.map(f => span(cls := "badge rounded-pill bg-secondary")(f)).toSeq
			)
		)
	} else Seq()

	private val rights = meta.rightsList.filter(_.nonEmpty).map { rightsList =>
		Seq(
			h5(cls := "mt-4 mb-3")("Rights"),
			div(cls := "mb-3")(
				rightsList.map { r =>
					div(cls := "mb-2")(
						r.rightsUri match {
							case Some(uri) => a(href := uri, target := "_blank", cls := "link-secondary")(r.rights)
							case None => span(r.rights)
						},
						r.rightsIdentifier.map(id => span(cls := "ms-2 text-muted")(s"($id)")).toSeq
					)
				}.toSeq
			)
		)
	}.getOrElse(Seq())

	private val fundingReferences = meta.fundingReferences.filter(_.nonEmpty).map { refs =>
		Seq(
			h5(cls := "mt-4 mb-3")("Funding References"),
			div(cls := "mb-3")(
				refs.map { fr =>
					div(cls := "mb-3 ps-3 border-start")(
						fr.funderName.map(name => div(cls := "mb-1")(strong(name))).toSeq,
						fr.funderIdentifier.flatMap { fi =>
							fi.funderIdentifier.map { id =>
								div(cls := "mb-1 small text-muted")(
									fi.scheme.map(s => span(s"$s: ")).getOrElse(span("")),
									span(id)
								)
							}
						}.toSeq,
						fr.award.flatMap { aw =>
							val parts = Seq(
								aw.awardNumber.map(n => s"Award: $n"),
								aw.awardTitle
							).flatten
							if (parts.nonEmpty || aw.awardUri.isDefined) {
								Some(div(cls := "mb-1 small")(
									parts.headOption.map(p => span(p)).toSeq,
									parts.tail.map(p => span(s" - $p")).toSeq,
									aw.awardUri.map(uri => span(" ", a(href := uri, target := "_blank", cls := "link-secondary")("Link"))).toSeq
								))
							} else None
						}.toSeq
					)
				}.toSeq
			)
		)
	}.getOrElse(Seq())

	private val geoLocations = meta.geoLocations.filter(_.nonEmpty).map { locs =>
		Seq(
			h5(cls := "mt-4 mb-3")("Geo Locations"),
			div(cls := "mb-3")(
				locs.map { loc =>
					div(cls := "mb-3 ps-3 border-start")(
						loc.geoLocationPlace.map(place => div(cls := "mb-1")(strong(place))).toSeq,
						loc.geoLocationPoint.flatMap { pt =>
							for {
								lat <- pt.pointLatitude
								lon <- pt.pointLongitude
							} yield div(cls := "mb-1 small text-muted")(s"Point: $lat, $lon")
						}.toSeq,
						loc.geoLocationBox.map { box =>
							val parts = Seq(
								box.northBoundLatitude.map(n => s"N: $n"),
								box.southBoundLatitude.map(s => s"S: $s"),
								box.eastBoundLongitude.map(e => s"E: $e"),
								box.westBoundLongitude.map(w => s"W: $w")
							).flatten
							div(cls := "mb-1 small text-muted")(s"Box: ${parts.mkString(", ")}")
						}.toSeq
					)
				}.toSeq
			)
		)
	}.getOrElse(Seq())

	private val relatedIdentifiers = meta.relatedIdentifiers.filter(_.nonEmpty).map { rels =>
		Seq(
			h5(cls := "mt-4 mb-3")("Related Identifiers"),
			div(cls := "mb-3")(
				rels.map { ri =>
					div(cls := "mb-2")(
						ri.relationType.map(rt => span(cls := "me-2")(strong(s"$rt:"))).toSeq,
						ri.relatedIdentifierType match {
							case Some(RelatedIdentifierType.DOI) =>
								a(href := s"https://doi.org/${ri.relatedIdentifier}", target := "_blank", cls := "link-secondary")(ri.relatedIdentifier)
							case Some(RelatedIdentifierType.URL) =>
								a(href := ri.relatedIdentifier, target := "_blank", cls := "link-secondary")(ri.relatedIdentifier)
							case _ =>
								span(ri.relatedIdentifier)
						},
						ri.resourceTypeGeneral.map(rtg => span(cls := "ms-2 text-muted small")(s"[$rtg]")).toSeq
					)
				}.toSeq
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
					contributors,
					alternativeTitles,
					url,
					dates,
					formats,
					rights,
					fundingReferences,
					geoLocations,
					relatedIdentifiers
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
