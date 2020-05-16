package net.atos.library

import org.json4s._
import org.json4s.native.JsonMethods._

object ActionHandler {

  def handle(request: String): Unit = {
    val json = parse(request)
    val JObject(List((actionName, _))) = json
    val argsJson = json \ actionName

    implicit val formats: Formats = DefaultFormats.withStrictOptionParsing

    val action = actionName match {
      case "addBook"     => argsJson.extract[AddBook]
      case "removeBook"  => argsJson.extract[RemoveBook]
      case "listBooks"   => argsJson.extract[ListBooks]
      case "searchBook"  => argsJson.extract[SearchBook]
      case "lentBook"    => argsJson.extract[LentBook]
      case "bookDetails" => argsJson.extract[BookDetails]
      case _ => ???
    }

    println(action)
  }

}
