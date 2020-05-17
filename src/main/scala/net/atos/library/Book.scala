package net.atos.library

/**
  * Book consist of title, year and author.
  * Each book has an unique identifier (Id) across application.
  *
  * @param title
  * @param year
  * @param author
  * @param isAvailable - if it's possible to lend the book
  * @param lentBy - a name of user who has lent the book
  */
final case class Book(id: Library.Id,
                      title: String,
                      year: Int, 
                      author: String, 
                      isAvailable: Boolean,
                      lentBy: Option[String] = None) {

  override def toString: String = s"(id=$id, title=$title, year=$year, author=$author, isAvailable=$isAvailable, lentBy=${if (lentBy.isDefined) lentBy.get else "nobody"})"

}