package se.lu.nateko.cp.doi.gui.test.widgets

import org.scalatest.FunSpec
import se.lu.nateko.cp.doi.gui.widgets.DoiTargetWidget.targetUrlError

class DoiTargetWidgetTests extends FunSpec{

	describe("targetUrlError method"){
		okTest("https://www.icos-cp.eu/")
		okTest("https://www.icos-cp.eu/simplePath")
		notOkTest("https://www.icos-cp.eu")
		notOkTest("http://www.icos-cp.eu/")
		okTest("https://meta-data.fieldsites.se/multi/segment/path/and#fragment")
		okTest("https://some-thing.icos-etc.eu/simplePathAnd#fragment")
	}

	private def okTest(url: String) = it(s"Finds no errors in '$url'"){
		assert(targetUrlError(url).isEmpty)
	}

	private def notOkTest(url: String) = it(s"'$url' is not an acceptable URL"){
		assert(targetUrlError(url).isDefined)
	}
}
