package se.lu.nateko.cp.doi.test.meta

import org.scalatest.FunSpec
import se.lu.nateko.cp.doi.Doi

class DoiTests extends FunSpec{

	describe("Doi validation"){

		it("accepts correct DOI"){
			assert(Doi("10.5555", "KVTD-VPWM").error.isEmpty)
			assert(Doi("10.5555", "KvTd-VPWM").error.isEmpty)
		}

		it("rejects empty prefix"){
			assert(Doi("", "blabla").error.isDefined)
		}

		it("rejects empty suffix"){
			assert(Doi("10.5555", "").error.isDefined)
		}

		it("rejects prefix that does not start with '10.'"){
			assert(Doi("23.473", "blabla").error.isDefined)
		}

		it("rejects suffix with space"){
			assert(Doi("10.5555", "bla bla").error.isDefined)
		}
	}
}