package net.atos.library

import org.json4s._
import org.json4s.native.JsonMethods._
import scala.util.Try

object RequestHandler {

  import Action._

  implicit val formats: Formats = DefaultFormats.withStrictOptionParsing

  def handle(request: String): LibraryAction[String] = lib => {
    val json = Try{ parse(request) } getOrElse new JObject(List(("wrongInput", null.asInstanceOf[JValue])))
    val actionName = json match {
      case JObject(List((actionName, _))) => actionName
      case JObject(Nil) => "wrongInput"
    }

    val argsJson = json \ actionName
    val action = actionName match {
      case "addBook"     => argsJson.extract[AddBook]
      case "removeBook"  => argsJson.extract[RemoveBook]
      case "listBooks"   => argsJson.extract[ListBooks]
      case "searchBook"  => argsJson.extract[SearchBook]
      case "lendBook"    => argsJson.extract[LendBook]
      case "bookDetails" => argsJson.extract[BookDetails]
      case req =>
        VoidAction(s"""{"ERROR": {"message": "${if (req equals "wrongInput") "Wrong input!" else s"Unknown request: '$req'"}"}}""")
    }

    action.perform(lib)
  }

}
