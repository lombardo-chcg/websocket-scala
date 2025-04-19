package runnable

object HelloWorld {

  def main(args: Array[String]): Unit = {

    // example 1
    import net.domlom.websocket._

    // Create a WebsocketBehavior instance and define handlers for Websocket lifecycle events.
    // This example uses a builder pattern but it is just a case class underneath.
    val msg = s"Hello World"
    val behavior = {
      WebsocketBehavior.empty
        .setOnOpen { connection =>
          println("Connection Open")
          connection.send(msg)
        }
        .setOnMessage { (connection, message) =>
          println(s"Message from server: ${message.value}")

          if (message.value == msg) {
            println("Received echo - closing connection.")
            connection.close()
          }
        }
        .setOnClose { closeDetails =>
          println(closeDetails)
        }
        .setOnError { (connection, throwable) =>
          println(s"Exception - closing connection. ${throwable.getMessage}")
          connection.close()
        }
    }

    // initialize a client
    val socket = Websocket("wss://echo.websocket.org", behavior)

    // say hello
    socket.connect()

    // example 2
    val sock = Websocket("wss://echo.websocket.org", behavior)
    println(sock.connect().map(_.message))
    // Success(Websocket Connected)

    sock.close()

    // example 3
    def doBusinessValueStuff(a: WsMessage): String = a.value.toUpperCase

    val myBaseWsTemplate =
      WebsocketBehavior.empty
        .setOnOpen { connection =>
          connection.send("subscribe request")
        }
        .setOnClose { reason =>
          println(reason)
        }
        .setOnError { (connection, throwable) =>
          println(throwable.getMessage)
        }

    // Step 2 - extend the base template with specific business logic
    val mySpecificUseCase = myBaseWsTemplate.setOnMessage { (connection, message) =>
      val response = doBusinessValueStuff(message)
      connection.send(response)
    }
  }
}
