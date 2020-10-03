package net.domlom

import net.domlom.websocket.lib.WebsocketFactory
import net.domlom.websocket.model.{ConnectionClosedDetails, Websocket}

package object websocket {

  object Websocket {

    /**
      * WebsocketClient
      *
      * @constructor
      * @param url ws:// or wss://
      * @param behavior a WebsocketBehavior instance
      * @param requestHeaders to be included on the initial connection request
      * @param debugMode print debug messages to stdout during login attempts
      *
      */
    def apply(
      url: String,
      behavior: WebsocketBehavior,
      requestHeaders: Map[String, String] = Map(),
      debugMode: Boolean = false
    ): Websocket =
      new WebsocketFactory(
        url = url,
        behavior = behavior,
        requestHeaders = requestHeaders,
        debugMode = debugMode
      ).api
  }

  /**
    *
    * @param onOpen  An event listener to be called when the connection is opened.
    * @param onMessage An event listener to be called when a message is received from the server.
    * @param onClose An event listener to be called when the connection is closed.
    * @param onError An event listener to be called when an error occurs.
    */
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

    /**
      *  A no-op implementation, suitable as a foundation for building custom behaviors
      */
    def empty: WebsocketBehavior =
      WebsocketBehavior(
        onOpen = connection => {},
        onMessage = (connection, message) => {},
        onClose = closeDetails => {},
        onError = (connection, throwable) => {}
      )

    /**
      * A `println` implementation
      */
    def debugBehavior: WebsocketBehavior =
      WebsocketBehavior(
        onOpen = connection => println("onOpen"),
        onMessage = (connection, message) => println(s"onMessage: $message"),
        onClose = closeDetails => println(s"onClose: $closeDetails"),
        onError = (connection, throwable) => println(s"onError: ${throwable.getMessage}")
      )
  }

  /**
    * this message wrapper will become a more robust type in future releases
    * and also includes a context message that can be used for logging.
    */
  case class WsMessage(value: String)

  /**
    * WsResponse signifies a successful Websocket client interaction,
    * and also includes a context message that can be used for logging.
    */
  case class WsResponse(message: String)

}
