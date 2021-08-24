package se.lu.nateko.cp.doi.core

import org.scalatest.funsuite.AnyFunSuite

import JsonSupport._
import spray.json._
import se.lu.nateko.cp.doi.DoiMeta

class JsonSupportTest extends AnyFunSuite{
	test("DoiMeta serialization/deserialization round trip"){
		val from = DoiMetaExamples.full
		val to = from.toJson.convertTo[DoiMeta]
		assert(from === to)
	}
}