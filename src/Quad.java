import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;

@SuppressWarnings("serial")
public class Quad extends QuadCurve2D.Double implements DrawableCurve {

	private final boolean isConvex, isConcave;

	public Quad(double x0, double y0, double x1, double y1, double x2, double y2) {
		super(x0, y0, x1, y1, x2, y2);
		isConvex = Util.orientation(x0, y0, x1, y1, x2, y2) > 0;
		isConcave = Util.orientation(x0, y0, x1, y1, x2, y2) < 0;

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

		double[] p0 = (t0 >= 0 && t0 <= 1) ? eval(t0) : new double[] {};
		double[] p1 = (t1 >= 0 && t1 <= 1) ? eval(t1) : new double[] {};

		double[] ret = new double[p0.length + p1.length];
		for (int i = 0; i < p0.length; i++) {
			ret[i] = p0[i];
		}
		for (int i = 0; i < p1.length; i++) {
			ret[p0.length + i] = p1[i];
		}

		return ret;
	}

	public void draw(Graphics2D g) {
		if (isConvex) {
			g.setColor(new Color(255, 127, 127));
			g.fill(this);
		}
		if (isConcave) {
			g.setColor(new Color(127, 127, 255));
			g.fill(this);
		}
		g.setColor(Color.BLACK);
		g.draw(this);
	}

	private double[] eval(double t) {
		return new double[] { (1 - t) * (1 - t) * x1 + 2 * t * (1 - t) * ctrlx + t * t * x2,
				(1 - t) * (1 - t) * y1 + 2 * t * (1 - t) * ctrly + t * t * y2 };
	}
}
