package net.atos.library

import org.scalatest.Assertions._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RequestHandlerSpec extends AnyFlatSpec with Matchers {
  "The RequestHandler object" should "provide an action that adds a new book into the library registry" in {
    val addRequest = """{"addBook": {"title": "Purely Functional Data Structures", "year": 1996, "author": "Chris Okasaki"}}"""
    val (response, lib2) = RequestHandler.handle(addRequest)(Library(Map(), 0))
    response shouldEqual s"""{"OK": {"message": "(id=${lib2.currentId}, title=Purely Functional Data Structures, year=1996, author=Chris Okasaki, isAvailable=true, lentBy=nobody) has been added"}}"""
  }

  it should "provide an action that removes a book from the library registry" in {
    val addRequest = """{"addBook": {"title": "Functional Programming in Scala", "year": 2014, "author": "Paul Chiusano"}}"""
    val removeRequest = """{"removeBook": {"id": 1}}"""
    val actions = List(
      RequestHandler.handle(addRequest),
      RequestHandler.handle(removeRequest)
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.last shouldEqual """{"OK": {"message": "The book with id=1 has been removed"}}"""
  }

  it should "return an action that responses with an error message when attempting to remove a book absent in the library registry" in {
    val removeRequest = s"""{"removeBook": {"id": 2137}}"""
    val (response, _) = RequestHandler.handle(removeRequest)(Library(Map(), 0))
    response shouldEqual s"""{"ERROR": {"message": "There is not a book with id=2137"}}"""
  }

  it should "return an action that responses with an error message when attepting to remove a book that is not currently available" in {
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "Introdutcion to Algorithm", "year": 1990, "author": "Thomas H. Cormen"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 1, "userName": "kolacz"}}"""),
      RequestHandler.handle("""{"removeBook": {"id": 1}}""")
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.last shouldEqual s"""{"ERROR": {"message": "Cannot delete the book with id=1, because it is lent"}}"""
  }

  it should "provide an action that lists all books in the library (distinctly)" in {
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "COVID-19", "year": 2020, "author": "Bill Gates"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "COVID-19", "year": 2020, "author": "Bill Gates"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "COVID-19", "year": 2020, "author": "Bill Gates"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "9/11", "year": 2001, "author": "George Bush"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "9/11", "year": 2001, "author": "George Bush"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 1, "userName": "foo"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 2, "userName": "bar"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 4, "userName": "baz"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Not Himself", "year": 2019, "author": "Jeffrey Epstein"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Not Himself", "year": 2019, "author": "Jeffrey Epstein"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Not Himself", "year": 2019, "author": "Jeffrey Epstein"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Not Himself", "year": 2019, "author": "Jeffrey Epstein"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 8, "userName": "buzz"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 9, "userName": "barfoo"}}"""),
      RequestHandler.handle("""{"listBooks": {}}""")
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.last shouldEqual """{"OK": {"message":
                                |["(title=9/11, year=2001, author=George Bush, available=1, lent=1)",
                                |"(title=COVID-19, year=2020, author=Bill Gates, available=1, lent=2)",
                                |"(title=Not Himself, year=2019, author=Jeffrey Epstein, available=2, lent=2)"]}}""".stripMargin.replaceAll("\n", " ")
  }

  it should "provide an action that searches a book by title/author/year" in {
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "Lorem ipsum dolor sit amet, vol. 3", "year": 1992, "author": "Jan Kowalski"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Consectetur adipiscing elit, vol. XII", "year": 1993, "author": "Jan Nowak"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Proin nibh augue, vol. 2", "year": 1994, "author": "Adam Kowalski"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Suscipit a", "year": 1995, "author": "Adam Nowak"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Scelerisque sed, vol. 1", "year": 1996, "author": "Bartosz Janusz"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Lacinia in, mi, vol. 7", "year": 1997, "author": "Janusz Kowal"}}"""),
      RequestHandler.handle("""{"searchBook": {"title": "vol", "author": "Kowal"}}""")
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.last shouldEqual """{"OK": {"message":
      |["(id=1, title=Lorem ipsum dolor sit amet, vol. 3, year=1992, author=Jan Kowalski, isAvailable=true, lentBy=nobody)",
      |"(id=3, title=Proin nibh augue, vol. 2, year=1994, author=Adam Kowalski, isAvailable=true, lentBy=nobody)",
      |"(id=6, title=Lacinia in, mi, vol. 7, year=1997, author=Janusz Kowal, isAvailable=true, lentBy=nobody)"]}}""".stripMargin.replaceAll("\n", " ")
  }

  it should "provide an action that returns an empty list when searching while library is empty" in {
    val searchRequest = """{"searchBook": {"year": 2020}}"""
    val (response, _) = RequestHandler.handle(searchRequest)(Library(Map(), 0))
    response shouldEqual s"""{"OK": {"message": []}}"""
  }

  it should "provide an action that allows the books to be lent" in {
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "Lorem ipsum dolor sit amet, vol. 3", "year": 1992, "author": "Jan Kowalski"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Consectetur adipiscing elit, vol. XII", "year": 1993, "author": "Jan Nowak"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Proin nibh augue, vol. 2", "year": 1994, "author": "Adam Kowalski"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Suscipit a", "year": 1995, "author": "Adam Nowak"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Scelerisque sed, vol. 1", "year": 1996, "author": "Bartosz Janusz"}}"""),
      RequestHandler.handle("""{"addBook": {"title": "Lacinia in, mi, vol. 7", "year": 1997, "author": "Janusz Kowal"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 1, "userName": "foo"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 3, "userName": "buzz"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 5, "userName": "bar"}}""")
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.takeRight(3) shouldEqual List(
      """{"OK": {"message": "User foo has lent the book with id=1"}}""",
      """{"OK": {"message": "User buzz has lent the book with id=3"}}""",
      """{"OK": {"message": "User bar has lent the book with id=5"}}"""
    )
  }

  it should "return an action that responses with an error message when attepting to lend a book already lent" in {
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "Lorem ipsum dolor sit amet, vol. 3", "year": 1992, "author": "Jan Kowalski"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 1, "userName": "foo"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 1, "userName": "buzz"}}""")
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.last shouldEqual """{"ERROR": {"message": "The book with id=1 is already lent"}}"""
  }

  it should "return an action that responses with an error message when attepting to lend a book that library doesn't have" in {
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "Lorem ipsum dolor sit amet, vol. 3", "year": 1992, "author": "Jan Kowalski"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 2137, "userName": "foo"}}""")
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.last shouldEqual """{"ERROR": {"message": "There is not a book with id=2137"}}"""
  }

  it should "provide an action that allows to look up book details" in {
    val actions = List(
      RequestHandler.handle("""{"addBook": {"title": "Lorem ipsum dolor sit amet, vol. 3", "year": 1992, "author": "Jan Kowalski"}}"""),
      RequestHandler.handle("""{"lendBook": {"id": 1, "userName": "foo"}}"""),
      RequestHandler.handle("""{"bookDetails": {"id": 1}}""")
    )
    val (response, _) = Action.sequence(actions)(Library(Map(), 0))
    response.last shouldEqual """{"OK": {"message": "(id=1, title=Lorem ipsum dolor sit amet, vol. 3, year=1992, author=Jan Kowalski, isAvailable=false, lentBy=foo)"}}"""
  }

  it should "return an action that responses with an error message when attepting to look up details of book that library doesn't have" in {
    val bookDetailsRequest = """{"bookDetails": {"id": 2137}}"""
    val (response, _) = RequestHandler.handle(bookDetailsRequest)(Library(Map(), 0))
    response shouldEqual """{"ERROR": {"message": "There is not a book with id=2137"}}"""
  }

  it should "be resistant for an input JSON with unknown request and return an error message" in {
    val incorrectRequest = """{"madeUpRequest": [1,2,3,4]}"""
    val (response, _) = RequestHandler.handle(incorrectRequest)(Library(Map(), 0))
    response shouldEqual """{"ERROR": {"message": "Unknown request: 'madeUpRequest'"}}"""
  }

  it should "be resistant for an incorrect input JSON and return an error message" in {
    val incorrectRequest = """{"abc":"""
    val (response, _) = RequestHandler.handle(incorrectRequest)(Library(Map(), 0))
    response shouldEqual """{"ERROR": {"message": "Wrong input!"}}"""
  }

}
