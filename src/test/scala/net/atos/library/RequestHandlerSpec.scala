package net.atos.library

import org.scalatest.Assertions._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RequestHandlerSpec extends AnyFlatSpec with Matchers {
  "The RequestHandler object" should "be able to add a new book into the library registry" in {
    val lib = Library(Map(), 0)
    val addRequest = """{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""
    val (response, lib2) = RequestHandler.handle(addRequest)(lib)
    response shouldEqual s"""{"OK": {"message": "(id=${lib2.currentId}, title=Purely Functional Data Structures, year=1996, author=Chris Okasaki, isAvailable=true, lentBy=nobody) has been added"}}"""
  }

  it should "be able to remove a book from the library registry" in {
    val lib = Library(Map(), 0)
    val addRequest = """{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""
    val removeRequest = """{"removeBook": {"id": 1}}"""
    val actions = List(
      RequestHandler.handle(addRequest),
      RequestHandler.handle(removeRequest)
    )
    val (response, _) = Action.sequence(actions)(lib)

    response.last shouldEqual """{"OK": {"message": "The book with id=1 has been removed"}}"""
  }

  it should "return an error message when attempting to remove a book absent in the library registry" in {
    val lib = Library(Map(), 0)
    val nonExistingBookId = 2137
    val removeRequest = s"""{"removeBook": {"id": $nonExistingBookId}}"""
    val (response, _) = RequestHandler.handle(removeRequest)(lib)
    response shouldEqual s"""{"ERROR": {"message": "There is not a book with id=$nonExistingBookId"}}"""
  }

  it should "return an error message when attepting to remove a book that is not currently available" in {
    val lib = Library(Map(), 0)
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 1, "userName": "kolacz"}}"""),
      RequestHandler.handle("""{"removeBook": {"id": 1}}""")
    )

    val (response, _) = Action.sequence(actions)(lib)
    response.last shouldEqual s"""{"ERROR": {"message": "Cannot delete the book with id=1, because it is lent"}}"""
  }

}
