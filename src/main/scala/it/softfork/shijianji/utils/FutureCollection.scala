package it.softfork.shijianji.utils

import scala.concurrent.{ExecutionContext, Future}

object FutureCollection {

  /**
    * Sequentially executes a async function for each element in a collection.
    * Will not start the processing of the next element until the previous one finishes.
    *
    * @return A future with a sequence of the result of each processed element.
    */
  def mapSequential[I, O](xs: Seq[I])(f: I => Future[O])(implicit ec: ExecutionContext): Future[Seq[O]] = {
    def next(ys: Seq[O], xs: Seq[I]): Future[Seq[O]] = {
      xs.headOption match {
        case Some(x) =>
          f(x).flatMap(y => next(ys :+ y, xs.tail))
        case None =>
          Future.successful(ys)
      }
    }

    next(Vector.empty, xs)
  }
}

