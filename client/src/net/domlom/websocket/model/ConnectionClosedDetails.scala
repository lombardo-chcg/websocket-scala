package net.domlom.websocket.model

// for info on the meaning of a given closeCode see https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
case class ConnectionClosedDetails(closeCode: Int, reasonPhrase: String)
