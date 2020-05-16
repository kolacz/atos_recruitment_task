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
case class RemoveBook(id: Long) extends Action
case class ListBooks() extends Action
case class SearchBook(title: Option[String], year: Option[Int], author: Option[String]) extends Action
case class LentBook(id: Long) extends Action
case class BookDetails(id: Long) extends Action

object Action {
  
  type LibraryAction = Library => (String, Library)

  def unit(s: String): LibraryAction = lib => (s, lib)
}
