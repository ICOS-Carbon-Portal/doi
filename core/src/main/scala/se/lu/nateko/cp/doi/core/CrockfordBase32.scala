package se.lu.nateko.cp.doi.core

import scala.collection.immutable.IndexedSeq
import scala.collection.immutable.Map
import scala.util.Try
import scala.util.Failure
import scala.util.Success

object CrockfordBase32 {

	val alphabet: IndexedSeq[Char] = IndexedSeq(
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
		'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
		'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z',
		'*', '~', '$', '=', 'U'
	)

	val encoder: Map[Int, Char] = alphabet.indices.map(i => (i, alphabet(i))).toMap

	val decoder: Map[Char, Int] = (
		alphabet.indices.map(i => (alphabet(i), i)) ++
		Seq('L' -> 1, 'O' -> 0)
	).toMap

	def encodeBitQuintuples(l: Long, nQuintuples: Int, check: Boolean): IndexedSeq[Char] = {
		val mask: Long = (1L << nQuintuples * 5) - 1
		val toEncode: Long = l & mask

		val encoded = for(
			shift <- (nQuintuples - 1) * 5 to 0 by -5
		) yield
			encoder(((toEncode >> shift) & 31L).toInt)


		if(check)
			encoded :+ encoder((toEncode % 37).toInt)
		else
			encoded
	}

	def decodeLong(chars: IndexedSeq[Char], check: Boolean): Try[Long] = {
		val nQuintuples = chars.size - (if(check) 1 else 0)
		val toDecode = chars.map(_.toUpper)

		if(nQuintuples < 0) Failure(new Exception("Empty list of chars, nothing to decode"))

		else if(nQuintuples > 12) Failure(new Exception("List of chars too long to fit in Long"))

		else if(toDecode.exists(c => !decoder.contains(c))) Failure(
			new Exception("List of chars contains invalid characters outside of the expected base32 alphabet")
		)

		else {
			var decoded = 0L

			for(i <- 0 until nQuintuples){
				val shift = (nQuintuples - 1 - i) * 5
				decoded |= (decoder(toDecode(i)).toLong << shift)
			}

			if(!check || encoder((decoded % 37).toInt) == chars.last) Success(decoded)
			else Failure(new Exception("The check symbols did not match"))
		}
	}
}
