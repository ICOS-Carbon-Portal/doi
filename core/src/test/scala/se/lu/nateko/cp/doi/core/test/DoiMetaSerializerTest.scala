package se.lu.nateko.cp.doi.core.test

import org.scalatest.FunSuite
import scala.xml.XML
import se.lu.nateko.cp.doi.core.DoiMetaParser
import javax.xml.validation.SchemaFactory
import javax.xml.XMLConstants
import java.net.URL
import javax.xml.transform.stream.StreamSource
import java.io.StringReader

class DoiMetaSerializerTest extends FunSuite{

	test("Serializing/parsing round trip"){
		val meta = DoiMetaExamples.full

		assertResult(meta){
			val serialized = views.xml.doi.DoiMeta(meta).body
			val xml = XML.loadString(serialized)
			DoiMetaParser.parse(xml).get
		}
	}

	ignore("For manual run: validating the XML-serialized metadata against DataCite's XSD Schema"){
		val sfactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
		val schema = sfactory.newSchema(new URL("http://schema.datacite.org/meta/kernel-4.0/metadata.xsd"))
		val validator = schema.newValidator()
		val serialized = views.xml.doi.DoiMeta(DoiMetaExamples.full).body
		val source = new StreamSource(new StringReader(serialized))
		validator.validate(source)
	}
}
