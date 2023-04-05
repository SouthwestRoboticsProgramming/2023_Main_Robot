from messenger import *
from time import sleep

host = 'localhost'
port = 5805


def main():
    client = MessengerClient(host, port, 'test')  # Start up a connections
    while not client.is_connected():  # Wait until it connects
        time.sleep(0.1)

    client.add_handler('Test', handler)

    # Add 5 numbers to the messenger
    for i in range(5):
        client.prepare('Test')\
            .add_int(i)\
            .send()
        sleep_with_read()

    client.disconnect()
    print("Done")

# Just print out what is going on
def handler(type, reader):
    print(type, reader.read_int())

def sleep_with_read(client):
    for _ in range(10):
        client.read_messages()
        time.sleep(0.1)

if __name__ == '__main__':
    main()
