package no.eyasys.app
import com.typesafe.config.ConfigFactory
import java.sql.Connection
import java.sql.DriverManager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import io.circe.syntax._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.CirceSupport._

import scala.io.StdIn
import scalaj.http.{Http, HttpOptions}

/**
 * @author ${user.name}
 */
object App {

  private val config = ConfigFactory.load()
  private val book = new Book(1,"War and Peace","Tolstoy L.N.",1000, 25)
  var bookStore:Array[Book] = new Array[Book](10)
  bookStore(1) = book

  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)

  def dbConnect (): Unit = {

    val dbType = config.getString("db.type")
    val dbConfig = config.getConfig(dbType)
    println("dbType = " + dbType)
    val driver = dbConfig.getString("driver")
    val url = dbConfig.getString("url")
    val username = dbConfig.getString("username")
    val password = dbConfig.getString("password")

    var connection:Connection = null

    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)

    val statement = connection.createStatement()

    if (dbType == "db.h2.mem"){
      statement.execute("CREATE TABLE PERSON (id int primary key, name varchar(255))")
      statement.executeUpdate("INSERT INTO PERSON (id, name) VALUES (1,'Maria')")
    }

    val resultSet = statement.executeQuery("SELECT id,name FROM person")
    while (resultSet.next()){
      val id = resultSet.getString("id")
      val name = resultSet.getString("name")
      println("id,name = " + id + ", " + name)
    }

    connection.close()

  }

  def orderBooks(): Unit = {
    val order = new Order(1,1,20,0)
    var result = scalaj.http.Http("http://localhost:8080/test").postData(order.asJson.toString)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    println("Printing result: " + result.body)

  }

  def handleOrder(order: Order): String = {
    val orderedBook = bookStore(order.itemId)
    order.totalOrderValue = order.amount*orderedBook.price
    order.asJson.toString()
    //order.totalOrderValue
  }
  
  def main(args : Array[String]) {

    //println("concat arguments = " + foo(args))

    //dbConnect()

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("test") {
        get {
          complete(book.asJson.toString())
        } ~
        post{
          decodeRequest{
            entity(as[Order]) { order =>
              complete(handleOrder(order))
            }
          }
        }
      }

    val bindingFuture = akka.http.scaladsl.Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    orderBooks()

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

case class Book(id: Int, name: String, author: String, pages: Int, price: Int){}

case class Order(id: Int, itemId: Int, amount: Int,var totalOrderValue: Int){
}