import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Main extends JPanel {

	public static final int SIZE = 768;

	public static void main(String[] args) {

		JFrame app = new JFrame();
		app.setTitle("Bezier Curves");
		app.setSize(SIZE, SIZE);
		app.setResizable(false);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		app.add(new Main());

		// Center on screen
		app.setLocationRelativeTo(null);
		app.setVisible(true);
	}

	private DrawableCurve curve;
	private final ArrayList<Point2D> points = new ArrayList<Point2D>();

	private int mx, my;

	private Main() {
		generateCurve();
		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)
					placePoint(e.getX(), e.getY());
				else if (e.getButton() == MouseEvent.BUTTON3)
					clearPoints();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)
				updatePoint(e.getX(), e.getY());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				mouseInput(e.getX(), e.getY());
			}
		});
		setFocusable(true);
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				keyInput(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	private void mouseInput(int x, int y) {
		mx = x;
		my = y;
		repaint();
	}

	private void keyInput(int keyC) {
		if (keyC == KeyEvent.VK_SPACE) {
			clearPoints();
		}
	}

	private void clearPoints() {
		points.clear();
		generateCurve();
	}

	private void placePoint(int x, int y) {
		if (points.size() < 4) {
			points.add(new Point2D.Double(x, y));
			generateCurve();
		}
	}

	private void updatePoint(int x, int y) {
		if (points.size() > 0)
			points.remove(points.size() - 1);

		placePoint(x, y);
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		g.setStroke(new BasicStroke(3));
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, SIZE, SIZE);

		// Points
		g.setColor(Color.GRAY);
		for (Point2D p : points) {
			int x = (int) Math.round(p.getX()), y = (int) Math.round(p.getY());
			g.drawLine(x, y, x, y);
		}

		// Curve
		if (curve != null) {
			curve.draw(g);
		}

		// Mouse
		g.setColor(Color.BLACK);
		g.drawLine(mx, my, mx, my);

		// Tangents
		if (curve != null) {
			g.setColor(Color.BLUE);
			g.setStroke(new BasicStroke(1));
			double[] points = curve.getTangentPoints(mx, my);
			for (int i = 0; i < points.length; i += 2) {
				g.drawLine(mx, my, (int) Math.round(points[i]), (int) Math.round(points[i + 1]));
			}
		}

	}

	private void generateCurve() {
		if (points.size() == 3) {
			Point2D p0 = points.get(0), p1 = points.get(1), p2 = points.get(2);
			curve = new Quad(p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY());
		} else if (points.size() >= 4) {
			Point2D p0 = points.get(0), p1 = points.get(1), p2 = points.get(2), p3 = points.get(3);
			curve = new Cubic(p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
		} else {
			curve = null;
		}
		repaint();
	}

}
