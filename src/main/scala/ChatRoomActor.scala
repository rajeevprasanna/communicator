import akka.actor.{ActorRef, Actor}

/**
  * Created by rajeevprasanna on 4/19/16.
  */
class ChatRoomActor(roomId:String) extends Actor {

  var participants: Map[String, ActorRef] = Map.empty[String, ActorRef]

  def receive: Receive = {
    case UserJoined(name, actorRef) =>  participants += (name -> actorRef)
                                        broadcastMessage(SystemMessage(s"User $name joined channel ..."))
                                        println(s"User $name joined channel[$roomId]")

    case UserLeft(name) =>  println(s"User $name left channel[$roomId]")
                            broadcastMessage(SystemMessage(s"User $name left channel[$roomId]"))
                            participants -= name

    case IncomingMessage(sender, text) => broadcastMessage(ChatMessage(sender, text))
  }

  def broadcastMessage(text: ChatMessage) = participants.values.map(_ ! text)

}