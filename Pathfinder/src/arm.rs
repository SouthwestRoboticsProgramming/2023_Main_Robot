use crate::{
    collision::{self, Circle, Rectangle},
    vectors::Vec2f,
};

const IN_PER_M: f64 = 39.37;

// Arm dimensions
pub const BOTTOM_LEN: f64 = 35.0 / IN_PER_M; // Distance from bottom axis to midpoint axis
pub const TOP_LEN: f64 = 29.95824774 / IN_PER_M; // Distance from midpoint axis to wrist axis
pub const INTAKE_RAD: f64 = (6.49785148 + 1.125) / IN_PER_M; // Maximum distance of intake from wrist axis
pub const MIDPOINT_RAD: f64 = 3.3 / IN_PER_M; // Radius of middle joint pulley

pub const BOTTOM_GEAR_RATIO: f64 = 600.0;
pub const TOP_GEAR_RATIO: f64 = 288.0;

// Limitations from game rules
const HEIGHT_LIMIT: f64 = 1.98;
const EXTENSION_LIMIT: f64 = 1.22;

// Limitations from robot
const FLOOR_HEIGHT: f64 = 9.62923832 / IN_PER_M; // Vertical distance from floor to bottom joint pivot axis
const BUMPER_THICKNESS: f64 = 3.625 / IN_PER_M; // Thickness of bumper from frame edge to outer edge
pub const FRAME_SIZE: f64 = 21.0 / IN_PER_M; // Size of drive base without bumpers
const BASE_SIZE: f64 = FRAME_SIZE + 2.0 * BUMPER_THICKNESS;
const BASE_HEIGHT: f64 = 4.7522244 / IN_PER_M; // Distance from floor to top of drive base
const MIN_BOTTOM_ANGLE: f64 = 25.0;
const MAX_BOTTOM_ANGLE: f64 = 180.0 - 25.0;

// TODO: Grid collision rectangles

pub const COLLISION_RECTS: [Rectangle; 2] = [
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
        // Prevent collision with gearbox
        let bottom_angle_deg = self.bottom_angle * 180.0 / std::f64::consts::PI;
        if bottom_angle_deg < MIN_BOTTOM_ANGLE || bottom_angle_deg > MAX_BOTTOM_ANGLE {
            return false;
        }

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
