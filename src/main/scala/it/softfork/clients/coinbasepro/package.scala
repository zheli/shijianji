package it.softfork.clients.coinbasepro

import akka.http.scaladsl.model.Uri

package object coinbasepro {
  val baseUri = Uri("https://api.pro.coinbase.com")
  val testBaseUri = Uri("https://api-public.sandbox.pro.coinbase.com")
}
