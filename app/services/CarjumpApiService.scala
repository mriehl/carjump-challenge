package services

import scala.annotation.tailrec
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
    // need to reverse because we RLE'd from right to left (appending is generally O(n))
    val correctCompression = reversedCompressionSink.mapMaterializedValue(_.map(_.toSeq.reverse))
    source.runWith(correctCompression)
  }
  override def decompress[A](compressed: Seq[Compressed[A]]): Seq[A] = {
    ???
  }
}

object CarjumpApiService {
  implicit class CompressedSearchSyntax[A](val compressed: Seq[Compressed[A]]) extends AnyVal {
    def atIndex(index: Int): A = {
      search(compressed.toList, index)
    }
  }

  @tailrec
  def search[A](compressed: List[Compressed[A]], wantedIndex: Int): A = (compressed, wantedIndex) match {
    case (_, illegalIndex) if illegalIndex < 0 ⇒
      throw new IndexNotFoundException()
    case (Nil, _) ⇒
      throw new IndexNotFoundException()
    case (chunks, wantedIndex: Int) if wantedIndex <= chunks.head.width - 1 ⇒
      chunks.head.value
    case (Single(_) :: tail, wantedIndex: Int) ⇒
      search(tail, wantedIndex - 1)
    case (Repeat(chunkSize, _) :: tail, wantedIndex: Int) ⇒
      search(tail, Math.max(wantedIndex - chunkSize, 0))
  }
}

trait Compressor {
  def compress[A](source: Source[A, Any]): Future[Seq[Compressed[A]]]
  def decompress[A](compressed: Seq[Compressed[A]]): Seq[A]
}

sealed trait Compressed[+A] {
  def value: A
  def width: Int
}
case class Single[A](element: A) extends Compressed[A] {
  override def value: A = element
  override def width: Int = 1
}
case class Repeat[A](count: Int, element: A) extends Compressed[A] {
  override def value: A = element
  override def width: Int = count
}

class IndexNotFoundException() extends Exception
