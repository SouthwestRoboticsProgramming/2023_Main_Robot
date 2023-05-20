use std::cell::{Cell, RefCell};

use ordered_float::OrderedFloat;
use priority_queue::PriorityQueue;

use crate::{graph::Grid2D, vectors::Vec2i};

struct Node<'a> {
    position: Vec2i,
    parent: RefCell<Option<&'a Node<'a>>>,

    closed: Cell<bool>,
    cost: Cell<f64>,
}

struct NodeStorage<'a> {
    nodes: Vec<Node<'a>>,
    width: i32,
}

impl<'a> NodeStorage<'a> {
    fn new(width: i32, height: i32) -> Self {
        let node_count = width * height;
        let mut nodes = Vec::with_capacity(node_count as usize);
        for y in 0..height {
            for x in 0..width {
                nodes.push(Node {
                    position: Vec2i { x, y },
                    parent: RefCell::new(None),
                    closed: Cell::new(false),
                    cost: Cell::new(0.0),
                });
            }
        }

        Self { nodes, width }
    }

    fn get(&self, position: &Vec2i) -> &Node<'a> {
        &self.nodes[(position.x + position.y * self.width) as usize]
    }
}

fn compute_cost<'a>(grid: &Grid2D, current: &'a Node<'a>, next: &'a Node<'a>) {
    let current_parent_opt = current.parent.borrow();
    if current_parent_opt.is_some() {
        let current_parent = current_parent_opt.unwrap();
        if grid.line_of_sight(&current_parent.position, &next.position) {
            let new_cost =
                current_parent.cost.get() + grid.cost(&current_parent.position, &next.position);

            if new_cost < next.cost.get() {
                next.parent.replace(Some(current_parent));
                next.cost.set(new_cost);
            }

            return;
        }
    }
    drop(current_parent_opt);

    let cost = current.cost.get() + grid.cost(&current.position, &next.position);
    if cost < next.cost.get() {
        next.parent.replace(Some(current));
        next.cost.set(cost);
    }
}

fn update_vertex<'a>(
    grid: &Grid2D,
    open: &mut PriorityQueue<Vec2i, OrderedFloat<f64>>,
    current: &'a Node<'a>,
    next: &'a Node<'a>,
    goal_pos: &Vec2i,
) {
    let old_cost = next.cost.get();
    compute_cost(grid, current, next);
    if next.cost.get() < old_cost {
        open.push(
            next.position,
            OrderedFloat(-next.cost.get() - grid.heuristic(&next.position, goal_pos)),
        );
    }
}

fn extract_path(end_node: &Node) -> Vec<Vec2i> {
    let mut out = Vec::new();
    let mut current_node = Some(end_node);

    while let Some(node) = current_node {
        out.insert(0, node.position);
        current_node = node.parent.borrow_mut().take();
    }

    out
}

pub fn find_path(grid: &Grid2D, start_pos: Vec2i, goal_pos: Vec2i) -> Option<Vec<Vec2i>> {
    let Vec2i {
        x: width,
        y: height,
    } = grid.point_size();
    let nodes = NodeStorage::new(width, height);

    let mut open = PriorityQueue::new();
    open.push(
        start_pos,
        OrderedFloat(-grid.heuristic(&start_pos, &goal_pos)),
    );

    while !open.is_empty() {
        // Should always be present since open is not empty
        let (current_pos, _) = open.pop().unwrap();
        let current_node = nodes.get(&current_pos);
        if current_pos == goal_pos {
            return Some(extract_path(current_node));
        }

        current_node.closed.set(true);

        let neighbor_points = grid.get_neighbors(&current_pos);
        for neighbor_pos in neighbor_points {
            let neighbor_node = nodes.get(&neighbor_pos);
            if !neighbor_node.closed.get() {
                match open.get(&neighbor_pos) {
                    None => {
                        neighbor_node.cost.set(f64::INFINITY);
                        neighbor_node.parent.replace(None);
                    }
                    Some(_) => {}
                }
                update_vertex(grid, &mut open, current_node, neighbor_node, &goal_pos);
            }
        }
    }

    None
}

// use priority_queue::PriorityQueue;

// use crate::{graph::Grid2D, vectors::Vec2i};

// struct Node {
//     position: Vec2i,
//     closed: bool,
//     cost: f64,
//     parent_pos: Option<Vec2i>,
// }

// fn to_ord(f: f64) -> i64 {
//     (f * (1 << 16) as f64) as i64
// }

// fn update_vertex(open: &mut PriorityQueue<Vec2i, i64>, current: &Node, next: &mut Node) {}

// fn extract_path(node: &Node) -> Vec<Vec2i> {
//     todo!();
// }

// pub fn find_path(grid: Grid2D, start_pos: Vec2i, goal_pos: Vec2i) -> Option<Vec<Vec2i>> {
//     let grid_size = grid.point_size();
//     let node_count = grid_size.x * grid_size.y;

//     let mut nodes = Vec::<Node>::with_capacity(node_count as usize);
//     for i in 0..node_count {
//         nodes.push(Node {
//             position: Vec2i {
//                 x: i % grid_size.x,
//                 y: i / grid_size.x,
//             },
//             closed: false,
//             cost: 0.0,
//             parent_pos: None,
//         });
//     }

//     let mut open = priority_queue::PriorityQueue::new();
//     open.push(start_pos, to_ord(grid.heuristic(&start_pos, &goal_pos)));

//     while !open.is_empty() {
//         let (current_pos, _) = open.pop().unwrap();
//         let current_node;
//         {
//             let current_node_mut = nodes
//                 .get_mut((current_pos.x + current_pos.y * grid_size.x) as usize)
//                 .unwrap();
//             if current_pos == goal_pos {
//                 return Option::Some(extract_path(current_node_mut));
//             }

//             current_node_mut.closed = true;
//             current_node = &*current_node_mut; // Don't let mutability escape
//         }

//         let neighbor_positions = grid.get_neighbors(&current_pos);
//         for neighbor_pos in neighbor_positions {
//             let neighbor_node = nodes
//                 .get_mut((neighbor_pos.x + neighbor_pos.y * grid_size.x) as usize)
//                 .unwrap();
//             if !neighbor_node.closed {
//                 match open.get(&neighbor_pos) {
//                     None => {
//                         neighbor_node.cost = f64::INFINITY;
//                         neighbor_node.parent_pos = None;
//                     }
//                     Some(_) => {}
//                 }
//                 update_vertex(&mut open, current_node, neighbor_node);
//             }
//         }
//     }

//     todo!();
// }
