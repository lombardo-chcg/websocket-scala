# websocket-scala

[![Actions Status](https://github.com/lombardo-chcg/websocket-scala/workflows/Scala%20CI/badge.svg)](https://github.com/lombardo-chcg/websocket-scala/workflows/Scala%20CI/badge.svg)
[![Latest version](https://index.scala-lang.org/lombardo-chcg/websocket-scala/websocket-scala/latest.svg)](https://index.scala-lang.org/lombardo-chcg/websocket-scala/websocket-scala)

`websocket-scala` is a simple Scala Websocket client library.  It is based on the `WebSocket` interface as defined in the [MDN web docs](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket), which is available as a JavaScript object in HTML5-compliant web browsers.  The goal of the library is to provide the same simple & intuitive Websocket client api for JVM Scala apps.

## installation

Supported scala versions:

- 2.11
- 2.12
- 2.13
- 3.x

##### sbt
```
libraryDependencies += "net.domlom" %% "websocket-scala" % "0.0.4"

```

##### gradle
```
scalaC = "2.12"

compile "net.domlom:websocket-scala_$scalaC:0.0.4"
```

##### ammonite
```
import $ivy.`net.domlom::websocket-scala:0.0.4`
```

## `Hello World`

an example using `echo.websocket.org`:

```scala
import net.domlom.websocket._

val msg = s"Hello World"

// Create a `WebsocketBehavior` instance and define event handlers.
// This example uses a builder pattern but it's just a case class underneath.
val behavior = {
  WebsocketBehavior.empty
    .setOnOpen { connection =>
      println("Connection Open")
      connection.send(msg)
    }
    .setOnMessage { (connection, message) =>
      println(s"Message from server: ${message.value}")

      if (message.value == msg) {
        println("Received echo - closing connection.")
        connection.close()
      }
    }
    .setOnClose { closeDetails =>
      println(closeDetails)
    }
    .setOnError { (connection, throwable) =>
      println(s"Exception - closing connection. ${throwable.getMessage}")
      connection.close()
    }
}

// initialize a client
val socket = Websocket("wss://echo.websocket.org", behavior)

// say hello
socket.connect()
```


# Usage

## Step 1 - Define a `WebsocketBehavior`

Define listener functions to handle the events of the ws connection:  `onOpen`,`onMessage`, `onClose` and `onError`

The listener definition is done via a case class config object called `WebsocketBehavior`:

```scala
case class WebsocketBehavior(
  onOpen: Websocket => Unit,
  onMessage: (Websocket, WsMessage) => Unit,
  onClose: ConnectionClosedDetails => Unit,
  onError: (Websocket, Throwable) => Unit
)
```

A `WebsocketBehavior` instance has dedicated extension methods for transforming it into a new `WebsocketBehavior`.
(This is done via the underlying `copy` semantics of a Scala case class).

```scala

// Step 1 - define a base template with common behaviors
val myBaseWsTemplate =
  WebsocketBehavior.empty
    .setOnOpen { connection =>
      connection.send("subscribe request")
    }
    .setOnClose { reason =>
      println(reason)
    }
    .setOnError { (connection, throwable) =>
      println(throwable.getMessage)
    }

// Step 2 - extend the base template with custom message handling
val mySpecificUseCase = myBaseWsTemplate
  .setOnMessage { (connection, message) =>
    val response = doBusinessValueStuff(message)
    connection.send(response)
  }
```

Note that we are only describing behavior via function definitions.
In order to use that behavior to process data, we must create a `Websocket` connection.

## Step 2 - Create a `Websocket` instance

Use a behavior to define a connection:

```scala

val myBehavior: WebsocketBehavior = ??? // see above

val socket = Websocket("wss://echo.websocket.org", myBehavior)

socket.connect()
```

Once the connection is open the callbacks from `WebsocketBehavior` will be used for the duration of the connection.

## Step 3 - Interact with the client.

Interactions with the `Websocket` client object can happen outside of the predefined `WebsocketBehavior` callback methods, as long as the connection stays open:

```scala
val socket = Websocket("wss://echo.websocket.org", myBehavior)

socket.connect()

socket.sendSync("example text")

socket.close()
```

## Error handling

Interactions with a `Websocket` instance will return `Try[WsResponse]`

- `WsResponse(message: String)` is like akka's `Done`, which signifies successful completion with no return value.  `WsResponse` signifies success, and also includes a context message that can be used for logging.

```
val sock = Websocket("wss://echo.websocket.org", behavior)

println(sock.connect().map(_.message))

// Success(Websocket Connected)
```

- Scala's native `Try` is a simple and effective way wrap a Java library, like we do here with [Project Tyrus](https://tyrus-project.github.io/)

- For more info on using `Try`, refer to [https://docs.scala-lang.org/overviews/scala-book/functional-error-handling.html](https://docs.scala-lang.org/overviews/scala-book/functional-error-handling.html) and [https://danielwestheide.com/blog/the-neophytes-guide-to-scala-part-6-error-handling-with-try/](https://danielwestheide.com/blog/the-neophytes-guide-to-scala-part-6-error-handling-with-try/)


## Request Headers

```scala
val myHeaders = Map("Authorization" -> "Bearer xxxxxxxxxxxxxx")

val socket = Websocket(
  url = "wss://echo.websocket.org",
  behavior = WebsocketBehavior.empty,
  requestHeaders = myHeaders
)

socket.connect()
```

## Message Handling

According the WebSocket Protocol (RFC 6455) a websocket message may be sent either complete, or in chunks.  At this time, `websocket-scala` only deals in complete Text messages.

This does not mean that partial Text messages are not processed however.  `websocket-scala` takes advantage of a feature in [Project Tyrus](https://tyrus-project.github.io/) which caches partial messages and delivers the full payload to the event listener.

## Async

Scala Futures are supported via `connection.sendAsync()` which returns a `Future[WsResponse]`. 

## Debug Mode

Set `debugMode = true` to get helpful messages sent to stdout during login attempts.  Use to troubleshoot connection issues.

```scala
val sock = Websocket(myEndpoint, behavior, debugMode = true)

sock.connect()

> Session xxxxx [47 ms]: Sending handshake request:
> Connection: Upgrade
...
```

## Building the project

- run example script
```
./mill examples[3.3.5].runMain "runnable.HelloWorld"     # versions: 2.11.12, 2.12.20, 2.13.16, 3.3.5
```

- run tests
```
./mill __.test
```

- build files for intellij
```
./mill mill.scalalib.GenIdea/idea
```

#### TODO

- handle partial text message
- support binary messages
- heartbeat / keepalive mechanism
- auto-reconnect with backoff
- cookbook

## License

GNU General Public License, version 2 with the GNU Classpath Exception, as required by Project Tyrus, its main dependency.
[Project Tyrus](https://tyrus-project.github.io/)
