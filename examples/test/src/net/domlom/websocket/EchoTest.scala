package net.domlom.websocket

import net.domlom.websocket.model.ConnectionClosedDetails
import net.domlom.websocket.util.TestUtil
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ArrayBuffer

class EchoTest extends AnyFunSuite with Eventually with BeforeAndAfterAll {
  private val buffer = ArrayBuffer[Int]()

  private val behavior: WebsocketBehavior =
    WebsocketBehavior.empty
      .setOnOpen { connection =>
        println("OnOpen")
        connection.sendSync("0")
      }
      .setOnMessage { (connection, message) =>
        println(s"OnMessage(${System.currentTimeMillis}): $message")
        val n = message.value.toInt
        buffer += n
        if (n >= 4) connection.close() else connection.send(s"${n + 1}")
      }
      .setOnClose { reason =>
        assert(reason == ConnectionClosedDetails.wsSpecCodes(1000))
        println("OnClose: " + reason)
      }
      .setOnError { (connection, throwable) =>
        println("OnError: " + throwable.getMessage)
      }

  val (server, echoEndpoint) = TestUtil.server
  val socket                 = Websocket(echoEndpoint, behavior)

  override def beforeAll() {
    server.start()
  }

  override def afterAll() {
    server.stop()
  }

  test("counting to 4") {

    assert(!socket.isOpen)

    socket.connect()

    assert(socket.isOpen)

    eventually {
      assert(buffer.contains(4))
    }

    socket.close()

    assert(!socket.isOpen)
  }
}
