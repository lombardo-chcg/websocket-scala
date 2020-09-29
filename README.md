# websocket-scala

`websocket-scala` is a Scala Websocket client library.  It is based on the `WebSocket` interface as defined in the [MDN web docs](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket).  `websocket-scala` provides a simple API for connecting to a websocket endpoint and managing a duplex communication channel with the server. 


## `Hello World`

an example using `echo.websocket.org`:

```scala
import dom.lom.websocket._

// setup a behavior to println received messages
val behavior =
  WebsocketBehavior.empty
    .setOnMessage { (_, message) =>
      println(s"Rec'd message: ${message.value}")
    }

// initialize a client
val socket = Websocket("wss://echo.websocket.org", behavior)

// say hello
for {
  _ <- socket.connect()
  _ <- socket.send(s"Hello World")
  _ = Thread.sleep(500)
  _ <- socket.close()
} yield ()


// Rec'd message: Hello World

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
  headers = myHeaders
)

socket.connect()
```

## Message Handling 

According the WebSocket Protocol (RFC 6455) a websocket message may be sent either complete, or in chunks.  At this time, `websocket-scala` only deals in complete Text messages. 

This does not mean that partial Text messages are not processed however.  `websocket-scala` takes advantage of a feature in [Project Tyrus](https://tyrus-project.github.io/) which caches partial messages and delivers the full payload to the event listener.  

#### Future Work

- handle partial text message 
- support binary messages


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

- build files for intellij
```
./mill mill.scalalib.GenIdea/idea
```

- run tests
```
./mill examples.test
```

## License

GNU General Public License, version 2 with the GNU Classpath Exception, as required by Project Tyrus, its main dependency.
[Project Tyrus](https://tyrus-project.github.io/)
