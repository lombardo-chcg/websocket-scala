package net.domlom.websocket.lib

import java.util

import javax.websocket.ClientEndpointConfig.Configurator

import scala.collection.JavaConverters._

class CustomConfigurator(requestHeaders: Map[String, String]) extends Configurator {

  override def beforeRequest(headers: util.Map[String, util.List[String]]): Unit = {
    requestHeaders.foreach { case (k, v) => headers.put(k, List(v).asJava) }
  }

}
