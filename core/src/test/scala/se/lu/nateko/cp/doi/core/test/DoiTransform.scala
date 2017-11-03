package se.lu.nateko.cp.doi.core.test

import scala.xml.XML
import se.lu.nateko.cp.doi.core.DoiMetaParser
import se.lu.nateko.cp.doi.DoiMeta
import se.lu.nateko.cp.doi.meta.Creator
import se.lu.nateko.cp.doi.meta.PersonalName
import se.lu.nateko.cp.doi.meta.Contributor
import se.lu.nateko.cp.doi.meta.ContributorType
import java.io.FileOutputStream
import java.io.File
import java.nio.charset.Charset
import se.lu.nateko.cp.doi.Doi
import scala.io.Source

/**
 * Just a playground. Is put here in the test code area because it is not needed in the deployment artifacts.
 */
object DoiTransform{

	//val folder = "/home/maintenance/Documents/CP/IngosMetadata/"
	//val srcFile = "IngosDataCiteMeta.xml"
	val folder = "/home/maintenance/Documents/CP/L3metadata/GCPdoi/"
	val srcFile = "gcp2017doiMetaDraft.xml"

	def printPeopleAffiliations(): Unit = {
		getMeta.contributors
			.flatMap{c => c.affiliations.map(a => (c, a))}
			.map{case (c, a) =>
				s"${c.name.toString}: $a"
			}
			.sortBy(s => s)
			.foreach(println)
	}

	def getMeta: DoiMeta = {
		val xml = XML.loadFile(folder + srcFile)
		DoiMetaParser.parse(xml).get
	}

	def transform(oldMeta: DoiMeta): DoiMeta = {
		val doi = Doi(prefix = "10.18160", suffix = "VNX5-QXCB")

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

	def readGcpContribs: Seq[Contributor] = {
		Source.fromFile(folder + "contribsList.tsv").getLines().map{line =>
			val vals = line.split("\t")
			val Seq(lastName, firstName) = vals(0).split(", ").toSeq
			Contributor(
				name = PersonalName(firstName, lastName),
				nameIds = Nil,
				affiliations = vals.drop(1).map(_.trim),
				contributorType = ContributorType.ContactPerson
			)
		}.toSeq
	}

	def transformGcp(): Unit = {
		val newMeta = getMeta.copy(contributors = readGcpContribs)
		saveXml(newMeta, "gcp2017doiMeta.xml")
	}
}

