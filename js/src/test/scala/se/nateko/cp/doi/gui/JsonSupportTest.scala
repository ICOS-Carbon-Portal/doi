package se.nateko.cp.doi.gui

import org.scalatest.funsuite.AnyFunSuite

import play.api.libs.json._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.JsonSupport.given
import se.lu.nateko.cp.doi.meta.Subject
class JsonSupportTest extends AnyFunSuite{

	test("Parsing all-DOIs JSON from production"){
		val fs = scalajs.js.Dynamic.global.require("fs")
		val jsonStr = fs.readFileSync("./core/src/test/resources/productionDois.json").toString
		val jso = Json.parse(jsonStr).as[JsObject]
		val dois = (jso \ "data").as[JsArray].value.map{jsv =>
			(jsv \ "attributes").as[DoiMeta]
		}
		assert(dois.length === 5)
	}

	test("Subject without SubjectScheme gets serialized without problems"){
		val subj = Json.toJson(Subject("subj"))
	}
}
