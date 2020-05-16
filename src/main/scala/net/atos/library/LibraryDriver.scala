package net.atos.library

import Library._

object LibraryDriver extends App {

  Iterator.continually(io.StdIn.readLine)
    .takeWhile(_.nonEmpty)
    .foldLeft(Action.unit("Initialization")(Library(inventory = Map[Id, Book](), currentId = 0))) {
      case ((_, lib), line) =>
        val (resp, updatedLib) = RequestHandler.handle(line)(lib)
        processResponse(resp)
        (resp, updatedLib)
    }

  def processResponse(response: String): Unit = {
    println(response)
  }
    
}

