package se.lu.nateko.cp.doi.test.meta

import org.scalatest.FunSpec
import se.lu.nateko.cp.doi.meta._


class DoiMetaTests extends FunSpec{

	describe("NameIdentifier"){

		it("is valid if has proper ORCID scheme"){
			val id = NameIdentifier("0000-0001-1234-123X", NameIdentifierScheme.Orcid)
			assert(id.error === None)
		}

		it("is invalid if has ORCID scheme but wrong format"){
			val id = NameIdentifier("0000-0001-1234-123A", NameIdentifierScheme.Orcid)
			assert(id.error.isDefined)
			assert(id.error.get.contains("format"))
		}

		it("is invalid if the scheme is neither ORCID nor ISNI"){
			val error = NameIdentifier("any", NameIdentifierScheme("other", None)).error
			assert(error.isDefined)
			assert(error.get.contains("Only the following name identifier schemes are supported"))
		}
	}
}