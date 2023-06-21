use arm::ArmPose;
use bytes::{Buf, BufMut, BytesMut};
use cfg_if::cfg_if;
use lerp::Lerp;
use std::{
    error::Error,
    f64::consts::PI,
    sync::{Arc, Mutex},
    thread,
    time::Duration,
};
use tokio::sync::mpsc;
use vectors::Vec2i;

use crate::messenger::Message;

pub mod arm;
pub mod collision;
pub mod dijkstra;
pub mod grid;
pub mod messenger;
pub mod theta_star;
pub mod vectors;

cfg_if! {
    if #[cfg(feature = "graphics")] {
        mod graphics;
        use graphics::GraphicsState;
    }
}

pub const STATE_SZ: Vec2i = Vec2i { x: 128, y: 128 };
pub const BOTTOM_RANGE: (f64, f64) = (0.0, PI);
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
    // println!(
    //     "Y infos: top: {} range: {:?} wrap: {} pct: {} sz: {}",
    //     pose.top_angle,
    //     TOP_RANGE,
    //     wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1),
    //     percent(
    //         wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1),
    //         TOP_RANGE.0,
    //         TOP_RANGE.1
    //     ),
    //     (percent(
    //         wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1),
    //         TOP_RANGE.0,
    //         TOP_RANGE.1
    //     ) * (STATE_SZ.y as f64)) as i32
    // );

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
    println!("Connected");

    loop {
        tokio::select! {
            m_result = msg.read_message() => match m_result {
                Ok(m) => {
                    // println!("Got message: {}", m.name);
                    recv_tx.send(m).await?;
                },
                Err(e) => return Err(Box::new(e))
            },

            to_send_opt = send_rx.recv() => match to_send_opt {
                Some(to_send) => {
                    // println!("Sending message: {}", to_send.name);
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

    let grid = Arc::new(grid);

    let mut start_pose = ArmPose {
        bottom_angle: 0.0,
        top_angle: 0.0,
    };
    let mut goal_pose = ArmPose {
        bottom_angle: 0.0,
        top_angle: 0.0,
    };

    #[cfg(feature = "graphics")]
    let graphics_state = Arc::new(Mutex::new(GraphicsState {
        start_state: pose_to_state(&start_pose),
        path_start_state: pose_to_state(&start_pose),
        goal_state: pose_to_state(&goal_pose),
        path: None,
    }));
    #[cfg(feature = "graphics")]
    graphics::show_graphics_window(grid.clone(), graphics_state.clone());

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
        #[cfg(feature = "graphics")]
        {
            let mut state = graphics_state.lock().unwrap();
            state.start_state = start;
            state.goal_state = goal;
        }
        // println!("Pathing from {:?} to {:?}", start, goal);

        let data = if let Some(start_pos) = dijkstra::find_nearest_passable(&grid, start) {
            #[cfg(feature = "graphics")]
            {
                graphics_state.lock().unwrap().path_start_state = start_pos;
            }

            let path = theta_star::find_path(&grid, start_pos, goal);

            let data = match &path {
                Some(path) => {
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

            #[cfg(feature = "graphics")]
            {
                graphics_state.lock().unwrap().path = path;
            }

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
