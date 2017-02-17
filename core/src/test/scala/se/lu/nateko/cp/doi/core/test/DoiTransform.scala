package se.lu.nateko.cp.doi.core.test

import scala.xml.XML
import se.lu.nateko.cp.doi.core.DoiMetaParser
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.Contributor
import se.lu.nateko.cp.doi.meta.ContributorType
import java.io.FileOutputStream
import java.io.File
import java.nio.charset.Charset

/**
 * Just a playground. Is put here in the test code area because it is not needed in the deployment artifacts.
 */
object DoiTransform{

	val folder = "/home/maintenance/Documents/CP/IngosMetadata/"

	def transformIngosMeta(): Unit = {
		saveXml(transform(getOldMeta), "IngosDataCiteMetaUpdated.xml")
	}

	def printPeopleAffiliations(): Unit = {
		transform(getOldMeta).contributors
			.flatMap{c => c.affiliations.map(a => (c, a))}
			.map{case (c, a) =>
				s"${c.name.toString}: $a"
			}
			.sortBy(s => s)
			.foreach(println)
	}

	def getOldMeta: DoiMeta = {
		val oldXml = XML.loadFile(folder + "IngosDataCiteMeta.xml")
		DoiMetaParser.parse(oldXml).get
	}

	def transform(oldMeta: DoiMeta): DoiMeta = {
		val doi = oldMeta.id.copy(prefix = "10.18160")

		val oldContr = oldMeta.contributors.head
		val creator = Creator(
			name = oldContr.name,
			nameIds = oldContr.nameIds,
			affiliations = oldContr.affiliations
		)

		val contributors = oldMeta.creators.map{cr => Contributor(
			name = cr.name,
			nameIds = cr.nameIds,
			affiliations = cr.affiliations,
			contributorType = ContributorType.ContactPerson
		)}

		oldMeta.copy(
			id = doi,
			creators = Seq(creator),
			contributors = contributors
		)
	}

	def saveXml(meta: DoiMeta, fileName: String): Unit = {
		val uglyXmlText = views.xml.doi.DoiMeta(meta).body
		val xml = XML.loadString(uglyXmlText)

		val prettyXmlText = "<?xml version='1.0' encoding='UTF-8'?>\n" +
			new scala.xml.PrettyPrinter(120, 4).format(xml)

		saveString(prettyXmlText, fileName)
	}

	def saveString(s: String, fileName: String): Unit = {
		val os = new FileOutputStream(new File(folder + fileName))
		os.write(s.getBytes(Charset.forName("UTF-8")))
		os.close()
	}
}

