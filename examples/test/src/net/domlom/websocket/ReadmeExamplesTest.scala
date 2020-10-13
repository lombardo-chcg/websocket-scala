package net.domlom.websocket

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ReadmeExamplesTest extends AnyFunSuite with Matchers {

  // setup a behavior to println received messages
  val behavior =
    WebsocketBehavior.empty
      .setOnMessage { (_, message) =>
        println(s"Rec'd message: ${message.value}")
      }

  // initialize a client
  val socket = Websocket("wss://echo.websocket.org", behavior)

  // say hello
  for {
    _ <- socket.connect()
    _ <- socket.send(s"Hello World")
    _ = Thread.sleep(500)
    _ <- socket.close()
  } yield ()

  val myHeaders = Map("Authorization" -> "Bearer xxxxxxxxxxxxxx")

  val sock = Websocket(
    url = "wss://echo.websocket.org",
    behavior = WebsocketBehavior.empty,
    requestHeaders = myHeaders
  )

  lazy val x = sock.connect()

  test("make sure readme example code compiles") {}
}
