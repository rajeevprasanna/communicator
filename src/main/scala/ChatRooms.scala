import akka.actor.ActorSystem

/**
  * Created by rajeevprasanna on 4/19/16.
  */

object ChatRooms {

  var chatRooms:Map[Int, ChatRoom] = Map.empty[Int, ChatRoom]

  def findOrCreateRoom(number:Int)(implicit actorSystem: ActorSystem):ChatRoom = chatRooms.getOrElse(number, createNewChatRoom(number))

  private def createNewChatRoom(number:Int)(implicit actorSystem: ActorSystem):ChatRoom = {
    val chatRoom = ChatRoom(number)
    chatRooms += number -> chatRoom
    chatRoom
  }
}
