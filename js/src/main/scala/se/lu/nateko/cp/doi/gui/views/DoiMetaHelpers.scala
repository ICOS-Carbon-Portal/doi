package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.DoiMeta

object DoiMetaHelpers {
	def extractTitle(meta: DoiMeta): String = {
		meta.titles
			.flatMap(_.headOption)
			.map(_.title)
			.getOrElse("No title")
	}
}
