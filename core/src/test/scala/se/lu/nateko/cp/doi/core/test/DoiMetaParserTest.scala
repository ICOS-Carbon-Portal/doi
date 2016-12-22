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
		val actual = DoiMetaParser.parse(getTestXml).get

		val expected = DoiMeta(
			id = Doi("10.5072", "example-full"),
			creators = Seq(
				Creator(GenericName("Miller, Elizabeth"), None, Nil)
			),
			titles = Seq(
				Title("Full DataCite XML Example", None, None),
				Title("Demonstration of DataCite Properties.", None, None)
			),
			publisher = "DataCite",
			publicationYear = 2014
		)

		assert(actual === expected)
	}

	test("Parses DOI ID successfully"){
		val doi = DoiMetaParser.parseDoi(getTestXml).get
	}

	test("Parses creators successfully"){
		val creators = DoiMetaParser.parseCreators(getTestXml).get
	}

	test("Parses titles successfully"){
		val creators = DoiMetaParser.parseTitles(getTestXml).get
	}
}
