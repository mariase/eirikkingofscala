package no.eyasys.app

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable.ListBuffer

/**
  * Created by maria on 21/12/16.
  */
class ItemCreation() extends Actor{

  private var localBookStore: Array[Book] = null
  private var requestSender: Option[ActorRef] = None

  def receive = {
    case InitializeActor(bookStore) =>
      println("Actor initialized")
      localBookStore = bookStore
      requestSender = Some(sender)
    case CreateItem() =>
      println("Creating item")
      localBookStore(2) = new Book(2,"Idiot","Dostoevsky",450,20)
      requestSender.map(_ ! localBookStore)
    case _ => println("received unknown message")
  }

}

case class InitializeActor(var bookStore : Array[Book])
case class CreateItem()