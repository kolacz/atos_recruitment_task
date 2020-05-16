package net.atos.library

final case class Book(title: String, 
                      year: Int, 
                      author: String, 
                      isAvailable: Boolean,
                      lentBy: Option[String] = None)