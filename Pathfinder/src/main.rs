use vectors::Vec2i;

pub mod graph;
pub mod theta_star;
pub mod vectors;

use macroquad::prelude::{
    clear_background, next_frame, BLUE, GREEN, LIGHTGRAY, RED, WHITE, YELLOW,
};
use macroquad::shapes::*;
use std::time::Instant;

const GRID_W: i32 = 128;
const GRID_H: i32 = 128;
const CELL_SZ: f32 = 4.0;

#[macroquad::main("Pathfinder test")]
async fn main() {
    let mut grid = graph::Grid2D::new(GRID_W as u32, GRID_H as u32);
    let mut start_pos = Vec2i { x: 1, y: 1 };
    let mut goal_pos = Vec2i { x: 15, y: 7 };

    for x in 0..GRID_W {
        grid.set_cell_passable(Vec2i { x, y: 0 }, false);
        grid.set_cell_passable(Vec2i { x, y: GRID_H - 1 }, false);
    }
    for y in 1..GRID_H - 1 {
        grid.set_cell_passable(Vec2i { x: 0, y }, false);
        grid.set_cell_passable(Vec2i { x: GRID_W - 1, y }, false);
    }

    for y in 2..GRID_H - 2 {
        for x in 2..GRID_W - 2 {
            let r: f32 = rand::random();
            grid.set_cell_passable(Vec2i { x, y }, r > 0.5);
        }
    }

    loop {
        clear_background(LIGHTGRAY);

        for y in 0..GRID_H {
            for x in 0..GRID_W {
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

        let mouse_pos = macroquad::input::mouse_position();
        let mouse_grid_pos = Vec2i {
            x: (mouse_pos.0 / CELL_SZ + 0.5) as i32,
            y: (mouse_pos.1 / CELL_SZ + 0.5) as i32,
        };
        if mouse_grid_pos.x >= 0
            && mouse_grid_pos.y >= 0
            && mouse_grid_pos.x < GRID_W
            && mouse_grid_pos.y < GRID_H
        {
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
                        3.0,
                        YELLOW,
                    );
                }
            }
            None => {}
        }

        next_frame().await
    }
}
