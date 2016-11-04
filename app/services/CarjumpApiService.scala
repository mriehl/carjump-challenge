package services

import scala.concurrent.{ ExecutionContext, Future }

import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import clients.CarjumpClient
import util.SLF4JLogging

class CarjumpApiService(carjumpClient: CarjumpClient)(implicit ec: ExecutionContext, mat: Materializer) extends SLF4JLogging with Compressor {

  def fetchCompressedData(): Future[Seq[Compressed[String]]] =
    carjumpClient
      .fetchData()
      .flatMap(compress(_))

  override def compress[A](source: Source[A, Any]): Future[Seq[Compressed[A]]] = {
    val reversedCompressionSink = Sink.fold[List[Compressed[A]], A](List.empty[Compressed[A]]) {
      case (accu, elementToCompress) ⇒
        accu match {
          case Single(element) :: tail if element.equals(elementToCompress) ⇒
            Repeat(2, element) :: tail
          case Repeat(repetitions, element) :: tail if element.equals(elementToCompress) ⇒
            Repeat(repetitions + 1, element) :: tail
          case otherCompresseds ⇒
            Single(elementToCompress) :: otherCompresseds
        }
    }
    // need to reverse because we RLE'd from right to left (list cons is O(1), appending to a Seq would be more expensive)
    val correctCompression = reversedCompressionSink.mapMaterializedValue(_.map(_.toSeq.reverse))
    source.runWith(correctCompression)
  }
  override def decompress[A](compressed: Seq[Compressed[A]]): Seq[A] = {
    ???
  }

}

trait Compressor {
  def compress[A](source: Source[A, Any]): Future[Seq[Compressed[A]]]
  def decompress[A](compressed: Seq[Compressed[A]]): Seq[A]
}

sealed trait Compressed[+A]
case class Single[A](element: A) extends Compressed[A]
case class Repeat[A](count: Int, element: A) extends Compressed[A]
