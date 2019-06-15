package it.softfork.shijianji.clients

import akka.http.scaladsl.model.Uri

package object coinbasepro {
  val productionBaseUri = Uri("https://api.pro.coinbase.com")
  val sandboxBaseUri = Uri("https://api-public.sandbox.pro.coinbase.com")
}
