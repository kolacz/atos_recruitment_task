package net.atos.library

import org.scalatest.Assertions._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RequestHandlerSpec extends AnyFlatSpec with Matchers {
  "The RequestHandler object" should "be able to add a new book into the library registry" in {
    val lib = Library(Map(), 0)
    val addRequest = """{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""
    val (response, lib2) = RequestHandler.handle(addRequest)(lib)
    response shouldEqual s"""{"OK": {"message": "Book(id=${lib2.currentId}, title=Purely Functional Data Structures, year=1996, author=Chris Okasaki, isAvailable=true) has been added"}}"""
  }

  it should "be able to remove a book from the library registry" in {
    val lib = Library(Map(), 0)
    val addRequest = """{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""
    val (_, lib2) = RequestHandler.handle(addRequest)(lib)

    // assert(lib2.currentId === 1)
    val removeRequest = s"""{"removeBook": {"id": ${lib2.currentId}}}"""
    val (response, _) = RequestHandler.handle(removeRequest)(lib2)
    response shouldEqual s"""{"OK": {"message": "The book with id=${lib2.currentId} has been removed"}}"""
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
    val addRequest = """{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""
    val (_, lib2) = RequestHandler.handle(addRequest)(lib)
    val currentId = lib2.currentId
    val lendRequest = s"""{"lendBook": {"id": $currentId, "userName": "kolacz"}}"""
    val (_, lib3) = RequestHandler.handle(lendRequest)(lib2)
    val removeRequest = s"""{"removeBook": {"id": $currentId}}"""
    val (response, _) = RequestHandler.handle(removeRequest)(lib3)
    response shouldEqual s"""{"ERROR": {"message": "Cannot delete the book with id=$currentId, because it is lent"}}"""
  }

}
