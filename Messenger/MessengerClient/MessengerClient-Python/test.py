from messenger import *
import time

host = 'localhost'
port = 5805

client = MessengerClient(host, port, 'test')
while not client.is_connected():
    time.sleep(0.1)

def handler(type, reader):
    print(type, reader.read_int())

def sleepWithRead():
    global client
    for i in range(10):
        client.read_messages()
        time.sleep(0.1)

client.add_handler('Test', handler)

for i in range(5):
    client.prepare('Test')\
        .add_int(i)\
        .send()
    sleepWithRead()

client.disconnect()
print("Done")
