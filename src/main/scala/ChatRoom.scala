import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Merge, Sink, Source, Flow}

/**
  * Created by rajeevprasanna on 4/19/16.
  */

object ChatRoom {
  def apply(roomId: Int)(implicit actorSystem: ActorSystem) = new ChatRoom(roomId, actorSystem)
}

class ChatRoom(roomId: Int, actorSystem: ActorSystem) {

  private[this] val chatRoomActor = actorSystem.actorOf(Props(classOf[ChatRoomActor], roomId))

  def sendMessage(message: ChatMessage): Unit = chatRoomActor ! message

  def websocketFlow(user: String): Flow[Message, Message, _] =
    Flow(Source.actorRef[ChatMessage](bufferSize = 5, OverflowStrategy.fail)) {
      implicit builder =>
        chatSource => //source provideed as argument

          import akka.stream.scaladsl.FlowGraph.Implicits._

          //flow used as input it takes Message's
          val fromWebsocket = builder.add(
            Flow[Message].collect {
              case TextMessage.Strict(txt) => IncomingMessage(user, txt)
            })

          //flow used as output, it returns Message's
          val backToWebsocket = builder.add(
            Flow[ChatMessage].map {
              case ChatMessage(author, text) => TextMessage(s"[$author]: $text")
            }
          )

          //send messages to the actor, if send also UserLeft(user) before stream completes.
          val chatActorSink = Sink.actorRef[ChatEvent](chatRoomActor, UserLeft(user))

          //merges both pipes
          val merge = builder.add(Merge[ChatEvent](2))

          //Materialized value of Actor who sit in chatroom
          val actorAsSource = builder.materializedValue.map(actor => UserJoined(user, actor))

          //Message from websocket is converted into IncommingMessage and should be send to each in room
          fromWebsocket ~> merge.in(0)

          //If Source actor is just created should be send as UserJoined and registered as particiant in room
          actorAsSource ~> merge.in(1)

          //Merges both pipes above and forward messages to chatroom Represented by ChatRoomActor
          merge ~> chatActorSink

          //Actor already sit in chatRoom so each message from room is used as source and pushed back into websocket
          chatSource ~> backToWebsocket

          // expose ports
          (fromWebsocket.inlet, backToWebsocket.outlet)
    }
}