package se.lu.nateko.cp.doi.test.meta

import org.scalatest.FunSpec
import se.lu.nateko.cp.doi.meta._
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi


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

	describe("URI validation support"){

		def error(uri: String): Option[String] = new SelfValidating{
			def error = validUri(uri)
		}.error

		it("accepts plain HTTP[S] URLS"){
			assertResult(None)(error("http://bebe.com"))
			assertResult(None)(error("https://bebe.com"))
		}

		it("rejects a dummy string"){
			assert(error("dummy").isDefined)
		}
	}

	describe("Title validation support"){
		it("rejects an empty title"){
			assertResult(Some("Title must not be empty"))(Title("", None, None).error)
		}
	}

	describe("DoiMeta validation support"){

		val example = DoiMeta(
			id = Doi("10.5072", "carbonportal"),
			creators = Seq(
				Creator(
					name = GenericName("ICOS CP"),
					nameIds = Nil,
					affiliations = Nil
				)
			),
			contributors = Nil,
			titles = Seq(
				Title("Carbon Portal home page", None, None)
			),
			publisher = "ICOS Carbon Portal",
			publicationYear = 2016,
			resourceType = ResourceType("website", ResourceTypeGeneral.Service)
		)

		it("accepts valid DoiMeta"){
			assertResult(None)(example.error)
		}

		it("gives correct error if only title is wrong (empty)"){
			val wrongExample = example.copy(titles = Seq(Title("", None, None)))
			assertResult(Some("Title must not be empty"))(wrongExample.error)
		}
	}
}