package net.atos.library

object LibraryDriver extends App {

  Iterator.continually(io.StdIn.readLine)
    .takeWhile(_.nonEmpty)
    .foldLeft(Action.unit("Initialization")(Library(inventory = Map[Long, Book](), currentId = 0))) { 
      case ((_, lib), line) =>
        val (response, updatedLib) = ActionHandler.handle(line)(lib)
        println(response)
        (response, updatedLib)
    }
    
}

