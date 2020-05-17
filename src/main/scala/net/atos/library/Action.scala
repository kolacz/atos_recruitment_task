package net.atos.library

import Action._

sealed trait Action {
  def perform: LibraryAction[String]
}

case class AddBook(title: String, year: Int, author: String) extends Action {
  def perform: LibraryAction[String] = lib => {
    val nextId = lib.currentId + 1
    val newBook = Book(nextId, title, year, author, isAvailable = true)
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
      .groupBy{ case (a,b) => (b.title, b.year, b.author)}
      .transform((a,b) => b.map(_._2.isAvailable))
      .toList.map{ case (book, summaries) => 
        val (available, lent) = summaries.foldLeft((0,0)){ case ((acc1, acc2), b) =>
          if (b) (acc1 + 1, acc2) else (acc1, acc2 + 1) }
        s"(title=${book._1}, year=${book._2}, author=${book._3}, available=$available, lent=$lent)"
      }.mkString("""["""", """", """", """"]""")
    (s"""{"OK": {"message": $listing}}""", lib)
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
    val bookToLend = lib.inventory get id
    if (bookToLend.isDefined) {
      val Book(_, title, year, author, isAvailable, lentBy) = bookToLend.get
      if (isAvailable) {
        val newEntry = (id -> Book(id, title, year, author, false, Some(userName)))
        val updatedLib = Library(lib.inventory - id + newEntry, lib.currentId)
        (s"""{"OK": {"message": "User $userName has lent the book with id=$id"}}""", updatedLib)
      } else
        (s"""{"ERROR": {"message": "The book with id=$id is already lent"}}""", lib)
    } else 
      (s"""{"ERROR": {"message": "There is not a book with id=$id"}}""", lib)
  }
}

case class BookDetails(id: Long) extends Action {
  def perform: LibraryAction[String] = lib => {
    val book = lib.inventory get id
    if (book.isDefined)
      (s"""{"OK": {"message": "Book details: ${book.get}"}}""", lib)
    else
      (s"""{"ERROR": {"message": "There is not a book with id=$id"}}""", lib)
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
