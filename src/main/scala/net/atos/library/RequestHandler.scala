package net.atos.library

import org.json4s._
import org.json4s.native.JsonMethods._
import scala.util.Try

/**
  * Object that parses a JSON request, chooses appropriate action and returns corresponding LibraryAction.
  * In the case of an incorrect JSON or unknown `actionName` it returns a void action with an error message.
  */
object RequestHandler {

  import Action._

  implicit val formats: Formats = DefaultFormats.withStrictOptionParsing

  def handle(request: String): LibraryAction[String] = lib => {
    val json = parseJson(request)
    val actionName = retrieveActionName(json)
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

  def parseJson(jsonString: String): JValue = {
    Try{ parse(jsonString) } getOrElse new JObject(List(("wrongInput", null.asInstanceOf[JValue])))
  }

  def retrieveActionName(json: JValue): String = json match {
    case JObject(List((actionName, _))) => actionName
    case _ => "wrongInput"
  }

}
