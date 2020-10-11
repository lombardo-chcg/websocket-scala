package runnable

object HelloWorld {

  def main(args: Array[String]): Unit = {

    // example 1
    import net.domlom.websocket._

    // setup a behavior to println received messages
    val behavior = {
      WebsocketBehavior.empty
        .setOnMessage { (_, message) =>
          println(s"Message from server: ${message.value}")
        }
        .setOnClose(reason => println(reason))
    }

    // initialize a client
    val socket = Websocket("wss://echo.websocket.org", behavior)

    // say hello
    for {
      _ <- socket.connect()
      _ <- socket.send(s"Hello World")
      _ = Thread.sleep(500)
      r <- socket.close()
    } yield r
    // Message from server: Hello World

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
