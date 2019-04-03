package it.softfork.shijianji

import akka.http.scaladsl.model.{HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

package object utils {
  implicit class RichUri(uri: Uri) {
    def /(path: String): Uri = {
      uri.copy(path = uri.path / path)
    }
  }

  implicit class RichFutureResponse(future: Future[HttpResponse]) {
    def asUnsafe[T](
      implicit ec: ExecutionContext,
      unmarshaller: Unmarshaller[HttpResponse, T],
      materializer: Materializer
    ): Future[T] = {
      future.flatMap(unmarshaller.apply)
    }

    def asSuccessful[T](
      implicit ec: ExecutionContext,
      unmarshaller: Unmarshaller[HttpResponse, T],
      materializer: Materializer
    ): Future[T] = {
      future.flatMap { response =>
        if (response.status.isSuccess()) unmarshaller(response)
        else throw new RuntimeException(s"$response")
      }
    }
  }

}
