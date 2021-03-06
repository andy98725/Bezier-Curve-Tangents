import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Cubic extends CubicCurve2D.Double implements DrawableCurve {

	protected static final int SPLIT_COUNT = 16;

	protected static final boolean ALLOW_INTERSECTING_TANS = false;

	private final CubicCurve2D convexCurve, concaveCurve;

	private final ArrayList<Point2D> approxPts;
	private final ArrayList<java.lang.Double> approxTimes;
	private final ArrayList<java.lang.Double> approxDists;

	public Cubic(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
		super(x0, y0, x1, y1, x2, y2, x3, y3);

		// Determine concavity pair
		// Split into subcurves (necessarily convex/concave each)
		CubicCurve2D.Double c1 = new CubicCurve2D.Double(), c2 = new CubicCurve2D.Double();
		splitCurve(0.5, c1, c2);
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

		approxPts = new ArrayList<Point2D>();
		approxTimes = new ArrayList<java.lang.Double>();
		approxDists = new ArrayList<java.lang.Double>();
		double distCovered = 0;
		for (double t = 0; t <= 1; t += 1.0 / SPLIT_COUNT) {
			approxTimes.add(t);
			Point2D p = eval(t);
			if (t == 0) {
				approxDists.add(0.0);
			} else {
				distCovered += p.distance(approxPts.get(approxPts.size() - 1));
				approxDists.add(distCovered);
			}
			approxPts.add(p);
		}
	}

	protected static Color CONVEX = new Color(255, 127, 127);
	protected static Color CONCAVE = new Color(127, 127, 255);

	public void draw(Graphics2D g) {
		if (convexCurve != null) {
			g.setColor(CONVEX);
			g.fill(convexCurve);
		}
		if (concaveCurve != null) {
			g.setColor(CONCAVE);
			g.fill(concaveCurve);
		}
		g.setColor(Color.BLACK);
		g.draw(this);
//		g.setColor(Color.red);
//		for (double t = 0.25; t <= 1; t += 0.25) {
//			Point2D p1 = eval(t - 0.25);
//			Point2D p2 = eval(t);
//			g.draw(new Line2D.Double(p1, p2));
//		}
//		g.setColor(Color.BLUE);
//		g.draw(new Line2D.Double(x1, y1, ctrlx1, ctrly1));
//		g.draw(new Line2D.Double(ctrlx1, ctrly1, ctrlx2, ctrly2));
//		g.draw(new Line2D.Double(ctrlx2, ctrly2, x2, y2));
	}

	public double[] getTangentPoints(double x, double y) {
		ArrayList<Point2D> foundPoints = new ArrayList<Point2D>();

		// Use flattening approximation
		for (int i = 1; i < approxPts.size() - 1; i++) {
			if (testTangent(i, x, y)) {
				foundPoints.add(approxPts.get(i));
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

	private boolean testTangent(int i, double x, double y) {
		Point2D p = approxPts.get(i), p1 = approxPts.get(i - 1), p2 = approxPts.get(i + 1);
		double oPrev = Util.orientation(x, y, p.getX(), p.getY(), p1.getX(), p1.getY());
		double oNext = Util.orientation(x, y, p2.getX(), p2.getY(), p.getX(), p.getY());
		if (oPrev != oNext && (i == 1 || oPrev != 0)) {
			// Potential tangent
			if (ALLOW_INTERSECTING_TANS) {
				return true;
			} else {
				// Detect any crosses
				Point2D p3 = approxPts.get(0);
				boolean allow = true;
				for (int j = 0; allow && j < i; j++) {
					Point2D p4 = approxPts.get(j);
					if (Util.orientation(x, y, p.getX(), p.getY(), p3.getX(), p3.getY()) != Util.orientation(x, y,
							p.getX(), p.getY(), p4.getX(), p4.getY())
							&& Util.orientation(p3.getX(), p3.getY(), p4.getX(), p4.getY(), x, y) != Util
									.orientation(p3.getX(), p3.getY(), p4.getX(), p4.getY(), p.getX(), p.getY())) {
						allow = false;
					}
				}
				p3 = approxPts.get(approxPts.size() - 1);
				for (int j = i + 1; allow && j < approxPts.size(); j++) {
					Point2D p4 = approxPts.get(j);
					if (Util.orientation(x, y, p.getX(), p.getY(), p3.getX(), p3.getY()) != Util.orientation(x, y,
							p.getX(), p.getY(), p4.getX(), p4.getY())
							&& Util.orientation(p3.getX(), p3.getY(), p4.getX(), p4.getY(), x, y) != Util
									.orientation(p3.getX(), p3.getY(), p4.getX(), p4.getY(), p.getX(), p.getY())) {
						allow = false;
					}
				}
				if (allow) {
					return true;
				}
			}
		}
		return false;
	}

	// Split curve at point in time
	public void splitCurve(double t, CubicCurve2D prev, CubicCurve2D next) {
		if (prev != null)
			splitCurve(t, x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2, prev, false);
		if (next != null)
			splitCurve(1 - t, x2, y2, ctrlx2, ctrly2, ctrlx1, ctrly1, x1, y1, next, true);
	}

	private void splitCurve(double t, double x1, double y1, double x2, double y2, double x3, double y3, double x4,
			double y4, CubicCurve2D curve, boolean flip) {
		double x12 = (x2 - x1) * t + x1;
		double y12 = (y2 - y1) * t + y1;
		double x23 = (x3 - x2) * t + x2;
		double y23 = (y3 - y2) * t + y2;
		double x34 = (x4 - x3) * t + x3;
		double y34 = (y4 - y3) * t + y3;

		double x123 = (x23 - x12) * t + x12;
		double y123 = (y23 - y12) * t + y12;
		double x234 = (x34 - x23) * t + x23;
		double y234 = (y34 - y23) * t + y23;

		double x1234 = (x234 - x123) * t + x123;
		double y1234 = (y234 - y123) * t + y123;

		if (flip)
			curve.setCurve(x1234, y1234, x123, y123, x12, y12, x1, y1);
		else
			curve.setCurve(x1, y1, x12, y12, x123, y123, x1234, y1234);
	}

	// Do through rough approximation
	@Override
	public double[] getTangentLines(DrawableCurve other) {
		ArrayList<Point2D> foundPoints = new ArrayList<Point2D>();
		ArrayList<Point2D> otherPoints = new ArrayList<Point2D>();

		// Brute force each point
		for (int i = 1; i < approxPts.size() - 1; i++) {
			Point2D p = approxPts.get(i);
			double[] tans = other.getTangentPoints(p.getX(), p.getY());
			// Test tangent back
			for (int j = 0; j < tans.length; j += 2) {
				if (testTangent(i, tans[j], tans[j + 1])) {
					foundPoints.add(p);
					otherPoints.add(new Point2D.Double(tans[j], tans[j + 1]));
				}

			}
		}

		// Combine into an array of lines
		double[] allLines = new double[foundPoints.size() * 4];
		for (int i = 0; i < foundPoints.size(); i++) {
			Point2D p = foundPoints.get(i);
			Point2D p2 = otherPoints.get(i);
			allLines[4 * i] = p.getX();
			allLines[4 * i + 1] = p.getY();
			allLines[4 * i + 2] = p2.getX();
			allLines[4 * i + 3] = p2.getY();
		}
		return allLines;
	}

	public double[] getTangentTimes(double x, double y) {
		ArrayList<java.lang.Double> foundPoints = new ArrayList<java.lang.Double>();

		// Use flattening approximation
		for (int i = 1; i < approxPts.size() - 1; i++) {
			if (testTangent(i, x, y)) {
				foundPoints.add(approxTimes.get(i));
			}
		}
		// Convert to array of times
		double[] times = new double[foundPoints.size()];
		for (int i = 0; i < foundPoints.size(); i++) {
			times[i] = foundPoints.get(i);
		}
		return times;
	}

	// Approximate times of tangent
	public double[][] getTangentTimes(DrawableCurve other) {
		ArrayList<double[]> foundPairs = new ArrayList<double[]>();

		// Brute force each point
		for (int i = 1; i < approxPts.size() - 1; i++) {
			Point2D p = approxPts.get(i);
			double[] tans = other.getTangentPoints(p.getX(), p.getY());
			// Test tangent back
			for (int j = 0; j < tans.length; j += 2) {
				if (testTangent(i, tans[j], tans[j + 1])) {
					foundPairs.add(new double[] { p.getX(), p.getY(), tans[j], tans[j + 1] });
				}

			}
		}

		// Combine into an array of times
		double[][] pairs = new double[foundPairs.size()][];
		for (int i = 0; i < pairs.length; i++) {
			pairs[i] = foundPairs.get(i);

		}
		return pairs;
	}

	public Point2D eval(double t) {
		double mt = 1 - t;

		return new Point2D.Double(
				mt * mt * mt * x1 + 3.0 * t * mt * mt * ctrlx1 + 3.0 * t * t * mt * ctrlx2 + t * t * t * x2,
				mt * mt * mt * y1 + 3.0 * t * mt * mt * ctrly1 + 3.0 * t * t * mt * ctrly2 + t * t * t * y2);
	}
}
