import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Quad extends QuadCurve2D.Double implements DrawableCurve {

	private final boolean isConvex, isConcave;
	private static final double SPLIT_COUNT = 16;

	// For segment on segment estimation
	private final ArrayList<Point2D> approxPts;
	private final ArrayList<java.lang.Double> approxDists, approxTimes;

	public Quad(double x0, double y0, double x1, double y1, double x2, double y2) {
		super(x0, y0, x1, y1, x2, y2);
		isConvex = Util.orientation(x0, y0, x1, y1, x2, y2) > 0;
		isConcave = Util.orientation(x0, y0, x1, y1, x2, y2) < 0;

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

	public double[] getTangentPoints(double x, double y) {
		final double a = x1, b = ctrlx, c = x2;
		final double j = y1, k = ctrly, l = y2;

		// Calculate divisor
		double div = 2 * (a * k - a * l - b * j + b * l + c * j - c * k);
		if (div == 0) {
			return new double[] {};
		}

		double base = 2 * a * k - a * l - a * y - 2 * b * j + 2 * b * y + c * j - c * y + j * x - 2 * k * x + l * x;
		double rt = Math
				.pow(2 * a * k - a * l - a * y - 2 * b * j + 2 * b * y + c * j - c * y + j * x - 2 * k * x + l * x, 2);
		rt += 4 * (a * k - a * l - b * j + b * l + c * j - c * k) * (-a * k + a * y + b * j - b * y - j * x + k * x);
		if (rt < 0) {
			return new double[] {};
		}

		double t0 = (base - Math.sqrt(rt)) / div;
		double t1 = (base + Math.sqrt(rt)) / div;

		Point2D p0 = (t0 >= 0 && t0 <= 1) ? eval(t0) : null;
		Point2D p1 = (t1 >= 0 && t1 <= 1) ? eval(t1) : null;

		if (p0 != null && p1 != null) {
			return new double[] { p0.getX(), p0.getY(), p1.getX(), p1.getY() };
		} else if (p0 != null) {
			return new double[] { p0.getX(), p0.getY() };
		} else if (p1 != null) {
			return new double[] { p1.getX(), p1.getY() };
		} else {
			return new double[] {};
		}
	}

	public void draw(Graphics2D g) {
		if (isConvex) {
			g.setColor(Cubic.CONVEX);
			g.fill(this);
		}
		if (isConcave) {
			g.setColor(Cubic.CONCAVE);
			g.fill(this);
		}
		g.setColor(Color.BLACK);
		g.draw(this);
	}

	private boolean testTangent(int i, double x, double y) {
		Point2D p = approxPts.get(i), p1 = approxPts.get(i - 1), p2 = approxPts.get(i + 1);
		double oPrev = Util.orientation(x, y, p.getX(), p.getY(), p1.getX(), p1.getY());
		double oNext = Util.orientation(x, y, p2.getX(), p2.getY(), p.getX(), p.getY());
		if (oPrev != oNext && (i == 1 || oPrev != 0)) {
			// Potential tangent
			if (Cubic.ALLOW_INTERSECTING_TANS) {
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
	public void splitCurve(double t, QuadCurve2D prev, QuadCurve2D next) {
		if (prev != null)
			splitCurve(t, x1, y1, ctrlx, ctrly, x2, y2, prev, false);
		if (next != null)
			splitCurve(1 - t, x2, y2, ctrlx, ctrly, x1, y1, next, true);
	}

	private void splitCurve(double t, double x1, double y1, double x2, double y2, double x3, double y3,
			QuadCurve2D curve, boolean flip) {
		double x12 = (x2 - x1) * t + x1;
		double y12 = (y2 - y1) * t + y1;
		double x23 = (x3 - x2) * t + x2;
		double y23 = (y3 - y2) * t + y2;

		double x123 = (x23 - x12) * t + x12;
		double y123 = (y23 - y12) * t + y12;

		if (flip)
			curve.setCurve(x123, y123, x12, y12, x1, y1);
		else
			curve.setCurve(x1, y1, x12, y12, x123, y123);
	}

	// Do through rough approximation
	@Override
	public double[] getTangentLines(DrawableCurve other) {
		if (other instanceof Cubic) {
			// This method is faster, because it uses quad getTangentPoints.
			return other.getTangentLines(this);
		} else if (other instanceof Quad) {
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
		} else {
			throw new RuntimeException();
		}
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
		if (other instanceof Cubic) {
			// This method is faster, because it uses quad getTangentPoints.
			double[][] ret = other.getTangentTimes(this);
			for (int i = 0; i < ret.length; i++) {
				double tmp = ret[i][0];
				ret[i][0] = ret[i][1];
				ret[i][1] = tmp;
			}
			return ret;
		} else if (other instanceof Quad) {
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
		} else {
			throw new RuntimeException();
		}
	}

	public Point2D eval(double t) {
		return new Point2D.Double((1 - t) * (1 - t) * x1 + 2 * t * (1 - t) * ctrlx + t * t * x2,
				(1 - t) * (1 - t) * y1 + 2 * t * (1 - t) * ctrly + t * t * y2);
	}
}
