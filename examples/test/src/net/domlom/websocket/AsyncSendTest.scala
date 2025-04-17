package net.domlom.websocket

import net.domlom.websocket.util.TestUtil
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class AsyncSendTest extends AnyFunSuite with Eventually with BeforeAndAfterAll {

  private implicit val ec: ExecutionContext = ExecutionContext.global

  val (server, echoEndpoint) = TestUtil.server

  val socket = Websocket(
    echoEndpoint,
    WebsocketBehavior.empty
      .setOnMessage((_, m) => rcd = m.value),
    debugMode = true
  )
  var rcd = ""

  override def beforeAll() = {
    server.start()
    socket.connect()
  }

  override def afterAll() = {
    server.stop()
    socket.close()
  }

  test("sendAsync sanity check") {
    val message = System.currentTimeMillis().toString

    val res = Await.result(socket.sendAsync(message), 3.seconds)

    println(res)

    assert(res.message.contains("sendAsync success"))

    assert(rcd == message)
  }
}
