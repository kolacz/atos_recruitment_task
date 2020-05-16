package net.atos.library

object LibraryDriver extends App {

  Iterator.continually(io.StdIn.readLine)
    .takeWhile(_.nonEmpty)
    .foreach(line => ActionHandler.handle(line))
    
}

