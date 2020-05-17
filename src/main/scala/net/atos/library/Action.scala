package net.atos.library

import Action._

sealed trait Action {
  def perform: LibraryAction[String]
}

case class AddBook(title: String, year: Int, author: String) extends Action {
  def perform: LibraryAction[String] = lib => {
    val nextId = lib.currentId + 1
    val newBook = Book(title, year, author, isAvailable = true)
    val newEntry = (nextId -> newBook)
    val updatedLib = Library(lib.inventory + newEntry, nextId)
    (s"""{"OK": {"message": "$newBook has been added"}}""", updatedLib)
  }
}

case class RemoveBook(id: Long) extends Action {
  def perform: LibraryAction[String] = lib => {
    val bookToRemoval = lib.inventory get id
    if (bookToRemoval.isDefined)
      if (bookToRemoval.get.isAvailable) {
        val updatedLib = Library(lib.inventory - id, lib.currentId)
        (s"""{"OK": {"message": "The book with id=$id has been removed"}}""", updatedLib)
      } else 
        (s"""{"ERROR": {"message": "Cannot delete the book with id=$id, because it is lent"}}""", lib)
    else
      (s"""{"ERROR": {"message": "There is not a book with id=$id"}}""", lib)
  }
}

case class ListBooks() extends Action {
  def perform: LibraryAction[String] = lib => {
    val listing = lib.inventory
      .groupBy(_._2).transform((a,b) => b.size)
      .groupBy(x => s"${x._1.title} ${x._1.year} ${x._1.author}")
      .transform((a,b) => b.toList.map(x => (x._1.isAvailable, x._2)))
      .toList.map{
        case (book, summaries) => book + " " + summaries.map{
          case (true,  k) => s"in: $k"
          case (false, k) => s"out: $k"
        }.mkString("(", ", ", ")")
      }.mkString("\n")
    (s"""{"OK": {"message": "$listing"}}""", lib)
  }
}

case class SearchBook(title: Option[String], year: Option[Int], author: Option[String]) extends Action {
  def perform: LibraryAction[String] = lib => {
    val condition: Book => Boolean = book =>
      book.title.contains(title getOrElse "") &&
      book.year.equals(year getOrElse book.year) &&
      book.author.contains(author getOrElse "")

    val results = lib.inventory.filter{ case (id, book) => condition(book)}.toList.mkString("[", ",\n", "]")

    (s"""{"OK": {"message": $results}}""", lib)
  }
}

case class LendBook(id: Long, userName: String) extends Action {
  def perform: LibraryAction[String] = lib => {
    val Book(title, year, author, isAvailable, lentBy) = lib.inventory(id)
    if (isAvailable) {
      val newEntry = (id -> Book(title, year, author, false, Some(userName)))
      val updatedLib = Library(lib.inventory - id + newEntry, lib.currentId)
      (s"""{"OK": {"message": "User $userName has lent the book with id=$id"}}""", updatedLib)
    } else
      (s"""{"ERROR": {"message": "The book with id=$id is already lent"}}""", lib)
  }
}

case class BookDetails(id: Long) extends Action {
  def perform: LibraryAction[String] = lib => {
    val book = lib.inventory(id)
    (s"""{"OK": {"message": "Book details: $book"}}""", lib)
  }
}

case class VoidAction(message: String) extends Action {
  def perform: LibraryAction[String] = unit(message)
}

object Action {

  type LibraryAction[A] = Library => (A, Library)

  def unit[A](a: A): LibraryAction[A] = lib => (a, lib)

  def map[A,B](action: LibraryAction[A])(f: A => B): LibraryAction[B] =
    lib => {
      val (s, lib2) = action(lib)
      (f(s), lib2)
    }

  def flatMap[A,B](action: LibraryAction[A])(g: A => LibraryAction[B]): LibraryAction[B] =
    lib => {
      val (s, lib2) = action(lib)
      g(s)(lib2)
    }

}
