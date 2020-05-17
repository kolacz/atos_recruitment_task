package net.atos.library

import Library._

/**
  * Library contains books to lend (at the beginning there are no books in the library)
  *
  * @param inventory - the main data structure used to store and retreive book informations
  * @param currentId - an auto-incrementing Id, which corresponds to last added book's Id 
  */
final case class Library(inventory: Map[Id, Book], currentId: Id)

object Library {
  type Id = Long
}
