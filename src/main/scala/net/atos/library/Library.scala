package net.atos.library

import Library._

final case class Library(inventory: Map[Id, Book], currentId: Id)

object Library {
  type Id = Long
}
