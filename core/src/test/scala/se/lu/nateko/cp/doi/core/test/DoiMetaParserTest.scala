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
					Seq(NameIdentifier("0000-0001-5000-0007", NameIdentifierScheme("ORCID", Some("http://orcid.org/")))),
					Seq("DataCite")
				)
			),
			contributors = Nil,
			titles = Seq(
				Title("Full DataCite XML Example", Some("en-us"), None),
				Title("Demonstration of DataCite Properties.", Some("en-us"), Some(TitleType.Subtitle))
			),
			publisher = "DataCite",
			publicationYear = 2014,
			resourceType = ResourceType("XML", ResourceTypeGeneral.Software)
		)

		assertResult(expected){
			DoiMetaParser.parse(getTestXml).get
		}
	}

	test("Parses DOI ID successfully"){
		val doi = DoiMetaParser.parseDoi(getTestXml).get
	}

}
