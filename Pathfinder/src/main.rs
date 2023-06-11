use arm::ArmPose;
use bytes::{Buf, BufMut, BytesMut};
use lerp::Lerp;
use std::{f64::consts::PI, thread, time::Duration};
use vectors::Vec2i;

use crate::messenger::Message;

pub mod arm;
pub mod collision;
pub mod dijkstra;
pub mod grid;
pub mod messenger;
pub mod theta_star;
pub mod vectors;

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

fn pose_to_state(pose: ArmPose) -> Vec2i {
    println!("Y infos: top: {} range: {:?} wrap: {} pct: {} sz: {}",
    pose.top_angle,
    TOP_RANGE,
    wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1),
    percent(wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1), TOP_RANGE.0, TOP_RANGE.1),
    (percent(wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1), TOP_RANGE.0, TOP_RANGE.1)
                * (STATE_SZ.y as f64)) as i32
    );

    Vec2i {
        x: (percent(wrap(pose.bottom_angle, BOTTOM_RANGE.0, BOTTOM_RANGE.1), BOTTOM_RANGE.0, BOTTOM_RANGE.1)
                           * (STATE_SZ.x as f64)) as i32,
        y: (percent(wrap(pose.top_angle, TOP_RANGE.0, TOP_RANGE.1), TOP_RANGE.0, TOP_RANGE.1)
                           * (STATE_SZ.y as f64)) as i32,
    }
}

async fn handle_connection(grid: &grid::Grid2D) -> Result<(), std::io::Error> {
    let mut msg = messenger::MessengerClient::connect("localhost:5805", "Pathfinding").await?;
    msg.listen("Pathfinding:Calc").await?;
    println!("Connected");

    loop {
        let m = msg.read_message().await?;
        println!("Got: {:?}", m);

        match m.name.as_str() {
            "Pathfinding:Calc" => {
                let mut d = m.data;
                let start_bot = d.get_f64();
                let start_top = d.get_f64();
                let goal_bot = d.get_f64();
                let goal_top = d.get_f64();

                let start = pose_to_state(ArmPose {
                    bottom_angle: start_bot,
                    top_angle: start_top,
                });
                let goal = pose_to_state(ArmPose {
                    bottom_angle: goal_bot,
                    top_angle: goal_top,
                });

                println!("Pathing from {:?} to {:?}", start, goal);

                if let Some(start_pos) = dijkstra::find_nearest_passable(grid, start) {
                    if let Some(goal_pos) = dijkstra::find_nearest_passable(grid, goal) {
                        let data = match theta_star::find_path(grid, start_pos, goal_pos) {
                            Some(path) => {
                                let mut buf = BytesMut::with_capacity(5 + 8 * path.len());
                                buf.put_u8(1);
                                buf.put_i32(path.len() as i32);
                                for point in path {
                                    let pose = state_to_pose(point.x as f32, point.y as f32);
                                    buf.put_f64(pose.bottom_angle);
                                    buf.put_f64(pose.top_angle);
                                }
                                println!("Yes path");
                                buf
                            }
                            None => {
                                println!("No path");
                                BytesMut::zeroed(1)
                            }
                        };

                        msg.send_message(Message {
                            name: "Pathfinding:Path".to_string(),
                            data,
                        })
                        .await?;
                    }
                }
            }
            _ => {}
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

    loop {
        if let Err(e) = handle_connection(&grid).await {
            eprintln!("Lost connection: {}", e);
            thread::sleep(Duration::from_secs(1));
        }
    }
}
