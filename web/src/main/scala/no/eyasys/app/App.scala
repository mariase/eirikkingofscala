package no.eyasys.app
import com.typesafe.config.ConfigFactory
import java.sql.Connection
import java.sql.DriverManager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.event.Logging

import scala.io.StdIn


/**
 * @author ${user.name}
 */
object App {

  private val config = ConfigFactory.load()

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
          complete("OK")
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
