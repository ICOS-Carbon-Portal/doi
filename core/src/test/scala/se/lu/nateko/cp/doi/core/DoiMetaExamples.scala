package se.lu.nateko.cp.doi.core

import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.{Doi, DoiMeta}

object DoiMetaExamples {

	def full = DoiMeta(
		doi = Doi("10.5072", "EXAMPLE-FULL"),
		creators = Seq(
			Creator(
				PersonalName("Elizabeth", "Miller"),
				Seq(NameIdentifier.orcid("0000-0001-5000-0007")),
				Seq(Affiliation("DataCite"))
			)
		),
		titles = Some(Seq(
			Title("Full DataCite XML Example", Some("en-us"), None),
			Title("Demonstration of DataCite Properties.", Some("en-us"), Some(TitleType.Subtitle))
		)),
		publisher = Some("DataCite"),
		publicationYear = Some(2014),
		types = Some(ResourceType(Some("XML"), Some(ResourceTypeGeneral.Software))),
		subjects = Seq(
			Subject("000 computer science", Some("en-us"), Some(SubjectScheme.Dewey), None)
		),
		contributors = Seq(
			Contributor(
				name = GenericName("Starr, Joan"),
				affiliation = Seq(Affiliation("California Digital Library")),
				nameIdentifiers = Seq(NameIdentifier.orcid("0000-0002-7285-027X")),
				contributorType = Some(ContributorType.ProjectLeader)
			)
		),
		dates = Seq(Date("2014-10-17", DateType.Updated)),
		formats = Seq("application/xml"),
		version = Some(Version(3, 1)),
		rightsList = Some(Seq(Rights("CC0 1.0 Universal", Some("http://creativecommons.org/publicdomain/zero/1.0/")))),
		descriptions = Seq(
			Description("XML example of all DataCite Metadata Schema v4.0 properties.", DescriptionType.Abstract, Some("en-us"))
		),
		url = Some("https://meta.icos-cp.eu/objects/-S_VUEUOFnH4L7nqlWmxuRN_")
	)
}
