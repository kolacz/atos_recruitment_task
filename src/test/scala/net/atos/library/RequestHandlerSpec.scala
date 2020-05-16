package net.atos.library

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RequestHandlerSpec extends AnyFlatSpec with Matchers {
  "The RequestHandler object" should "insert books into library registry" in {
    val lib = Library(Map(), 0)
    val addRequest = """{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""
    val (response, _) = RequestHandler.handle(addRequest)(lib)
    response shouldEqual s"""{"OK": {"message": "Book(title=Purely Functional Data Structures, year=1996, author=Chris Okasaki, isAvailable=true) has been added"}}"""
  }
}
