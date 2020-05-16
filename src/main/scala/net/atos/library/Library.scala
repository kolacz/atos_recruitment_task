package net.atos.library


final case class Library(inventory: Map[Long, Book], currentId: Long)
