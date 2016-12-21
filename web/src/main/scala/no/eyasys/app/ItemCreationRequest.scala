package no.eyasys.app

import akka.actor.{Actor, ActorRef}

/**
  * Created by maria on 21/12/16.
  */
class ItemCreationRequest() extends Actor{

  def receive = {
    case StartCreatingRequest(actorResponse) =>
      println("Creating item request")
      actorResponse ! CreateItem()
    case _ => println("received unknown message")
  }

}

case class StartCreatingRequest(actorRef: ActorRef)
