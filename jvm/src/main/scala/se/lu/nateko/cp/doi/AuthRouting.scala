package se.lu.nateko.cp.doi

import se.lu.nateko.cp.cpauth.core.PublicAuthConfig
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directive1
import se.lu.nateko.cp.cpauth.core.Authenticator
import se.lu.nateko.cp.cpauth.core.UserId
import scala.util.Success
import scala.util.Failure
import se.lu.nateko.cp.cpauth.core.CookieToToken
import akka.http.javadsl.server.CustomRejection

class AuthRouting(authConfig: PublicAuthConfig) {

	private[this] val authenticator = Authenticator(authConfig).get

	val user: Directive1[UserId] = cookie(authConfig.authCookieName).flatMap{cookie => {
		val tokenTry = for(
			signedToken <- CookieToToken.recoverToken(cookie.value);
			token <- authenticator.unwrapToken(signedToken)
		) yield token

		tokenTry match {
			case Success(token) => provide(token.userId)
			case Failure(err) => reject(new CpauthAuthenticationFailedRejection(toMessage(err)))
		}
	}}

	val userOpt: Directive1[Option[UserId]] = user.map(Some(_)) | provide(None)

	private def toMessage(err: Throwable): String = {
		val msg = err.getMessage
		if(msg == null || msg.isEmpty) err.getClass.getName else msg
	}
}

class CpauthAuthenticationFailedRejection(val msg: String) extends CustomRejection
