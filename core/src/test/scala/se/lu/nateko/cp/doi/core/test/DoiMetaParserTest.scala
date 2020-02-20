package se.lu.nateko.cp.doi.core.test

import org.scalatest.funsuite.AnyFunSuite
import scala.xml.Elem
import scala.xml.XML
import se.lu.nateko.cp.doi.core.DoiMetaParser

class DoiMetaParserTest extends AnyFunSuite{

	def getTestXml: Elem = XML.load(getClass.getResource("/doiMetaExampleFull.xml"))

	test("Parses the full DataCite metadata example successfully"){
		val expected = DoiMetaExamples.full

		assertResult(expected){
			DoiMetaParser.parse(getTestXml).get
		}
	}

}
