import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Cubic extends CubicCurve2D.Double implements DrawableCurve {

	// How flat should it approximate? (1 = 1 px)
	private static final double FLATNESS = 1;

	private final CubicCurve2D convexCurve, concaveCurve;

	private final ArrayList<Point2D> approxPts;
	private final ArrayList<java.lang.Double> approxDist;

	public Cubic(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
		super(x0, y0, x1, y1, x2, y2, x3, y3);

		// Determine concavity pair
		// Split into subcurves (necessarily convex/concave each)
		CubicCurve2D.Double c1 = new CubicCurve2D.Double(), c2 = new CubicCurve2D.Double();
		subdivide(c1, c2);
		final double o1 = Util.orientation(c1.x1, c1.y1, (c1.ctrlx1 + c1.ctrlx2) / 2, (c1.ctrly1 + c1.ctrly2) / 2,
				c1.x2, c1.y2);
		final double o2 = Util.orientation(c2.x1, c2.y1, (c2.ctrlx1 + c2.ctrlx2) / 2, (c2.ctrly1 + c2.ctrly2) / 2,
				c2.x2, c2.y2);

		// Is it wholly concave/convex?
		if (o1 == o2) {
			if (o1 > 0) {
				convexCurve = this;
				concaveCurve = null;
			} else if (o1 < 0) {
				convexCurve = null;
				concaveCurve = this;
			} else {
				convexCurve = null;
				concaveCurve = null;
			}
		} else {
			if (o1 > 0) {
				// c1 is convex
				convexCurve = c1;
				if (o2 < 0) {
					concaveCurve = c2;
				} else {
					concaveCurve = null;
				}
			} else if (o1 < 0) {
				// c1 is concave
				concaveCurve = c1;
				if (o2 > 0) {
					convexCurve = c2;
				} else {
					convexCurve = null;
				}
			} else {
				// c1 is linear; c2 is convex or concave
				if (o2 > 0) {
					convexCurve = c2;
					concaveCurve = null;
				} else {
					concaveCurve = c2;
					convexCurve = null;
				}
			}
		}

		// NOTE: this approximation is lazy, using a builtin method (which would get the
		// distances wrong, if that's a problem.)
		approxPts = new ArrayList<Point2D>();
		approxDist = new ArrayList<java.lang.Double>();
		double distCovered = 0;
		Path2D path = new Path2D.Double();
		path.moveTo(x0, y0);
		path.curveTo(x1, y1, x2, y2, x3, y3);
		path.closePath();
		double[] coords = new double[6];
		for (PathIterator p = path.getPathIterator(null, FLATNESS); !p.isDone(); p.next()) {
			switch (p.currentSegment(coords)) {
			case PathIterator.SEG_LINETO:
				// Increment distance
				distCovered += Math.hypot(coords[0] - coords[2], coords[1] - coords[3]);
			case PathIterator.SEG_MOVETO:
				approxPts.add(new Point2D.Double(coords[0], coords[1]));
				approxDist.add(distCovered);
				// Save previous coords
				coords[2] = coords[0];
				coords[3] = coords[1];
				break;

			}
		}
	}

	public double[] getTangentPoints(double x, double y) {
		ArrayList<Point2D> foundPoints = new ArrayList<Point2D>();

		// Use flattening approximation
		for (int i = 1; i < approxPts.size() - 1; i++) {
			Point2D p = approxPts.get(i), p1 = approxPts.get(i - 1), p2 = approxPts.get(i + 1);
			if (Util.orientation(x, y, p.getX(), p.getY(), p1.getX(), p1.getY()) == Util.orientation(x, y, p.getX(),
					p.getY(), p2.getX(), p2.getY())) {
				foundPoints.add(p);
			}
		}
		// Convert to primitive
		double[] ret = new double[2 * foundPoints.size()];
		for (int i = 0; i < foundPoints.size(); i++) {
			ret[2 * i] = foundPoints.get(i).getX();
			ret[2 * i + 1] = foundPoints.get(i).getY();
		}
		return ret;
	}

	public void draw(Graphics2D g) {
		if (convexCurve != null) {
			g.setColor(new Color(255, 127, 127));
			g.fill(convexCurve);
		}
		if (concaveCurve != null) {
			g.setColor(new Color(127, 127, 255));
			g.fill(concaveCurve);
		}
		g.setColor(Color.BLACK);
		g.draw(this);

	}

//	private double[] eval(double t) {
//		double mt = 1-t;
//		return new double[] { mt * mt * mt * x1 + 3 * t * mt * mt * ctrlx1 + 3 * t * t * mt + ctrlx2 + t * t * t * x2,
//				mt * mt * mt * y1 + 3 * t * mt * mt * ctrly1 + 3 * t * t * mt + ctrly2 + t * t * t * y2 };
//	}
}