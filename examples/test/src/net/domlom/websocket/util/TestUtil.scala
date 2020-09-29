package net.domlom.websocket.util

import javax.websocket.server.{HandshakeRequest, ServerEndpoint, ServerEndpointConfig}
import javax.websocket.{HandshakeResponse, OnError, OnMessage, Session}
import org.glassfish.tyrus.server.Server

import scala.util.Try

@ServerEndpoint(value = "/echo")
class EchoEndpointAnnotated {

  @OnMessage
  def onMessage(message: String, session: Session): Unit = {
    session.getBasicRemote.sendText(message)
  }

  @OnError
  def onError(session: Session, error: Throwable): Unit = {
    throw error
  }
}

object TestUtil {

  def server: (Server, String) = {
    val built = ServerEndpointConfig.Builder
      .create(classOf[EchoEndpointAnnotated], "/echo")
      .configurator(new MyHeaderInspectorConfig)
      .build()
    val host = "localhost"
    val port = {
      val options = (8000 to 65535)
      options(scala.util.Random.nextInt(options.length - 1))
    }
    val contextPath = "/ws"
    val url = s"$host:$port$contextPath"
    val clientUrl = s"ws://$url"
    val echoEndpoint = s"$clientUrl/echo"

    println(s"Creating server @ $url")
    (new Server(host, port, contextPath, null, classOf[EchoEndpointAnnotated]), echoEndpoint)
  }

  def sequence[T](lst: List[Try[T]]): Try[List[T]] =
    lst.foldRight(Try(List.empty[T])) {
      case (cur, acc) =>
        for {
          t <- cur
          a <- acc
        } yield t :: a
    }

  class MyHeaderInspectorConfig extends ServerEndpointConfig.Configurator {

    import scala.collection.JavaConverters._

    override def modifyHandshake(
      sec: ServerEndpointConfig,
      request: HandshakeRequest,
      response: HandshakeResponse
    ): Unit =
      request.getHeaders.asScala.foreach(println)
  }

}
