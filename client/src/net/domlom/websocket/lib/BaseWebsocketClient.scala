package net.domlom.websocket.lib

import java.net.URI

import javax.websocket.MessageHandler.Whole
import javax.websocket._
import net.domlom.websocket.{WebsocketBehavior, WsMessage, WsResponse}
import net.domlom.websocket.model.{ConnectionClosedDetails, Websocket}
import org.glassfish.tyrus.client.{ClientManager, ClientProperties, SslContextConfigurator, SslEngineConfigurator}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Try}

class BaseWebsocketClient(
  val url: String,
  behavior: WebsocketBehavior,
  requestHeaders: Map[String, String] = Map(),
  val disableHostVerification: Boolean = false,
  val debugMode: Boolean = false
) extends Websocket {
  self =>

  private lazy val uri = new URI(url)

  private val cec: ClientEndpointConfig =
    ClientEndpointConfig.Builder
      .create()
      .configurator(new CustomConfigurator(requestHeaders))
      .build

  private val client: ClientManager = {
    val c = ClientManager.createClient

    if (debugMode) {
      c.getProperties.put(ClientProperties.LOG_HTTP_UPGRADE, "true")
    }

    if (disableHostVerification) {
      val sslEngineConfigurator = new SslEngineConfigurator(new SslContextConfigurator)
      sslEngineConfigurator.setHostVerificationEnabled(false)
      client.getProperties.put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator)
    }

    c
  }

  private val endpoint: Endpoint = new Endpoint() {

    val messageHandler: Whole[String] = new Whole[String] {

      def onMessage(msg: String) =
        behavior.onMessage(self, WsMessage(msg))
    }

    override def onOpen(session: Session, config: EndpointConfig): Unit = {
      sessionOpt = Some(session)
      behavior.onOpen(self)
      session.addMessageHandler(messageHandler)
    }

    override def onClose(session: Session, closeReason: CloseReason): Unit = {
      sessionOpt = None
      val details = ConnectionClosedDetails(
        closeReason.getCloseCode.getCode,
        closeReason.getReasonPhrase
      )
      behavior.onClose(details)
    }

    override def onError(session: Session, error: Throwable): Unit =
      wrapUnsafe("onError", () => behavior.onError(self, error))
  }
  private var sessionOpt: Option[Session] = None

  override def connect(): Try[WsResponse] =
    wrapUnsafe("Websocket Connected", () => client.connectToServer(endpoint, cec, uri))

  def sendSync(message: String): Try[WsResponse] = sessionOpt match {
    case Some(session) =>
      wrapUnsafe("sendSync success", () => session.getBasicRemote.sendText(message))
    case None =>
      Failure(new RuntimeException("sendSync called but no active connection found"))
  }

  override def sendAsync(message: String)(implicit ec: ExecutionContext): Future[WsResponse] =
    sessionOpt match {
      case Some(session) =>
        wrapJavaFuture("sendAsync success", session.getAsyncRemote.sendText(message))
      case None =>
        Future.failed(new RuntimeException("sendAsync called but no active connection found"))
    }

  override def isOpen: Boolean =
    sessionOpt.fold(false)(_.isOpen)

  override def close(): Try[WsResponse] = sessionOpt match {
    case Some(session) =>
      wrapUnsafe("Websocket connection closed", () => session.close())
    case None =>
      Failure(new RuntimeException("close called but no active connection found"))
  }

  private def wrapUnsafe[A](tag: String, f: () => A): Try[WsResponse] =
    Try(f()).map(_ => WsResponse(tag))

  private def wrapJavaFuture[T](tag: String, f: java.util.concurrent.Future[T])(
    implicit ec: ExecutionContext
  ): Future[WsResponse] = {
    val p = Promise[T]()
    Future {
      p.complete(Try(f.get))
    }
    p.future.map(_ => WsResponse("sendAsync success"))
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
