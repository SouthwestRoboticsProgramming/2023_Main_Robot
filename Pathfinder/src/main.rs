// TODO: Some way of getting the arm out of invalid states
//       At the moment, the pathfinder won't report a path if either current or goal is invalid

use collision::{Circle, Rectangle};
use graph::Grid2D;
use lerp::Lerp;
use std::f64::consts::PI;
use std::time::Instant;
use vectors::Vec2f;

pub mod collision;
pub mod graph;
pub mod theta_star;
pub mod vectors;

// Arm dimensions (FIXME: Wrong)
pub const BOTTOM_LEN: f64 = 1.0; // Distance from bottom axis to midpoint axis
pub const TOP_LEN: f64 = 0.8; // Distance from midpoint axis to wrist axis
pub const INTAKE_RAD: f64 = 0.1; // Maximum distance of intake from wrist axis
pub const MIDPOINT_RAD: f64 = 0.1; // Radius of middle joint pulley

pub const BOTTOM_GEAR_RATIO: f64 = 600.0;
pub const TOP_GEAR_RATIO: f64 = 300.0;

// Limitations from game rules
const HEIGHT_LIMIT: f64 = 1.98;
const EXTENSION_LIMIT: f64 = 1.22;

// Limitations from robot (FIXME: Wrong)
const FLOOR_HEIGHT: f64 = 0.2; // Vertical distance from floor to bottom joint pivot axis
const BUMPER_THICKNESS: f64 = 0.05; // Thickness of bumper from frame edge to outer edge
const FRAME_SIZE: f64 = 0.53; // Size of drive base without bumpers
const BASE_SIZE: f64 = FRAME_SIZE + 2.0 * BUMPER_THICKNESS;
const BASE_HEIGHT: f64 = 0.1; // Distance from floor to top of drive base

// TODO: Grid collision rectangles

const COLLISION_RECTS: [Rectangle; 2] = [
    Rectangle {
        // Bounds of allowed region by rules and the existence of the floor
        position: Vec2f {
            x: 0.0,
            y: -FLOOR_HEIGHT + (HEIGHT_LIMIT + FLOOR_HEIGHT) / 2.0,
        },
        size: Vec2f {
            x: EXTENSION_LIMIT * 2.0,
            y: HEIGHT_LIMIT + FLOOR_HEIGHT,
        },
        rotation: 0.0,
        inverted: true,
    },
    Rectangle {
        // Drive base collision
        position: Vec2f {
            x: 0.0,
            y: -FLOOR_HEIGHT + BASE_HEIGHT / 2.0,
        },
        size: Vec2f {
            x: BASE_SIZE,
            y: BASE_HEIGHT,
        },
        rotation: 0.0,
        inverted: false,
    },
];

pub const STATE_SZ: Vec2i = Vec2i { x: 128, y: 128 };
pub const BOTTOM_RANGE: (f64, f64) = (0.0, PI);
pub const TOP_RANGE: (f64, f64) = (-1.5 * PI, 0.5 * PI);

pub struct ArmPose {
    pub bottom_angle: f64,
    pub top_angle: f64,
}

impl ArmPose {
    pub fn get_midpoint(&self) -> Vec2f {
        Vec2f::new_from_angle(self.bottom_angle, BOTTOM_LEN)
    }

    pub fn get_endpoint(&self) -> Vec2f {
        let midpoint = self.get_midpoint();
        let rel_endpoint = Vec2f::new_from_angle(self.top_angle, TOP_LEN);

        midpoint + rel_endpoint
    }

    pub fn is_valid(&self) -> bool {
        let midpoint = self.get_midpoint();
        let endpoint = self.get_endpoint();

        // Prevent exiting frame perimeter on both sides
        // If both sides are extending outside frame perimeter, and it's opposite sides, invalid
        let endpoint_extent = endpoint.x.abs() + INTAKE_RAD;
        let midpoint_extent = midpoint.x.abs() + MIDPOINT_RAD;
        if endpoint_extent >= FRAME_SIZE / 2.0
            && midpoint_extent >= FRAME_SIZE / 2.0
            && midpoint.x.signum() != endpoint.x.signum()
        {
            return false;
        }

        let circle = Circle {
            position: endpoint,
            radius: INTAKE_RAD,
        };

        for rect in COLLISION_RECTS {
            if collision::circle_rect_collides(&circle, &rect) {
                return false;
            }
        }

        true
    }
}

use macroquad::color::*;
use macroquad::shapes::*;
use macroquad::window::clear_background;
use macroquad::window::next_frame;
use vectors::Vec2i;

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

const CELL_SZ: f32 = 4.0;

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

#[macroquad::main("Arm pathing")]
async fn main() {
    // We use X axis for bottom joint and Y axis for top joint

    let bias_x = (BOTTOM_RANGE.1 - BOTTOM_RANGE.0) * BOTTOM_GEAR_RATIO;
    let bias_y = (TOP_RANGE.1 - TOP_RANGE.0) * TOP_GEAR_RATIO;

    let mut grid = graph::GridBuilder::new(STATE_SZ)
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

    let mut start_pos = Vec2i { x: 10, y: 10 };
    let mut goal_pos = Vec2i { x: 100, y: 100 };

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
            let left_down =
                macroquad::input::is_mouse_button_down(macroquad::input::MouseButton::Left);
            let right_down =
                macroquad::input::is_mouse_button_down(macroquad::input::MouseButton::Right);

            if left_down {
                start_pos = mouse_grid_pos;
            }
            if right_down {
                goal_pos = mouse_grid_pos;
            }

            draw_state_preview(mouse_grid_pos.x as f32, mouse_grid_pos.y as f32);
        }

        draw_circle(
            start_pos.x as f32 * CELL_SZ,
            start_pos.y as f32 * CELL_SZ,
            5.0,
            GREEN,
        );
        draw_circle(
            goal_pos.x as f32 * CELL_SZ,
            goal_pos.y as f32 * CELL_SZ,
            5.0,
            BLUE,
        );

        let start_time = Instant::now();
        let path_opt = theta_star::find_path(&grid, start_pos, goal_pos);
        let elapsed = start_time.elapsed();

        println!("Elapsed time: {}", elapsed.as_secs_f32());

        match path_opt {
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
                    let secs_passed = begin_time.elapsed().as_secs_f64();

                    let mut total_len = 0.0;
                    for i in 1..path.len() {
                        let prev = path[i - 1];
                        let curr = path[i];
                        total_len += (prev - curr).mag();
                    }

                    let len_per_sec = 35.0;
                    let total_time = total_len / len_per_sec;

                    let progress = (secs_passed % total_time) / total_time * total_len;
                    println!("Progress: {}, {}", progress, total_len);
                    let mut len_so_far = 0.0;
                    for i in 1..path.len() {
                        let prev = path[i - 1];
                        let curr = path[i];

                        let diff_vec = prev - curr;
                        let mag = diff_vec.mag();

                        let diff_prog = progress - len_so_far;
                        if diff_prog > mag {
                            len_so_far += mag;
                            continue;
                        }

                        let f = (diff_prog / mag) as f32;
                        let x = (prev.x as f32).lerp(curr.x as f32, f);
                        let y = (prev.y as f32).lerp(curr.y as f32, f);

                        draw_state_preview(x, y);
                        break;
                    }
                }
            }
            None => {}
        }

        next_frame().await
    }
}
