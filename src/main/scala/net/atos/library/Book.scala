package net.atos.library

final case class Book(title: String, 
                      year: Int, 
                      author: String, 
                      isAvailable: Boolean,
                      lentBy: Option[String] = None) {

  override def toString: String = s"Book(title=$title, year=$year, author=$author, isAvailable=$isAvailable, lentBy=${lentBy getOrElse "nobody"})"

}