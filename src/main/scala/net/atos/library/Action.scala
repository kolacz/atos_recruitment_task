package net.atos.library

sealed trait Action
case class AddBook(title: String, year: Int, author: String) extends Action
case class RemoveBook(id: Long) extends Action
case class ListBooks() extends Action
case class SearchBook(title: Option[String], year: Option[Int], author: Option[String]) extends Action
case class LentBook(id: Long) extends Action
case class BookDetails(id: Long) extends Action
