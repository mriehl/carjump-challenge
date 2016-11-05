package services

import clients.CarjumpClient
import org.specs2.mutable.Specification

import CarjumpApiService._

class CarjumpApiServiceSpec extends Specification {

  "A carjump api service" should {

    "find elements by index" in {
      val compressed = Seq(Repeat(3, "a"), Repeat(2, "b"), Single("c"), Repeat(2, "a"))
      compressed atIndex 0 must be equalTo ("a")
      compressed atIndex 1 must be equalTo ("a")
      compressed atIndex 2 must be equalTo ("a")
      compressed atIndex 3 must be equalTo ("b")
      compressed atIndex 4 must be equalTo ("b")
      compressed atIndex 5 must be equalTo ("c")
      compressed atIndex 6 must be equalTo ("a")
      compressed atIndex 7 must be equalTo ("a")
    }
    "complain when index is out of bounds" in {
      val compressed = Seq.empty[Compressed[String]]
      compressed atIndex 0 must throwA[IndexNotFoundException]
      compressed atIndex 42 must throwA[IndexNotFoundException]
    }
  }
}
