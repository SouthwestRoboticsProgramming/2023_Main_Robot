use std::collections::HashMap;

use crate::{grid::Grid2D, vectors::Vec2i};
use ordered_float::OrderedFloat;
use priority_queue::PriorityQueue;

pub fn find_nearest_passable(grid: &Grid2D, start_pos: Vec2i) -> Option<Vec2i> {
    let mut cost_so_far = HashMap::new();
    let mut open = PriorityQueue::new();

    open.push(start_pos, OrderedFloat(0.0));
    cost_so_far.insert(start_pos, 0.0);

    loop {
        match open.pop() {
            Some((pos, _)) => {
                if grid.can_point_pass(&pos) {
                    return Some(pos);
                }

                for neighbor in grid.get_all_neighbors(&pos) {
                    let new_cost = cost_so_far.get(&pos).unwrap() + grid.cost(&pos, &neighbor);
                    if match cost_so_far.get(&neighbor) {
                        Some(cost) => new_cost < *cost,
                        None => true,
                    } {
                        cost_so_far.insert(neighbor, new_cost);
                        open.push(neighbor, OrderedFloat(-new_cost));
                    }
                }
            }
            None => return None,
        }
    }
}
