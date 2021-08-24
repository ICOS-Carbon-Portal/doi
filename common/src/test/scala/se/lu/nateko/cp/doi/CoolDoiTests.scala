package se.lu.nateko.cp.doi

import org.scalatest.funsuite.AnyFunSuite

class CoolDoiTests extends AnyFunSuite{

	test("decodes a valid cool DOI successfully"){
		assert(CoolDoi.validate("VNX5-QXCB").isSuccess)
		assert(CoolDoi.validate("KVTD-VPWM").isSuccess)
	}

	test("completes a round trip successfully"){
		val underlyingValue = 38237984L

		val doi = CoolDoi.makeCoolDoi(underlyingValue)

		assert(CoolDoi.validate(doi).get == underlyingValue)
	}

	test("reports error on checksum mismatch"){
		val validation = CoolDoi.validate("VNX5-QXCA")

		assert(validation.isFailure)

		assert(validation.failed.get.getMessage.contains("did not match"))
	}
}
