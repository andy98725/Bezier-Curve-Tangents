import java.awt.Graphics2D;

public interface DrawableCurve {

	public void draw(Graphics2D g);

	public double[] getTangentPoints(double x, double y);

	public double[] getTangentLines(DrawableCurve other);
	
	public double[][] getTangentTimes(DrawableCurve other);
}
