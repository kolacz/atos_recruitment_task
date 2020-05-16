package net.atos.library

import org.json4s._
import org.json4s.native.JsonMethods._

object ActionHandler {

  import Action._

  def handle(request: String): LibraryAction = lib => {
    val json = parse(request)
    val JObject(List((actionName, _))) = json
    val argsJson = json \ actionName

    implicit val formats: Formats = DefaultFormats.withStrictOptionParsing

    val action = actionName match {
      case "addBook"     => argsJson.extract[AddBook]
      case "removeBook"  => argsJson.extract[RemoveBook]
      case "listBooks"   => argsJson.extract[ListBooks]
      case "searchBook"  => argsJson.extract[SearchBook]
      case "lentBook"    => argsJson.extract[LendBook]
      case "bookDetails" => argsJson.extract[BookDetails]
      case _ => ???
    }

    action.perform(lib)
  }

}
