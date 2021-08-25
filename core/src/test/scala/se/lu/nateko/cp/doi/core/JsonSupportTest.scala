package se.lu.nateko.cp.doi.core

import org.scalatest.funsuite.AnyFunSuite

import JsonSupport._
import spray.json._
import se.lu.nateko.cp.doi.DoiMeta
import java.nio.file.Files
import java.nio.file.Paths

class JsonSupportTest extends AnyFunSuite{
	test("DoiMeta serialization/deserialization round trip"){
		val from = DoiMetaExamples.full
		val to = from.toJson.convertTo[DoiMeta]
		assert(from === to)
	}

	test("Parsing all-DOIs JSON from production"){
		val jsStr = Files.readString(Paths.get("./core/src/test/resources/productionDois.json"))
		val dois = jsStr.parseJson.asJsObject.fields("data").asInstanceOf[JsArray].elements.map{el =>
			val js = el.asJsObject.fields("attributes")

			try{js.convertTo[DoiMeta]}
			catch{
				case err: Throwable =>
					println(js.prettyPrint)
					throw err
			}
		}
		assert(dois.length === 5)
	}
}