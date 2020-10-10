package net.domlom.websocket.lib

import java.net.URI

import javax.websocket.MessageHandler.Whole
import javax.websocket._
import net.domlom.websocket.{WebsocketBehavior, WsMessage, WsResponse}
import net.domlom.websocket.model.{ConnectionClosedDetails, Websocket}
import org.glassfish.tyrus.client.ClientManager

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Try}

class WebsocketBase(
    val url: String,
    behavior: WebsocketBehavior,
    requestHeaders: Map[String, String] = Map(),
    disableHostVerification: Boolean = false,
    debugMode: Boolean = false
) {
  self =>

  val api = new Websocket {

    override def url: String =
      self.url

    override def connect(): Try[WsResponse] =
      self.connect()

    override def sendSync(message: String): Try[WsResponse] =
      self.sendSync(message)

    override def sendAsync(message: String)(implicit ec: ExecutionContext): Future[WsResponse] =
      self.sendAsync(message)

    override def close(): Try[WsResponse] =
      self.close()

    override def isOpen: Boolean =
      self.isOpen
  }

  private var sessionOpt: Option[Session] = None

  private lazy val uri = new URI(url)

  private val cec: ClientEndpointConfig = Interop.clientEndpointConfig(requestHeaders)

  private val client: ClientManager = Interop.client(debugMode, disableHostVerification)

  private def connect(): Try[WsResponse] =
    wrapUnsafe(s"Websocket Connection Opened - $url", () => client.connectToServer(endpoint, cec, uri))

  private def sendSync(message: String): Try[WsResponse] =
    sessionOpt match {
      case Some(session) =>
        wrapUnsafe("sendSync success", () => session.getBasicRemote.sendText(message))
      case None =>
        Failure(new RuntimeException("sendSync called but no active connection found"))
    }

  private def sendAsync(message: String)(implicit ec: ExecutionContext): Future[WsResponse] =
    sessionOpt match {
      case Some(session) =>
        wrapJavaFuture("sendAsync success", session.getAsyncRemote.sendText(message))
      case None =>
        Future.failed(new RuntimeException("sendAsync called but no active connection found"))
    }

  private def isOpen: Boolean =
    sessionOpt.fold(false)(_.isOpen)

  private def close(): Try[WsResponse] =
    sessionOpt match {
      case Some(session) =>
        wrapUnsafe("Websocket connection closed", () => session.close())
      case None =>
        Failure(new RuntimeException("close called but no active connection found"))
    }

  private def wrapUnsafe[A](tag: String, f: () => A): Try[WsResponse] =
    Try(f()).map(_ => WsResponse(tag))

  private def wrapJavaFuture[T](tag: String, f: java.util.concurrent.Future[T])(
      implicit
      ec: ExecutionContext
  ): Future[WsResponse] = {
    val p = Promise[T]()
    Future {
      p.complete(Try(f.get))
    }
    p.future.map(_ => WsResponse("sendAsync success"))
  }

  private val endpoint: Endpoint = new Endpoint() {

    val messageHandler: Whole[String] = new Whole[String] {

      def onMessage(msg: String) =
        behavior.onMessage(api, WsMessage(msg))
    }

    override def onOpen(session: Session, config: EndpointConfig): Unit = {
      sessionOpt = Some(session)
      behavior.onOpen(api)
      session.addMessageHandler(messageHandler)
    }

    override def onClose(session: Session, closeReason: CloseReason): Unit = {
      sessionOpt = None
      behavior.onClose(ConnectionClosedDetails(closeReason))
    }

    override def onError(session: Session, error: Throwable): Unit =
      wrapUnsafe("onError", () => behavior.onError(api, error))
  }

  // TODO:
  //    val streamHandler: Whole[java.io.Reader] = new Whole[java.io.Reader] {
  //
  //      def onMessage(msg: java.io.Reader) = {
  //
  //        def read(buf: StringBuffer, msg: java.io.Reader): String = {
  //          val data = msg.read()
  //          if (data == -1) buf.toString
  //          else {
  //            buf.append(Character.toString(data.toChar))
  //            read(buf, msg)
  //          }
  //        }
  //
  //        val out = read(new StringBuffer, msg)
  //        println(read(new StringBuffer, msg))
  //
  //        eventHandler.onMessage(self, out)
  //      }
  //    }
}
