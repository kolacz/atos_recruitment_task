package net.atos.library

/**
  * Book consist of title, year and author.
  * Each book should have unique identifier (ID) across application.
  *
  * @param title
  * @param year
  * @param author
  * @param isAvailable
  * @param lentBy
  */
final case class Book(id: Library.Id,
                      title: String,
                      year: Int, 
                      author: String, 
                      isAvailable: Boolean,
                      lentBy: Option[String] = None) {

  override def toString: String = s"Book(id=$id, title=$title, year=$year, author=$author, isAvailable=$isAvailable)"

}