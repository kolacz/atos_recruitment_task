package net.atos.library

import Library._

/**
  * Library contains books to lend (at the beginning there are no books in the library)
  *
  * @param inventory
  * @param currentId
  */
final case class Library(inventory: Map[Id, Book], currentId: Id)

object Library {
  type Id = Long
}
