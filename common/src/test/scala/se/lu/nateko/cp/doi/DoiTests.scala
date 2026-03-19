package se.lu.nateko.cp.doi

import org.scalatest.funspec.AnyFunSpec

class DoiTests extends AnyFunSpec{

	describe("Doi validation"){

		it("accepts correct DOI"){
			assert(Doi("10.5555", "KVTD-VPWM").errors.isEmpty)
			assert(Doi("10.5555", "KvTd-VPWM").errors.isEmpty)
		}

		it("rejects empty prefix"){
			assert(Doi("", "blabla").errors.nonEmpty)
		}

		it("rejects empty suffix"){
			assert(Doi("10.5555", "").errors.nonEmpty)
		}

		it("rejects prefix that does not start with '10.'"){
			assert(Doi("23.473", "blabla").errors.nonEmpty)
		}

		it("rejects suffix with space"){
			assert(Doi("10.5555", "bla bla").errors.nonEmpty)
		}
	}

	describe("DOI parsing"){
		it("succeeds on correct DOI string"){
			assert(Doi.parse("10.1234/bebebe").isSuccess)
		}

		it("fails if prefix does not start with 10"){
			assert(Doi.parse("20.1234/bebebe").isFailure)
		}

		it("fails if suffix is bad"){
			assert(Doi.parse("10.1234/$").isFailure)
		}
	}
}
