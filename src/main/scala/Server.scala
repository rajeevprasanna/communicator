import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.http.scaladsl.server.{Route, Directives}
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Flow

import scala.io.StdIn

/**
  * Created by rajeevprasanna on 4/19/16.
  */
object Server extends App {

  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  import actorSystem.dispatcher

  val config = actorSystem.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage(s"Echo : $txt")
    case _ => TextMessage("Message type not supported")
  }

    import Directives._

    val route = get {
      pathEndOrSingleSlash {
        complete("Welcome to websocket server")
      }
    } ~
      path("ws-echo") {
        get {
          handleWebsocketMessages(echoService)
        }
      } ~
      pathPrefix("ws-chat") { //chatId =>
        parameter('name) { userName =>
          val chatId = 1000
          handleWebsocketMessages(ChatRooms.findOrCreateRoom(chatId).websocketFlow(userName))
        }
      }

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"server is now online at http://$interface:$port\n Press RETURN to stop...")
  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.shutdown())
  println("Server is down...")


//  object MainService {
//    def route: Route = pathEndOrSingleSlash {
//      complete("Welcome to websocket server")
//    }
//  }
//
//  object EchoService {
//    def route: Route = path("ws-echo") {
//      get {
//        handleWebsocketMessages(echoService)
//      }
//    }
//  }
//
//  object ChatService {
//    def route(implicit actorSystem: ActorSystem, materializer: Materializer): Route =
//      pathPrefix("ws-chat" / IntNumber) { chatId =>
//        parameter('name) { userName =>
//          handleWebsocketMessages(ChatRooms.findOrCreateRoom(chatId).websocketFlow(userName))
//        }
//      }
//  }
//
//  val route = MainService.route ~
//    EchoService.route ~
//    ChatService.route
}