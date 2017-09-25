package utils

import java.net.URL

import models.OAuthErrorCode._
import models._

import scala.concurrent.Future
import scala.util.Try

object UriUtils {

  def urisMatch(reqUri: String)(uri: String) = reqUri == uri || urlsMatch(reqUri, uri)

  private def urlsMatch(reqUri: String, uri: String): Boolean = (for {
    reqUrl <- parseUrl(reqUri)
    url <- parseUrl(uri)
  } yield urlsMatch(reqUrl, url)).getOrElse(false)

  private def urlsMatch(reqUrl: URL, url: URL) =
    reqUrl.getProtocol == url.getProtocol &&
      reqUrl.getHost == url.getHost &&
      reqUrl.getPort == url.getPort &&
      isChildPathOf(reqUrl.getPath, url.getPath)

  private def parseUrl(url: String) = Try { new URL(url) }.toOption

  private def isChildPathOf(reqPath: String, path: String) =
    reqPath.split('/').indexOfSlice(path.split('/')) == 0
}
