use crate::vectors::Vec2i;
use bit_vec::BitVec;

pub struct GridBuilder {
    bias_x: f64,
    bias_y: f64,
    size: Vec2i,
}

impl GridBuilder {
    pub fn new(size: Vec2i) -> Self {
        Self {
            bias_x: 1.0,
            bias_y: 1.0,
            size,
        }
    }

    pub fn with_bias(&mut self, bias_x: f64, bias_y: f64) -> &mut Self {
        self.bias_x = bias_x;
        self.bias_y = bias_y;
        self
    }

    pub fn build(&self) -> Grid2D {
        Grid2D::new(self)
    }
}

pub struct Grid2D {
    passable: BitVec,
    bias_x: f64,
    bias_y: f64,
    size: Vec2i,
}

impl Grid2D {
    fn new(builder: &GridBuilder) -> Self {
        let passable = BitVec::from_elem((builder.size.x * builder.size.y) as usize, true);

        return Self {
            passable,
            bias_x: builder.bias_x,
            bias_y: builder.bias_y,
            size: builder.size,
        };
    }

    fn cell_idx(&self, pos: &Vec2i) -> usize {
        (pos.x + pos.y * self.size.x) as usize
    }

    pub fn can_pass(&self, x: i32, y: i32) -> bool {
        if x < 0 || y < 0 || x >= self.size.x || y >= self.size.y {
            return false;
        }

        self.passable
            .get(self.cell_idx(&Vec2i { x, y }))
            .unwrap_or(false)
    }

    pub fn set_cell_passable(&mut self, pos: &Vec2i, passable: bool) {
        self.passable.set(self.cell_idx(pos), passable);
    }

    pub fn cell_size(&self) -> Vec2i {
        self.size
    }

    pub fn point_size(&self) -> Vec2i {
        Vec2i {
            x: self.size.x + 1,
            y: self.size.y + 1,
        }
    }

    pub fn heuristic(&self, point: &Vec2i, goal: &Vec2i) -> f64 {
        self.cost(point, goal)
    }

    pub fn cost(&self, current: &Vec2i, goal: &Vec2i) -> f64 {
        let dx = current.x - goal.x;
        let dy = current.y - goal.y;

        let biased_x = (dx as f64) * self.bias_x;
        let biased_y = (dy as f64) * self.bias_y;

        (biased_x * biased_x + biased_y * biased_y).sqrt()
    }

    pub fn get_neighbors(&self, current: &Vec2i) -> Vec<Vec2i> {
        let mut out = Vec::with_capacity(8);

        let Vec2i { x: w, y: h } = self.point_size();
        for x in (-1)..=1 {
            for y in (-1)..=1 {
                if x == 0 && y == 0 {
                    continue;
                }

                let px = current.x + x;
                let py = current.y + y;
                let p = Vec2i { x: px, y: py };

                if px >= 0 && px < w && py >= 0 && py < h && self.line_of_sight(current, &p) {
                    out.push(p);
                }
            }
        }

        out
    }

    pub fn line_of_sight(&self, s: &Vec2i, sp: &Vec2i) -> bool {
        let mut x0 = s.x;
        let mut y0 = s.y;
        let x1 = sp.x;
        let y1 = sp.y;
        let mut dy = y1 - y0;
        let mut dx = x1 - x0;
        let mut f = 0;

        let sy;
        let sx;

        if dy < 0 {
            dy = -dy;
            sy = -1;
        } else {
            sy = 1;
        }

        if dx < 0 {
            dx = -dx;
            sx = -1;
        } else {
            sx = 1;
        }

        if dx >= dy {
            while x0 != x1 {
                f = f + dy;
                if f >= dx {
                    if !self.can_pass(x0 + ((sx - 1) / 2), y0 + ((sy - 1) / 2)) {
                        return false;
                    }
                    y0 = y0 + sy;
                    f = f - dx;
                }
                if f != 0 && !self.can_pass(x0 + ((sx - 1) / 2), y0 + ((sy - 1) / 2)) {
                    return false;
                }
                if dy == 0
                    && !self.can_pass(x0 + ((sx - 1) / 2), y0)
                    && !self.can_pass(x0 + ((sx - 1) / 2), y0 - 1)
                {
                    return false;
                }
                x0 = x0 + sx;
            }
        } else {
            while y0 != y1 {
                f = f + dx;
                if f >= dy {
                    if !self.can_pass(x0 + ((sx - 1) / 2), y0 + ((sy - 1) / 2)) {
                        return false;
                    }
                    x0 = x0 + sx;
                    f = f - dy;
                }
                if f != 0 && !self.can_pass(x0 + ((sx - 1) / 2), y0 + ((sy - 1) / 2)) {
                    return false;
                }
                if dx == 0
                    && !self.can_pass(x0, y0 + ((sy - 1) / 2))
                    && !self.can_pass(x0 - 1, y0 + ((sy - 1) / 2))
                {
                    return false;
                }
                y0 = y0 + sy;
            }
        }

        return true;
    }
}
