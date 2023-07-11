# Messenger Specification

## Data types

All data in this protocol is big-endian (most significant byte sent first). The supported data formats within messages are:

| Type      | Size (bytes) | Format |
| --------- | ------------ | ------ |
| `boolean` | 1            | 1 if true, 0 if false |
| `String`  | len + 2      | First 2 bytes are unsigned 16-bit integer indicating `length`, remaining `length` bytes are UTF-8 |
| `char`    | 2            | One UTF-16 codepoint |
| `byte`    | 1            | Signed 8-bit integer |
| `short`   | 2            | Signed 16-bit integer |
| `int`     | 4            | Signed 32-bit integer |
| `long`    | 8            | Signed 64-bit integer |
| `float`   | 4            | [Single-precision 32-bit IEEE 754 floating point number](http://en.wikipedia.org/wiki/Single-precision_floating-point_format) |
| `double`  | 8            | [Double-precision 64-bit IEEE 754 floating point number](http://en.wikipedia.org/wiki/Double-precision_floating-point_format) |
| `raw`     | any          | undefined number of bytes, length must be inferred by context |

## Protocol

### Communication

All communication between client and server is over a TCP socket typically on port 5805. The client initiates a connection to the server, and the connection is kept alive until the client disconnects.

### Handshaking

Upon connecting to the server, the client sends a single `String` containing its name. This name is used for debugging purposes, and has no other effect on the protocol. The client and server then immediately switch to the Transmission state.

The server does not send anything during handshake.

### Transmission

The format of a message is as follows:

| Name      | Type     | Description                             |
| --------- | -------- | --------------------------------------- |
| Type      | `String` | The type ID of the message              |
| Data size | `int`    | Size in bytes of the following raw data |
| Data      | `raw`    | Raw data transmitted with the message   |

Both the client and server may send a message over the socket at any time while in the Transmission state.

While the message data can technically contain any data, it is recommended to use only the data types declared in the section above to help ensure the data can be correctly interpreted by multiple languages.

#### Internal message type IDs

The message type IDs `_Heartbeat`, `_Listen`, `_Unlisten`, and `_Disconnect` are reserved for internal messages. These message IDs may not be used in any case except where specified here.

#### Listening to messages

The server only sends messages to a client if the client listens to the corresponding type ID.

A type ID is typically of the format `Namespace:Type`, where `Namespace` is a common prefix for all types of messages relating to one module, and `Type` is the type of the message within that namespace. This is to prevent multiple processes from accidentally using the same message type.

The client indicates that it wants to listen to a message type by sending a message with a type ID of `_Listen`, with the message data containing a single `String`, the target ID. If the target ID ends with the `*` character, it is a wildcard. This causes the client to listen to all messages which type IDs start with the preceding characters. Otherwise, the client listens only to messages where the type ID matches exactly.

If a client no longer wants to listen to a message type, it can send a message of type `_Unlisten` containing a `String` with the type to stop listening to.

#### Heartbeat

Both the client and server periodically send a heartbeat message to ensure the connection has not been lost.

The client is responsible for initiating the heartbeat sequence. It must send a heartbeat at least once every 5 seconds. If longer than 5 seconds passes between heartbeats, the server will disconnect the client. It is recommended that the client send a heartbeat once every second to avoid false timeouts.

A heartbeat message is a message with a type ID of `_Heartbeat`, and empty (0 bytes) data.

When the server receives a heartbeat from the client, it will respond with its own matching heartbeat message. If the server does not respond to the heartbeat within 4 seconds, the client should disconnect from the server.

#### Disconnecting

If the client wishes to disconnect from the server, it can do so by sending a message with type ID `_Disconnect` and empty data. After sending this message, the client should close the socket, and the server will close the socket upon receiving it.

## Built-in messages

The Messenger protocol includes a few built-in messages for debugging the server. These messages are exactly the same as messages sent by a client, and as such, a client must listen to the message types in order to receive them.

The formats of the data contained in these messages are as follows:

### `Messenger:Event`
Sent by server.

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| Type       | `String` | The type of event. This can be `Connect`, `Listen`, `Unlisten`, `Disconnect`, `Timeout`, or `Error` |
| Name       | `String` | Name of the client which generated the event |
| Descriptor | `String` | Target ID for `Listen` and `Unlisten`, empty for all others |

An event message is sent whenever a client generates an event. These events can have the following types:

| Type         | Condition                                              |
| ------------ | ------------------------------------------------------ |
| `Connect`    | A new client completes its handshake                   |
| `Listen`     | A client listens to a type ID                          |
| `Unlisten`   | A client stops listening to a type ID                  |
| `Disconnect` | A client disconnects with the `_Disconnect` message    |
| `Timeout`    | A client disconnects due to heartbeat timeout          |
| `Error`      | An exception occurs while handling a client connection |

Events are also logged to the server console, and to a log file if the server is configured to log messages.

### `Messenger:GetClients`
Sent by client. Contains no data.

This message queries the server for a list of connected client names. The server will respond with a `Messenger:Clients` message.

### `Messenger:Clients`
Sent by server.

| Name         | Type              | Description                                   |
| ------------ | ----------------- | --------------------------------------------- |
| Count        | `int`             | Number of client names in the following array |
| Client names | array of `String` | Names of all connected clients                |

This message is sent by the server as a response to the `Messenger:GetClients` message.
