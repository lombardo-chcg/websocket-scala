package net.domlom

import net.domlom.websocket.lib.BaseWebsocketClient
import net.domlom.websocket.model.{ConnectionClosedDetails, Websocket}

package object websocket {

  object Websocket {

    /**
      * WebsocketClient
      *
      * @constructor
      * @param url
      * @param behavior
      * @param requestHeaders
      * @param debugMode
      *
      */
    def apply(
      url: String,
      behavior: WebsocketBehavior,
      requestHeaders: Map[String, String] = Map(),
      debugMode: Boolean = false
    ) =
      new BaseWebsocketClient(
        url = url,
        behavior = behavior,
        requestHeaders = requestHeaders,
        debugMode = debugMode
      )
  }

  case class WebsocketBehavior(
    onOpen: Websocket => Unit,
    onMessage: (Websocket, WsMessage) => Unit,
    onClose: ConnectionClosedDetails => Unit,
    onError: (Websocket, Throwable) => Unit
  ) {
    self =>

    def setOnOpen(f: Websocket => Unit): WebsocketBehavior =
      self.copy(onOpen = f)

    def setOnMessage(f: (Websocket, WsMessage) => Unit): WebsocketBehavior =
      self.copy(onMessage = f)

    def setOnClose(f: ConnectionClosedDetails => Unit): WebsocketBehavior =
      self.copy(onClose = f)

    def setOnError(f: (Websocket, Throwable) => Unit): WebsocketBehavior =
      self.copy(onError = f)
  }

  object WebsocketBehavior {

    def empty: WebsocketBehavior =
      WebsocketBehavior(
        onOpen = connection => {},
        onMessage = (connection, message) => {},
        onClose = closeDetails => {},
        onError = (connection, throwable) => {}
      )

    def printlnBehavior: WebsocketBehavior =
      WebsocketBehavior(
        onOpen = connection => println("onOpen"),
        onMessage = (connection, message) => println(s"onMessage: $message"),
        onClose = closeDetails => println(s"onClose: $closeDetails"),
        onError = (connection, throwable) => println(s"onError: ${throwable.getMessage}")
      )
  }

  case class WsMessage(value: String)

  case class WsResponse(message: String)

}
