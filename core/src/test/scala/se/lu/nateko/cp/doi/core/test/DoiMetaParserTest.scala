package se.lu.nateko.cp.doi.core.test

import org.scalatest.FunSuite
import scala.xml.Elem
import scala.xml.XML
import se.lu.nateko.cp.doi.core.DoiMetaParser
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi
import se.lu.nateko.cp.doi.meta._

class DoiMetaParserTest extends FunSuite{

	def getTestXml: Elem = XML.load(getClass.getResource("/doiMetaExampleFull.xml"))

	test("Parses the full DataCite metadata example successfully"){
		val expected = DoiMeta(
			id = Doi("10.5072", "example-full"),
			creators = Seq(
				Creator(
					PersonalName("Elizabeth", "Miller"),
					Seq(NameIdentifier.orcid("0000-0001-5000-0007")),
					Seq("DataCite")
				)
			),
			titles = Seq(
				Title("Full DataCite XML Example", Some("en-us"), None),
				Title("Demonstration of DataCite Properties.", Some("en-us"), Some(TitleType.Subtitle))
			),
			publisher = "DataCite",
			publicationYear = 2014,
			resourceType = ResourceType("XML", ResourceTypeGeneral.Software),
			subjects = Seq(
				Subject("000 computer science", Some("en-us"), Some(SubjectScheme.Dewey), None)
			),
			contributors = Seq(
				Contributor(
					name = GenericName("Starr, Joan"),
					affiliations = Seq("California Digital Library"),
					nameIds = Seq(NameIdentifier.orcid("0000-0002-7285-027X")),
					contributorType = ContributorType.ProjectLeader
				)
			),
			dates = Seq(Date("2014-10-17", DateType.Updated)),
			formats = Seq("application/xml"),
			version = Some(Version(3, 1)),
			rights = Seq(Rights("CC0 1.0 Universal", Some("http://creativecommons.org/publicdomain/zero/1.0/"))),
			descriptions = Seq(
				Description("XML example of all DataCite Metadata Schema v4.0 properties.", DescriptionType.Abstract, Some("en-us"))
			)
		)

		assertResult(expected){
			DoiMetaParser.parse(getTestXml).get
		}
	}

	test("Parses DOI ID successfully"){
		val doi = DoiMetaParser.parseDoi(getTestXml).get
	}

}
