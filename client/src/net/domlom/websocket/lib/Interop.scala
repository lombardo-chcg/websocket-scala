package net.domlom.websocket.lib

import javax.websocket.ClientEndpointConfig
import org.glassfish.tyrus.client.{
  ClientManager,
  ClientProperties,
  SslContextConfigurator,
  SslEngineConfigurator
}

object Interop {

  def clientEndpointConfig(requestHeaders: Map[String, String] = Map()): ClientEndpointConfig =
    ClientEndpointConfig.Builder
      .create()
      .configurator(new CustomConfigurator(requestHeaders))
      .build

  def client(debugMode: Boolean, disableHostVerification: Boolean): ClientManager = {
    val client = ClientManager.createClient

    if (debugMode) {
      client.getProperties.put(ClientProperties.LOG_HTTP_UPGRADE, "true")
    }

    if (disableHostVerification) {
      val sslEngineConfigurator = new SslEngineConfigurator(new SslContextConfigurator)
      sslEngineConfigurator.setHostVerificationEnabled(false)
      client.getProperties.put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator)
    }

    client
  }
}
