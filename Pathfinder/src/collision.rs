// Positions of shapes are their centers

use crate::vectors::Vec2f;

pub struct Circle {
    pub position: Vec2f,
    pub radius: f64,
}

pub struct Rectangle {
    pub position: Vec2f,
    pub size: Vec2f,
    pub rotation: f64,
    pub inverted: bool,
}

pub fn circle_rect_collides(circle: &Circle, rect: &Rectangle) -> bool {
    let global_rel = circle.position - rect.position;
    let rel = global_rel.rotated(-rect.rotation);
    let half_sz = rect.size / 2.0;

    if rect.inverted {
        if rel.x.abs() > half_sz.x || rel.y.abs() > half_sz.y {
            return true;
        }

        let n = half_sz + rel;
        let p = half_sz - rel;

        let min = n.x.min(p.x).min(n.y.min(p.y));
        return min <= circle.radius;
    }

    let closest = Vec2f {
        x: rel.x.clamp(-half_sz.x, half_sz.x),
        y: rel.y.clamp(-half_sz.y, half_sz.y),
    };

    let delta = rel - closest;

    delta.mag_sq() < circle.radius * circle.radius
}
