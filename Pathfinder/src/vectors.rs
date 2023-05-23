use std::ops::{Add, Div, Mul, Sub};

#[derive(Clone, Copy, PartialEq, Eq, Hash, Debug)]
pub struct Vec2i {
    pub x: i32,
    pub y: i32,
}

impl Vec2i {
    pub fn mag(&self) -> f64 {
        ((self.x * self.x + self.y * self.y) as f64).sqrt()
    }
}

impl Sub for Vec2i {
    type Output = Vec2i;

    fn sub(self, rhs: Self) -> Self::Output {
        Vec2i {
            x: self.x - rhs.x,
            y: self.y - rhs.y,
        }
    }
}

#[derive(Clone, Copy, PartialEq, Debug)]
pub struct Vec2f {
    pub x: f64,
    pub y: f64,
}

impl Vec2f {
    pub fn new_from_angle(angle: f64, magnitude: f64) -> Self {
        Self {
            x: angle.cos() * magnitude,
            y: angle.sin() * magnitude,
        }
    }

    pub fn rotated(&self, angle: f64) -> Self {
        let sin = angle.sin();
        let cos = angle.cos();

        Self {
            x: cos * self.x - sin * self.y,
            y: sin * self.x + cos * self.y,
        }
    }

    pub fn mag_sq(&self) -> f64 {
        return self.x * self.x + self.y * self.y;
    }
}

impl Add for Vec2f {
    type Output = Vec2f;

    fn add(self, rhs: Self) -> Self::Output {
        Self {
            x: self.x + rhs.x,
            y: self.y + rhs.y,
        }
    }
}

impl Sub for Vec2f {
    type Output = Vec2f;

    fn sub(self, rhs: Self) -> Self::Output {
        Self {
            x: self.x - rhs.x,
            y: self.y - rhs.y,
        }
    }
}

impl Mul<f64> for Vec2f {
    type Output = Vec2f;

    fn mul(self, rhs: f64) -> Self::Output {
        Self {
            x: self.x * rhs,
            y: self.y * rhs,
        }
    }
}

impl Div<f64> for Vec2f {
    type Output = Vec2f;

    fn div(self, rhs: f64) -> Self::Output {
        Self {
            x: self.x / rhs,
            y: self.y / rhs,
        }
    }
}
