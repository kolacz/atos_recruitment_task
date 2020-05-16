package net.atos.library

import Action._

sealed trait Action {
  def perform: LibraryAction
}
case class AddBook(title: String, year: Int, author: String) extends Action {
  def perform: LibraryAction = lib => {
    val nextId = lib.currentId + 1
    val newEntry = (nextId -> Book(title, year, author, isAvailable = true))
    val updatedLib = Library(lib.inventory + newEntry, nextId)
    ("OK", updatedLib)
  }
}
case class RemoveBook(id: Long) extends Action {
  def perform: LibraryAction = lib => {
    val updatedLib = Library(lib.inventory - id, lib.currentId)
    ("OK", updatedLib)
  }
}
case class ListBooks() extends Action {
  def perform: LibraryAction = lib => {
    val listing = lib.inventory
      .groupBy(_._2).transform((a,b) => b.size)
      .groupBy(x => s"${x._1.title} ${x._1.year} ${x._1.author}")
      .transform((a,b) => b.toList.map(x => (x._1.isAvailable, x._2)))
      .toList.map{
        case (book, summaries) => book + " " + summaries.map{
          case (true, k)  => s"in: $k"
          case (false, k) => s"out: $k"
        }.mkString("(", ", ", ")")
      }.mkString("\n")
    (listing, lib)
  }
}
case class SearchBook(title: Option[String], year: Option[Int], author: Option[String]) extends Action {
  def perform: LibraryAction = lib => {
    ("", lib)
  }
}
case class LendBook(id: Long, userName: String) extends Action {
  def perform: LibraryAction = lib => {
    val Book(title, year, author, isAvailable, lentBy) = lib.inventory(id)
    if (isAvailable) {
      val newEntry = (id -> Book(title, year, author, false, Some(userName)))
      val updatedLib = Library(lib.inventory - id + newEntry, lib.currentId)
      (s"User $userName has lent the book with id $id", updatedLib)
    } else
      (s"Book with id $id is already lent", lib)
  }
}
case class BookDetails(id: Long) extends Action {
  def perform: LibraryAction = lib => {
    val book = lib.inventory(id)
    (s"$book", lib)
  }
}

object Action {
  
  type LibraryAction = Library => (String, Library)
  def unit(s: String): LibraryAction = lib => (s, lib)

}
