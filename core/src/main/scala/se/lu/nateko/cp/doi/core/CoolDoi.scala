package se.lu.nateko.cp.doi.core

import java.util.Random
import scala.util.Try

object CoolDoi {

	def makeRandom(seed: Long): String = {
		val rnd = new Random(seed)
		makeCoolDoi(rnd.nextLong())
	}

	def makeRandom: String = makeRandom(System.currentTimeMillis)

	def makeCoolDoi(from: Long): String = {
		val chars = CrockfordBase32.encodeBitQuintuples(from, 7, true)
		chars.take(4).mkString + '-' + chars.drop(4).mkString
	}

	def validate(coolDoi: String): Try[Long] =
		CrockfordBase32.decodeLong(coolDoi.replace("-", ""), true)
}
