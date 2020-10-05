package net.domlom.websocket.model

import javax.websocket.CloseReason

/* for info on the meaning of a given closeCode see https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent */
case class ConnectionClosedDetails(closeCode: Int, reasonPhrase: String)

object ConnectionClosedDetails {

  def apply(closeReason: CloseReason): ConnectionClosedDetails = {
    val closeCode = closeReason.getCloseCode.getCode
    if (wsSpecCodes.isDefinedAt(closeCode)) wsSpecCodes(closeCode)
    else
      ConnectionClosedDetails(
        closeCode,
        closeReason.getReasonPhrase
      )
  }

  val wsSpecCodes: PartialFunction[Int, ConnectionClosedDetails] = {
    case 1000 => ConnectionClosedDetails(1000, "Normal Closure")
    case 1001 => ConnectionClosedDetails(1001, "Going Away")
    case 1002 => ConnectionClosedDetails(1002, "Protocol Error")
    case 1003 => ConnectionClosedDetails(1003, "Unsupported Data")
    case 1004 => ConnectionClosedDetails(1004, "Reserved")
    case 1005 => ConnectionClosedDetails(1005, "No Status Received")
    case 1006 => ConnectionClosedDetails(1006, "Abnormal Closure")
    case 1007 => ConnectionClosedDetails(1007, "Invalid frame payload data")
    case 1008 => ConnectionClosedDetails(1008, "Policy Violation")
    case 1009 => ConnectionClosedDetails(1009, "Message too big")
    case 1010 => ConnectionClosedDetails(1010, "Missing Extension")
    case 1011 => ConnectionClosedDetails(1011, "Internal Error")
    case 1012 => ConnectionClosedDetails(1012, "Service Restart")
    case 1013 => ConnectionClosedDetails(1013, "Try Again Later")
    case 1014 => ConnectionClosedDetails(1014, "Bad Gateway")
    case 1015 => ConnectionClosedDetails(1015, "TLS Handshake")
  }
}
