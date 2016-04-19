import akka.actor.ActorRef

/**
  * Created by rajeevprasanna on 4/19/16.
  */

//This class holds all model objects for communication in system

case class ChatMessage(sender: String, text: String)
object SystemMessage {
  def apply(text: String) = ChatMessage("System", text)
}

sealed trait ChatEvent
case class UserJoined(userName:String, userActor:ActorRef) extends ChatEvent
case class UserLeft(userName:String) extends ChatEvent
case class IncomingMessage(sender:String, text:String) extends ChatEvent