package net.domlom.websocket

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class HeaderTest extends AnyFunSuite with Matchers {

  test("setting headers on init requests") {
    val headers = Map("Authorization" -> "Bearer ABC")
    val socket =
      Websocket("wss://echo.websocket.org", WebsocketBehavior.empty, requestHeaders = headers)
    // TODO: need to enhance the testutil server to allow asserting on headers
  }
}
