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
		val expected = DoiMetaExamples.full

		assertResult(expected){
			DoiMetaParser.parse(getTestXml).get
		}
	}

}
