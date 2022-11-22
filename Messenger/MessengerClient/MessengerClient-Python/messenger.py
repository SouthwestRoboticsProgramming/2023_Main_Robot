"""
Python port of the MessengerClient API.

Use MessengerClient to connect to a server. In order to properly
receive messages, you must call the read_messages() method on it
while your program is running. The disconnect() method should be
called when the program ends to shut down background threads. If
you do not call disconnect(), the program will appear to hang.
"""

import socket
import time
import threading
import struct
import select


class MessageBuilder:
    """
    Allows easy storage of data into a message.
    """

    def __init__(self, client, message_type):
        self.client = client
        self.message_type = message_type
        self.buffer = b''

    def send(self):
        """
        Sends the message with the message_type and data.
        """
        self.client._send_message(self.message_type, self.buffer)

    def add_boolean(self, boolean):
        """
        Adds a boolean to this message

        :param boolean: boolean to add
        :return: self
        """

        self.buffer += struct.pack('>?', boolean)
        return self

    def add_string(self, string):
        """
        Adds a string to this message

        :param string: string to add
        :return: self
        """

        self.buffer += _encode_string(string)
        return self

    def add_char(self, character):
        """
        Adds a character to this message

        :param character: character to add
        :return: self
        """

        self.buffer += struct.pack('>c', character)
        return self

    def add_byte(self, byte):
        """
        Adds a byte to this message

        :param byte: byte to add
        :return: self
        """

        self.buffer += struct.pack('>b', byte)
        return self

    def add_short(self, short):
        """
        Adds a short to this message

        :param short: short to add
        :return: self
        """

        self.buffer += struct.pack('>s', short)
        return self

    def add_int(self, integer):
        """
        Adds an int to this message

        :param integer: int to add
        :return: self
        """

        self.buffer += struct.pack('>i', integer)
        return self

    def add_long(self, long_int):
        """
        Adds a long to this message

        :param long_int: long to add
        :return: self
        """

        self.buffer += struct.pack('>q', long_int)
        return self

    def add_float(self, f):
        """
        Adds a float to this message

        :param f: float to add
        :return: self
        """

        self.buffer += struct.pack('>f', f)
        return self

    def add_double(self, double):
        """
        Adds a double to this message

        :param double: double to add
        :return: self
        """

        self.buffer += struct.pack('>d', double)
        return self

    def add_raw(self, data):
        """
        Adds raw data to this message

        :param data: data to add
        :return: self
        """

        self.buffer += data
        return self


class MessageReader:
    """
    Allows easy access to data stored within a message.
    """

    def __init__(self, data):
        """
        Creates a new MessageReader that reads from a raw byte array.

        :param data: raw data
        """

        self.data = data
        self.cursor = 0

    def _next(self, count):
        # Reads the next count bytes from the data array as a bytes object

        read = self.data[self.cursor:(self.cursor + count)]
        self.cursor += count
        return read

    def read_boolean(self):
        """
        Reads a boolean from the message.

        :return: boolean read
        """

        return struct.unpack('?', self._next(1))[0]

    def read_string(self):
        """
        Reads a string from the message.

        :return: string read
        """

        utf_len = struct.unpack('>H', self._next(2))[0]
        return self._next(utf_len).decode('utf-8')

    def read_char(self):
        """
        Reads a character from the message.

        :return: character read
        """

        return chr(struct.unpack('>H', self._next(2)))[0]

    def read_byte(self):
        """
        Reads a byte from the message.

        :return: byte read
        """

        return struct.unpack('b', self._next(1))[0]

    def read_short(self):
        """
        Reads a short from the message.

        :return: short read
        """

        return struct.unpack('>h', self._next(2))[0]

    def read_int(self):
        """
        Reads an int from the message.

        :return: int read
        """

        return struct.unpack('>i', self._next(4))[0]

    def read_long(self):
        """
        Reads a long from the message.

        :return: long read
        """

        return struct.unpack('>q', self._next(8))[0]

    def read_float(self):
        """
        Reads a float from the message.

        :return: float read
        """

        return struct.unpack('>f', self._next(4))[0]

    def read_double(self):
        """
        Reads a double from the message.

        :return: double read
        """

        return struct.unpack('>d', self._next(8))[0]

    def read_raw(self, length):
        """
        Reads raw data as a byte array from the message.

        :param length: number of bytes to read
        :return: bytes read
        """
        return self._next(length)

    def read_all_data(self):
        """
        Reads all remaining data as a bytes object

        :return: data read
        """
        return self.data[self.cursor:]


# Built-in messages
_HEARTBEAT = '_Heartbeat'
_LISTEN = '_Listen'
_DISCONNECT = '_Disconnect'


def _heartbeat_thread(msg):
    # Sends a heartbeat message once per second to keep the
    # connection alive

    while not msg.heartbeat_event.is_set():
        msg._send_message(_HEARTBEAT, b'')
        time.sleep(1)


def _connect_thread(msg):
    # Repeatedly tries to connect to the server until successful

    while not msg.connected and not msg.connect_event.is_set():
        try:
            msg.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            msg.socket.connect((msg.host, msg.port))

            msg._send(_encode_string(msg.name))
            msg.connected = True

            for listen in msg.listening:
                msg._listen(listen)
        except Exception:
            if msg.log_errors:
                print('Messenger connection failed')

        time.sleep(1)
    msg.connect_thread = None


def _encode_string(string):
    # Encodes a string into a length-prefixed UTF-8 bytes object

    encoded_len = struct.pack(">h", len(string))
    return encoded_len + string.encode("utf-8")


class WildcardHandler:
    # Handles wildcard patterns (i.e. patterns that end in '*')

    def __init__(self, pattern, handler):
        self.pattern = pattern
        self.handler = handler

    def handle(self, message_type, data):
        if message_type.startswith(self.pattern):
            self.handler(message_type, MessageReader(data))


class DirectHandler:
    # Handles simple patterns (message_type must match exactly)

    def __init__(self, message_type, handler):
        self.message_type = message_type
        self.handler = handler

    def handle(self, message_type, data):
        if self.message_type == message_type:
            self.handler(message_type, MessageReader(data))


class MessengerClient:
    """
    Represents a connection to the Messenger server.
    This can be used to send messages between processes.
    """

    def __init__(self, host, port, name, mute_errors=False):
        """
        Creates a new instance and attempts to connect to a
        Messenger server at the given address.

        :param host: server host
        :param port: server port
        :param name: unique string used in logging
        """

        self.host = host
        self.port = port
        self.name = name
        self.log_errors = not mute_errors

        self.socket = None
        self.connected = False

        self.heartbeat_event = threading.Event()
        self.heartbeat_thread = threading.Thread(target=_heartbeat_thread, args=(self,))
        self.heartbeat_thread.start()
        self.listening = []
        self.handlers = []

        self._start_connect_thread()

    def reconnect(self, host, port, name):
        """
        Attempts to reconnect to a different Messenger server at
        a given address.

        :param host: server host
        :param port: server port
        :param name: unique string used in logging
        :return:
        """

        self.host = host
        self.port = port
        self.name = name

        self.send(_DISCONNECT)
        self._disconnect_socket()
        self.connected = False

        self._start_connect_thread()

    def read_messages(self):
        """
        Reads all incoming messages. If not connected, this will do
        nothing. Message handlers will be invoked from this method.
        """

        if not self.connected:
            return

        try:
            while self._available():
                self._read_message()
        except Exception:
            self._handle_error()

    def is_connected(self):
        """
        Gets whether this client is currently connected to a server.

        :return: connected
        """
        return self.connected

    def disconnect(self):
        """
        Disconnects from the current server. After this method is called,
        this object should no longer be used. If you want to change servers,
        use reconnect(...).
        """

        self.send(_DISCONNECT)

        self.heartbeat_event.set()
        self.heartbeat_thread.join()

        self._disconnect_socket()
        self.connected = False

    def prepare(self, message_type):
        """
        Prepares to send a message. This returns a MessageBuilder,
        which allows you to add data to the message.

        :param message_type: message_type of the message to send
        :return: builder to add data
        """

        return MessageBuilder(self, message_type)

    def send(self, message_type):
        """
        Immediately sends a message with no data.

        :param message_type: message_type of the message to send
        """
        self._send_message(message_type, b'')

    def add_handler(self, message_type, handler):
        """
        Registers a message handler to handle incoming messages.
        If the message_type ends in '*', the handler will be invoked for all messages
        that match the content before. For example, "Foo*" would match a
        message of message_type "Foo2", while "Foo" would only match messages of
        message_type "Foo".

        The handler parameter should be a function with two parameters:
        the message_type of message as a string, and a MessageReader to read data
        from the message.

        :param message_type: message_type of message to listen to
        :param handler: handler to invoke
        """

        if message_type.endswith('*'):
            handler = WildcardHandler(message_type[:-1], handler)
        else:
            handler = DirectHandler(message_type, handler)
        self.handlers.append(handler)

        if message_type not in self.listening:
            self.listening.append(message_type)
            if self.connected:
                self._listen(message_type)

    def _available(self):
        readable = select.select([self.socket], [], [], 0)[0]

        return any(sock == self.socket for sock in readable)

    def _listen(self, message_type):
        self.prepare(_LISTEN).add_string(message_type).send()

    def _handle_error(self):
        self._disconnect_socket()

        if self.log_errors:
            print("Messenger connection lost")

        self.connected = False

        self._start_connect_thread()

    def _read(self, count):
        data = b""
        while len(data) < count:
            data += self.socket.recv(count - len(data))
        return data

    def _send(self, data):
        total = 0
        while total < len(data):
            send = self.socket.send(data[total:])
            if send == 0:
                self._handle_error()
                return
            total += send

    def _send_message(self, message_type, data):
        if not self.connected:
            return

        encoded_message_type = _encode_string(message_type)
        encoded_data_len = struct.pack(">i", len(data))
        encoded = encoded_message_type + encoded_data_len + data

        # packet_len = struct.pack(">i", len(encoded))
        # packet = packet_len + encoded
        packet = encoded

        self._send(packet)

    def _start_connect_thread(self):
        self.connect_event = threading.Event()
        self.connect_thread = threading.Thread(target=_connect_thread, args=(self,))
        self.connect_thread.start()

    def _read_message(self):
        message_type_len = struct.unpack('>h', self._read(2))[0]
        message_message_type = self._read(message_type_len).decode('utf-8')
        data_len = struct.unpack('>i', self._read(4))[0]
        message_data = self._read(data_len)

        for handler in self.handlers:
            handler.handle(message_message_type, message_data)

    def _disconnect_socket(self):
        if self.connect_thread is not None:
            self.connect_event.set()
            self.connect_thread.join()
        self.connect_thread = None

        self.socket.close()
