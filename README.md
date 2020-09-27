# About

This is a Bezier Curve demonstration which may not look visually incredible, but contains a significant amount of sweat and math.
I recommend familiarizing yourself first with the concept of a [Bezier curve](https://en.wikipedia.org/wiki/B%C3%A9zier_curve) if you are not already. 
Another good link is [this page](https://pages.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/Bezier/bezier-der.html) on the derivative of a n degree bezier curve.

# Purpose

This started as an optimization to the Unit sight algorithm in my game, [Base Wars](https://everlastinggames.net/base-wars). Unit/terrain shapes are stored in the awt.Area class, which is comprised of (surprise!) Bezier curves.
I recognized that finding the derivative with respect to a point (unit vision raycasting) was roughly a 1.25x increase in speed compared to derivative estimation.
The linear case was trivial; The Quad case came after some work. The cubic case requires a degree 5 polynomial, which I am not going to implement a solver for.

# Math

We have the equation of a n-degree Bezier curve, B<sub>x</sub>(t) and B<sub>y</sub>(t).
We have the dereivative of it, B'<sub>x</sub>(t) and B'<sub>y</sub>(t).
And we have a point (x, y).
We want the [0, n] possible equations tangent to the curve that intersect (x, y).

These may be computed through the observation (B<sub>y</sub>(t) - y)/(B<sub>x</sub>(t) - x) -= B'<sub>y</sub>(t)/B'<sub>x</sub>(t)
This results in a polynomial of degree n(n-1).

We have implemented the cubic formula for solving the described Bezier equations.

# Instructions

Compile src/Main.java and run it. It should create a window. Drag the mouse around to view the tangents; click to generate a new Bezier curve.
