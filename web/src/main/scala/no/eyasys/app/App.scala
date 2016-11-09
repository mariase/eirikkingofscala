package no.eyasys.app
import com.typesafe.config.ConfigFactory
import java.sql.Connection
import java.sql.DriverManager

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
    println("concat arguments = " + foo(args))

    //val dbType = args(0)
    //println("dbType = " + args(0))

    dbConnect()

  }

}
