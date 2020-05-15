package net.atos.library

object LibraryDriver extends App {

  Iterator.continually(scala.io.StdIn.readLine).
    takeWhile(_.nonEmpty).
    foreach(line => println(line))
    
}

