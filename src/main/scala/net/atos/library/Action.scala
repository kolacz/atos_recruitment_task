package net.atos.library

import Action._

/**
  * Trait with companion object containing implementation of all library actions
  */
sealed trait Action {
  def perform: LibraryAction[String]
}

/**
  * Adds a new book to the library. Id is not passed as an argument,
  * it is incremented during this action automatically.
  *
  * @param title
  * @param year
  * @param author
  */
case class AddBook(title: String, year: Int, author: String) extends Action {
  def perform: LibraryAction[String] = lib => {
    val nextId = lib.currentId + 1
    val newBook = Book(nextId, title, year, author, isAvailable = true)
    val newEntry = (nextId -> newBook)
    val updatedLib = Library(lib.inventory + newEntry, nextId)
    (s"""{"OK": {"message": "$newBook has been added"}}""", updatedLib)
  }
}

/**
  * Removes a book from the library by Id. When the book has been already lent
  * or is not present in the registry it returns corresponding error message.
  *
  * @param id
  */
case class RemoveBook(id: Library.Id) extends Action {
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

/**
  * Returns a listing on all books in the library (distinctly) together with
  * an information how many books are available/lent.
  *
  */
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

/**
  * Returns a list of books that matches the provided search criteria.
  * Each criterion is optional.
  *
  * @param title - or its substring
  * @param year
  * @param author - e.g. his first name only
  */
case class SearchBook(title: Option[String], year: Option[Int], author: Option[String]) extends Action {
  def perform: LibraryAction[String] = lib => {
    val condition: Book => Boolean = book =>
      (book.title.toLowerCase) contains (title getOrElse "").toLowerCase &&
      (book.author.toLowerCase) contains (author getOrElse "").toLowerCase &&
      (book.year) equals (year getOrElse book.year)

    val results = lib.inventory.filter{ case (id, book) => condition(book)}.toList.mkString("[", ",\n", "]")

    (s"""{"OK": {"message": $results}}""", lib)
  }
}

/**
  * Changes the book's `isAvailable` status from `true` to `false`.
  * If a book has been already lent or there is not a book with a given Id,
  * it returns an error message.
  *
  * @param id
  * @param userName
  */
case class LendBook(id: Library.Id, userName: String) extends Action {
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

/**
  * Returns a pretty printed book info for a given Id.
  * If there is not a book with a given Id within the registry, it returns an error message.
  *
  * @param id
  */
case class BookDetails(id: Library.Id) extends Action {
  def perform: LibraryAction[String] = lib => {
    val book = lib.inventory get id
    if (book.isDefined)
      (s"""{"OK": {"message": "${book.get}"}}""", lib)
    else
      (s"""{"ERROR": {"message": "There is not a book with id=$id"}}""", lib)
  }
}

/**
  * A void action, that just forwards a message without changing any state.
  *
  * @param message
  */
case class VoidAction(message: String) extends Action {
  def perform: LibraryAction[String] = unit(message)
}

object Action {

  type LibraryAction[A] = Library => (A, Library)  // state action type alias

  def unit[A](a: A): LibraryAction[A] = lib => (a, lib)

  // Simple functors to deal with stateful transformations

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
