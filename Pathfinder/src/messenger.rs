use std::{
    io::{Error, ErrorKind},
    time::Duration,
};

use bytes::{Buf, BufMut, BytesMut};
use tokio::{
    io::AsyncWriteExt,
    net::{
        tcp::{OwnedReadHalf, OwnedWriteHalf},
        TcpStream, ToSocketAddrs,
    },
    time::Interval,
};
use tokio_stream::StreamExt;
use tokio_util::codec::{Decoder, FramedRead};

// Assumes buf has enough space to store the string and length prefix
fn pack_str(string: &str, buf: &mut BytesMut) {
    buf.put_u16(string.len() as u16);
    buf.put(string.as_bytes());
}

#[derive(Debug)]
pub struct Message {
    pub name: String,
    pub data: BytesMut,
}

impl Message {
    fn pack(&mut self) -> BytesMut {
        let mut buf = BytesMut::with_capacity(6 + self.name.len() + self.data.len());

        pack_str(&self.name, &mut buf);
        buf.put_i32(self.data.len() as i32);
        buf.put(&mut self.data);

        buf
    }
}

pub struct MessengerClient {
    read: FramedRead<OwnedReadHalf, MessageDecoder>,
    write: OwnedWriteHalf,

    heartbeat_interval: Interval,
    packed_heartbeat: BytesMut,
}

impl MessengerClient {
    pub async fn connect<A>(addr: A, name: &str) -> Result<Self, Error>
    where
        A: ToSocketAddrs,
    {
        let stream = TcpStream::connect(addr).await?;
        let (read, write) = stream.into_split();

        let mut client = Self {
            read: FramedRead::new(read, MessageDecoder {}),
            write,

            heartbeat_interval: tokio::time::interval(Duration::from_secs(1)),
            packed_heartbeat: Message {
                name: "_Heartbeat".to_string(),
                data: BytesMut::new(),
            }
            .pack(),
        };

        let mut name_buf = BytesMut::with_capacity(2 + name.len());
        pack_str(name, &mut name_buf);
        client.write.write_all(&name_buf).await?;

        Ok(client)
    }

    pub async fn read_message(&mut self) -> Result<Message, Error> {
        loop {
            tokio::select! {
                _ = self.heartbeat_interval.tick() => {
                    self.write.write_all(&self.packed_heartbeat).await?;
                    // println!("Heartbeat!");
                }

                result = self.read.next() => match result {
                    Some(res) => {
                        if let Ok(msg) = &res {
                            if msg.name != "_Heartbeat" {
                                return res;
                            }
                        } else {
                            return res;
                        }
                    }

                    // None indicates end of input stream
                    None => return Err(Error::from(ErrorKind::BrokenPipe))
                }
            }
        }
    }

    pub async fn send_message(&mut self, mut msg: Message) -> Result<(), Error> {
        self.write.write_all(&msg.pack()).await
    }

    pub async fn listen(&mut self, name: &str) -> Result<(), Error> {
        let mut name_buf = BytesMut::with_capacity(2 + name.len());
        pack_str(name, &mut name_buf);

        self.send_message(Message {
            name: "_Listen".to_string(),
            data: name_buf,
        })
        .await
    }
}

pub struct MessageDecoder;

// const MAX_LEN: usize = 1024 * 1024; // Messages really shouldn't be this big
impl Decoder for MessageDecoder {
    type Item = Message;
    type Error = std::io::Error;

    fn decode(&mut self, src: &mut bytes::BytesMut) -> Result<Option<Self::Item>, Self::Error> {
        if src.len() < 2 {
            // Have not received type length prefix yet
            return Ok(None);
        }

        let mut name_len_bytes = [0u8; 2];
        name_len_bytes.copy_from_slice(&src[..2]);
        let name_len = u16::from_be_bytes(name_len_bytes) as usize;

        if src.len() < 2 + name_len + 4 {
            // Have not received name and data length yet
            src.reserve(2 + name_len + 4 - src.len());
            return Ok(None);
        }

        let mut data_len_bytes = [0u8; 4];
        data_len_bytes.copy_from_slice(&src[(2 + name_len)..(6 + name_len)]);
        let data_len = i32::from_be_bytes(data_len_bytes) as usize;

        if src.len() < 6 + name_len + data_len {
            // Have not received data yet
            src.reserve(6 + name_len + data_len - src.len());
            return Ok(None);
        }

        // If we reach here, we have the full message data

        let name_data = src[2..2 + name_len].to_vec();
        let data = BytesMut::from(&src[6 + name_len..6 + name_len + data_len]);
        src.advance(6 + name_len + data_len);

        let name = match String::from_utf8(name_data) {
            Ok(string) => Ok(string),
            Err(decode_err) => Err(std::io::Error::new(
                std::io::ErrorKind::InvalidData,
                decode_err,
            )),
        }?;

        Ok(Some(Message { name, data }))
    }
}
