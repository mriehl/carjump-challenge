package clients

import java.net.URL

case class CarjumpBaseUrl(value: URL) extends AnyVal {
  override def toString: String = value.toString()
}
