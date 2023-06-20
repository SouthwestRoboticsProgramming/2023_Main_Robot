use lerp::Lerp;
use macroquad::{prelude::*, Window};
use std::{
    sync::{Arc, Mutex},
    thread,
    time::Instant,
};

use crate::{
    arm::{COLLISION_RECTS, FRAME_SIZE, INTAKE_RAD, MIDPOINT_RAD},
    grid::Grid2D,
    state_to_pose,
    vectors::{Vec2f, Vec2i},
    STATE_SZ,
};

#[derive(Clone)]
pub struct GraphicsState {
    pub start_state: Vec2i,
    pub path_start_state: Vec2i,
    pub goal_state: Vec2i,

    pub path: Option<Vec<Vec2i>>,
}

pub fn show_graphics_window(grid: Arc<Grid2D>, state: Arc<Mutex<GraphicsState>>) {
    thread::spawn(|| Window::new("Pathfinder", draw(grid, state)));
}

const CELL_SZ: f32 = 3.0;

fn polyline(positions: Vec<Vec2f>, thickness: f32, color: Color) {
    let count = positions.len();
    for i in 0..count {
        let curr = positions[i];
        let next;
        if i == count - 1 {
            next = positions[0];
        } else {
            next = positions[i + 1];
        }

        draw_line(
            curr.x as f32,
            curr.y as f32,
            next.x as f32,
            next.y as f32,
            thickness,
            color,
        );
    }
}

fn draw_state_space_grid(grid: &Grid2D) {
    for y in 0..STATE_SZ.y {
        for x in 0..STATE_SZ.x {
            let color;
            if grid.can_pass(x, y) {
                color = WHITE;
            } else {
                color = RED;
            }

            draw_rectangle(
                x as f32 * CELL_SZ,
                y as f32 * CELL_SZ,
                CELL_SZ,
                CELL_SZ,
                color,
            );
        }
    }
}

fn draw_state_preview(state_x: f32, state_y: f32) {
    let meter_scale: f64 = 100.0;
    let pose = state_to_pose(state_x, state_y);
    let origin = Vec2f { x: 800.0, y: 300.0 };
    let midpoint = pose.get_midpoint() * meter_scale;
    let endpoint = pose.get_endpoint() * meter_scale;

    draw_line(
        origin.x as f32,
        origin.y as f32,
        (origin.x + midpoint.x) as f32,
        (origin.y + midpoint.y) as f32,
        3.0,
        DARKGREEN,
    );
    draw_line(
        (origin.x + midpoint.x) as f32,
        (origin.y + midpoint.y) as f32,
        (origin.x + endpoint.x) as f32,
        (origin.y + endpoint.y) as f32,
        3.0,
        GREEN,
    );

    for rect in COLLISION_RECTS {
        let center = origin + rect.position * meter_scale;
        let corner_rel = rect.size / 2.0 * meter_scale;

        polyline(
            [
                center + corner_rel.rotated(rect.rotation),
                center
                    + Vec2f {
                        x: -corner_rel.x,
                        y: corner_rel.y,
                    }
                    .rotated(rect.rotation),
                center - corner_rel.rotated(rect.rotation),
                center
                    + Vec2f {
                        x: corner_rel.x,
                        y: -corner_rel.y,
                    }
                    .rotated(rect.rotation),
            ]
            .to_vec(),
            3.0,
            ORANGE,
        );
    }

    let scaled_frame = FRAME_SIZE * meter_scale;
    draw_line(
        (origin.x + scaled_frame / 2.0) as f32,
        origin.y as f32 - 1000.0,
        (origin.x + scaled_frame / 2.0) as f32,
        origin.y as f32 + 1000.0,
        2.0,
        YELLOW,
    );
    draw_line(
        (origin.x - scaled_frame / 2.0) as f32,
        origin.y as f32 - 1000.0,
        (origin.x - scaled_frame / 2.0) as f32,
        origin.y as f32 + 1000.0,
        2.0,
        YELLOW,
    );

    draw_circle_lines(
        (origin.x + endpoint.x) as f32,
        (origin.y + endpoint.y) as f32,
        (INTAKE_RAD * meter_scale) as f32,
        3.0,
        RED,
    );

    draw_circle_lines(
        (origin.x + midpoint.x) as f32,
        (origin.y + midpoint.y) as f32,
        (MIDPOINT_RAD * meter_scale) as f32,
        3.0,
        RED,
    );
}

async fn draw(grid: Arc<Grid2D>, state: Arc<Mutex<GraphicsState>>) {
    let begin_time = Instant::now();
    loop {
        clear_background(LIGHTGRAY);
        draw_state_space_grid(&grid);

        let mouse_pos = macroquad::input::mouse_position();
        let mouse_grid_pos = Vec2i {
            x: (mouse_pos.0 / CELL_SZ + 0.5) as i32,
            y: (mouse_pos.1 / CELL_SZ + 0.5) as i32,
        };
        let show_preview = mouse_grid_pos.x >= 0
            && mouse_grid_pos.y >= 0
            && mouse_grid_pos.x < STATE_SZ.x
            && mouse_grid_pos.y < STATE_SZ.y;

        if show_preview {
            draw_state_preview(mouse_grid_pos.x as f32, mouse_grid_pos.y as f32);
        }

        let state = { state.lock().unwrap().clone() };
        draw_circle(
            state.start_state.x as f32 * CELL_SZ,
            state.start_state.y as f32 * CELL_SZ,
            5.0,
            GREEN,
        );
        draw_circle(
            state.goal_state.x as f32 * CELL_SZ,
            state.goal_state.y as f32 * CELL_SZ,
            5.0,
            BLUE,
        );

        match state.path {
            Some(path) => {
                for i in 1..path.len() {
                    let prev = path[i - 1];
                    let curr = path[i];
                    draw_line(
                        prev.x as f32 * CELL_SZ,
                        prev.y as f32 * CELL_SZ,
                        curr.x as f32 * CELL_SZ,
                        curr.y as f32 * CELL_SZ,
                        5.0,
                        ORANGE,
                    );
                }

                if !show_preview {
                    draw_state_preview(state.start_state.x as f32, state.start_state.y as f32);
                    // let secs_passed = begin_time.elapsed().as_secs_f64();

                    // let mut total_len = 0.0;
                    // for i in 1..path.len() {
                    //     let prev = path[i - 1];
                    //     let curr = path[i];
                    //     total_len += (prev - curr).mag();
                    // }

                    // let len_per_sec = 35.0;
                    // let total_time = total_len / len_per_sec;

                    // let progress = (secs_passed % total_time) / total_time * total_len;
                    // println!("Progress: {}, {}", progress, total_len);
                    // let mut len_so_far = 0.0;
                    // for i in 1..path.len() {
                    //     let prev = path[i - 1];
                    //     let curr = path[i];

                    //     let diff_vec = prev - curr;
                    //     let mag = diff_vec.mag();

                    //     let diff_prog = progress - len_so_far;
                    //     if diff_prog > mag {
                    //         len_so_far += mag;
                    //         continue;
                    //     }

                    //     let f = (diff_prog / mag) as f32;
                    //     let x = (prev.x as f32).lerp(curr.x as f32, f);
                    //     let y = (prev.y as f32).lerp(curr.y as f32, f);

                    //     draw_state_preview(x, y);
                    //     break;
                    // }
                }
            }
            None => {}
        }

        next_frame().await
    }
}
