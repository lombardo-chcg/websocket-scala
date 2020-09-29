package net.domlom.websocket.model


import net.domlom.websocket.WsResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Websocket {
  def url: String

  def connect(): Try[WsResponse]

  def sendSync(message: String): Try[WsResponse]

  // shorthand alias
  def send(message: String): Try[WsResponse] = sendSync(message)

  def sendAsync(message: String)(
    implicit ec: ExecutionContext
  ): Future[WsResponse]

  def close(): Try[WsResponse]

  def isOpen: Boolean

  def debugMode: Boolean
}
