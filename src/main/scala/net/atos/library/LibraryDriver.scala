package net.atos.library

import Library._

/**
  * The main object of the purely functional implementation of the Library logic.
  */
object LibraryDriver extends App {

  Iterator.continually(io.StdIn.readLine)
    .takeWhile(_.nonEmpty)
    .foldLeft(Action.unit(())(Library(inventory = Map[Id, Book](), currentId = 0))) {
      case ((_, lib), line) =>
        Action.map(RequestHandler.handle(line))(processResponse)(lib)
    }

  def processResponse(response: String): Unit = {
    println(response)
  }
    
}

