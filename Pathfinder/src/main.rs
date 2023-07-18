use arm::ArmPose;
use bytes::{Buf, BufMut, BytesMut};
use lerp::Lerp;
use std::{error::Error, f64::consts::PI, thread, time::Duration};
use tokio::{sync::mpsc, time::Instant};
use vectors::Vec2i;

use crate::messenger::Message;

pub mod arm;
pub mod collision;
pub mod dijkstra;
pub mod grid;
pub mod messenger;
pub mod theta_star;
pub mod vectors;

pub const STATE_SZ: Vec2i = Vec2i { x: 128, y: 256 };
pub const BOTTOM_RANGE: (f64, f64) = (0.4363, 2.1817);
pub const TOP_RANGE: (f64, f64) = (-1.5 * PI, 0.5 * PI);

fn state_to_pose(state_x: f32, state_y: f32) -> ArmPose {
    ArmPose {
        bottom_angle: BOTTOM_RANGE
            .0
            .lerp(BOTTOM_RANGE.1, state_x as f64 / STATE_SZ.x as f64),
        top_angle: TOP_RANGE
            .0
            .lerp(TOP_RANGE.1, state_y as f64 / STATE_SZ.y as f64),
    }
}

fn wrap(val: f64, min: f64, max: f64) -> f64 {
    (val - min).rem_euclid(max - min) + min
}

fn percent(val: f64, min: f64, max: f64) -> f64 {
    (val - min) / (max - min)
}

fn pose_to_state(pose: &ArmPose) -> Vec2i {
    Vec2i {
        x: (percent(
            wrap(pose.bottom_angle, BOTTOM_RANGE.0, BOTTOM_RANGE.1),
            BOTTOM_RANGE.0,
            BOTTOM_RANGE.1,
        ) * (STATE_SZ.x as f64)) as i32,
        y: (percent(
            wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1),
            TOP_RANGE.0,
            TOP_RANGE.1,
        ) * (STATE_SZ.y as f64)) as i32,
    }
}

// TODO: Move into messenger module somehow
async fn messages_io(
    recv_tx: &mut mpsc::Sender<Message>,
    send_rx: &mut mpsc::Receiver<Message>,
) -> Result<(), Box<dyn Error>> {
    let mut msg = messenger::MessengerClient::connect("localhost:5805", "Pathfinding").await?;
    msg.listen("Pathfinding:Calc").await?;
    msg.listen("Pathfinding:GetInfo").await?;
    println!("Connected");

    loop {
        tokio::select! {
            m_result = msg.read_message() => match m_result {
                Ok(m) => {
                    recv_tx.send(m).await?;
                },
                Err(e) => return Err(Box::new(e))
            },

            to_send_opt = send_rx.recv() => match to_send_opt {
                Some(to_send) => {
                    msg.send_message(to_send).await?;
                }
                None => {}
            }
        }
    }
}

async fn messages_io_task(
    mut recv_tx: mpsc::Sender<Message>,
    mut send_rx: mpsc::Receiver<Message>,
) {
    loop {
        if let Err(e) = messages_io(&mut recv_tx, &mut send_rx).await {
            eprintln!("Connection error: {}", e);
            thread::sleep(Duration::from_secs(1));
        }
    }
}

#[tokio::main]
pub async fn main() {
    // We use X axis for bottom joint and Y axis for top joint

    let bias_x = (BOTTOM_RANGE.1 - BOTTOM_RANGE.0) * arm::BOTTOM_GEAR_RATIO;
    let bias_y = (TOP_RANGE.1 - TOP_RANGE.0) * arm::TOP_GEAR_RATIO;

    let mut grid = grid::GridBuilder::new(STATE_SZ)
        .with_bias(bias_x, bias_y)
        .build();

    for state_y in 0..STATE_SZ.y {
        for state_x in 0..STATE_SZ.x {
            let state = Vec2i {
                x: state_x,
                y: state_y,
            };

            let pose = state_to_pose(state.x as f32, state.y as f32);
            grid.set_cell_passable(&state, pose.is_valid());
        }
    }

    let mut start_pose = ArmPose {
        bottom_angle: 0.0,
        top_angle: 0.0,
    };
    let mut goal_pose = ArmPose {
        bottom_angle: 0.0,
        top_angle: 0.0,
    };

    let (send_tx, send_rx) = mpsc::channel(32);
    let (recv_tx, mut recv_rx) = mpsc::channel(32);
    tokio::spawn(messages_io_task(recv_tx, send_rx));

    loop {
        let mut need_new_path = false;
        loop {
            match recv_rx.try_recv() {
                Ok(msg) => match msg.name.as_str() {
                    "Pathfinding:Calc" => {
                        let mut d = msg.data;
                        let start_bot = d.get_f64();
                        let start_top = d.get_f64();
                        let goal_bot = d.get_f64();
                        let goal_top = d.get_f64();

                        start_pose = ArmPose {
                            bottom_angle: start_bot,
                            top_angle: start_top,
                        };
                        goal_pose = ArmPose {
                            bottom_angle: goal_bot,
                            top_angle: goal_top,
                        };
                        need_new_path = true;
                    }
                    "Pathfinding:GetInfo" => {
                        println!("Get info requested");
                        let mut out = BytesMut::new();
                        out.put_i32(STATE_SZ.x);
                        out.put_i32(STATE_SZ.y);
                        out.put_f64(BOTTOM_RANGE.0);
                        out.put_f64(BOTTOM_RANGE.1);
                        out.put_f64(TOP_RANGE.0);
                        out.put_f64(TOP_RANGE.1);
                        out.put_f64(arm::FRAME_SIZE);

                        out.put_i32(arm::COLLISION_RECTS.len() as i32);
                        for rect in arm::COLLISION_RECTS {
                            out.put_f64(rect.position.x);
                            out.put_f64(rect.position.y);
                            out.put_f64(rect.size.x);
                            out.put_f64(rect.size.y);
                            out.put_f64(rect.rotation);
                            out.put_u8(if rect.inverted { 1 } else { 0 });
                        }

                        for y in 0..STATE_SZ.y {
                            for x in 0..STATE_SZ.x {
                                out.put_u8(if grid.can_pass(x, y) { 1 } else { 0 });
                            }
                        }

                        send_tx
                            .send(Message {
                                name: "Pathfinding:Info".to_string(),
                                data: out,
                            })
                            .await
                            .unwrap();
                        println!("Sent info");
                    }
                    _ => {}
                },
                Err(_) => break,
            }
        }
        if !need_new_path {
            continue;
        }

        let start = pose_to_state(&start_pose);
        let goal = pose_to_state(&goal_pose);
        // println!("Pathing from {:?} to {:?}", start, goal);

        let data = if let Some(start_pos) = dijkstra::find_nearest_passable(&grid, start) {
            let start_time = Instant::now();
            let mut path = theta_star::find_path(&grid, start_pos, goal);
            let end_time = Instant::now();
            println!(
                "Solve took {} secs; found: {}",
                end_time.duration_since(start_time).as_secs_f32(),
                path.is_some()
            );

            let data = match &mut path {
                Some(path) => {
                    if start != start_pos {
                        path.insert(0, start);
                    }

                    let mut buf = BytesMut::with_capacity(5 + 8 * path.len());
                    buf.put_u8(1);
                    buf.put_i32((path.len() + 1) as i32);
                    for point in path {
                        let pose = state_to_pose(point.x as f32, point.y as f32);
                        buf.put_f64(pose.bottom_angle);
                        buf.put_f64(pose.top_angle);
                    }
                    buf.put_f64(goal_pose.bottom_angle);
                    buf.put_f64(goal_pose.top_angle);
                    // println!("Yes path");
                    buf
                }
                None => {
                    // println!("No path");
                    BytesMut::zeroed(1)
                }
            };

            data
        } else {
            // println!("Bad goal");
            BytesMut::zeroed(1)
        };

        send_tx
            .send(Message {
                name: "Pathfinding:Path".to_string(),
                data,
            })
            .await
            .unwrap();
    }
}
