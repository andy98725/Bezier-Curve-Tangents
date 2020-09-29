
public class Util {

	public static double orientation(double x0, double y0, double x1, double y1, double x2, double y2) {
		return Math.signum((y1 - y0)*(x2 - x1) - (y2 - y1)*(x1 - x0));
	}
}
