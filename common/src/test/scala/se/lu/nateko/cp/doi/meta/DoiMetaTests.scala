package se.lu.nateko.cp.doi.meta

import org.scalatest.funspec.AnyFunSpec
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.Doi
import scala.collection.Seq


class DoiMetaTests extends AnyFunSpec{

	describe("NameIdentifier"){

		it("is valid if has proper ORCID scheme"){
			val id = NameIdentifier("0000-0001-1234-123X", NameIdentifierScheme.ORCID)
			assert(id.errors.isEmpty)
		}

		it("is invalid if has ORCID scheme but wrong format"){
			val id = NameIdentifier("0000-0001-1234-123A", NameIdentifierScheme.ORCID)
			assert(id.errors.nonEmpty)
			assert(id.errors.exists(_.message.contains("format")))
		}

		it("is invalid if the scheme is neither ORCID nor ISNI"){
			val errors = NameIdentifier("any", NameIdentifierScheme("other", None)).errors
			assert(errors.nonEmpty)
			assert(errors.exists(_.message.contains("Only the following name identifier schemes are supported")))
		}
	}

	describe("URI validation support"){

		def errors(uri: String): Seq[ValidationError] = new SelfValidating{
			def errors = validateUri(uri, ValidationSection.DoiTarget, Nil)
		}.errors

		it("accepts plain HTTP[S] URLS"){
			assert(errors("http://bebe.com").isEmpty)
			assert(errors("https://bebe.com").isEmpty)
		}

		it("rejects a dummy string"){
			assert(errors("dummy").nonEmpty)
		}
	}

	describe("Title validation support"){
		it("rejects an empty title"){
			val titleErrors = Title("", None, None).errors
			assert(titleErrors.nonEmpty)
			assert(titleErrors.exists(_.message == "Title must not be empty"))
		}
	}

	describe("DoiMeta validation support"){

		val example = DoiMeta(
			doi = Doi("10.5072", "carbonportal"),
			creators = Seq(
				Creator(
					name = GenericName("ICOS CP"),
					nameIdentifiers = Nil,
					affiliation = Nil
				)
			),
			contributors = Nil,
			titles = Some(Seq(
				Title("Carbon Portal home page", None, None)
			)),
			publisher = Some("ICOS Carbon Portal"),
			publicationYear = Some(2016),
			types = Some(ResourceType(Some("website"), Some(ResourceTypeGeneral.Service))),
			url = Some("https://meta.icos-cp.eu/objects/-S_VUEUOFnH4L7nqlWmxuRN_")
		)

		it("accepts valid DoiMeta"){
			assert(example.errors.isEmpty)
		}

		it("gives correct error if only title is wrong (empty)"){
			val wrongExample = example.copy(titles = Some(Seq(Title("", None, None))))
			assert(wrongExample.errors.nonEmpty)
			assert(wrongExample.errors.exists(_.message == "Title must not be empty"))
		}
	}
}