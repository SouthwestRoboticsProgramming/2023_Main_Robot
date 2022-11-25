# MathLib

The MathLib project aims to make frequently used math, such as operations on angles and vectors quicker to implement, and more reliable.

#### **Angle**
The `Angle` classes aim to increase the consistency of angle measurements by forcing an absolute direction and adding units.
This makes it much less likely to  mess up clockwise vs counterclockwise.

#### **Vec2d**
The `Vec2d` class functions as a replacment for `Translation2d` with more functions and features.

#### **CoordinateConversions**
The `CoordinateConversions` class translates between WPI's coordinates (Relative to official's table) and Ultraviolet's coordinates (Relative to the driver).

#### **MathUtil**
The `MathUtil` class offers handy features such as linear interpolation and clamping.
