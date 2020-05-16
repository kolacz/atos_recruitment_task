package net.atos.library

object LibraryDriver extends App {

  Iterator.continually(io.StdIn.readLine)
    .takeWhile(_.nonEmpty)
    .foldLeft(Action.unit("init")(Library(inventory = Map[Long, Book](), currentId = 0))) { 
      case ((response, lib), line) => 
        println(response)
        ActionHandler.handle(line)(lib)
    }
    
}

