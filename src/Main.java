import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Main extends JPanel {

	public static final int SIZE = 512;

	public static void main(String[] args) {

		JFrame app = new JFrame();
		app.setTitle("My app");
		app.setSize(SIZE, SIZE);
		app.setResizable(false);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		app.add(new Main());

		// Center on screen
		app.setLocationRelativeTo(null);
		app.setVisible(true);
	}

	private Quad curve;

	private int mx, my;

	private Main() {
		generateCurve();
		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				generateCurve();
				repaint();
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
				mouseInput(e.getX(), e.getY());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				mouseInput(e.getX(), e.getY());
			}
		});
	}

	private void mouseInput(int x, int y) {
		mx = x;
		my = y;
		repaint();
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, SIZE, SIZE);

		// Curve
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(3));
		g.draw(curve);

		// Mouse
		g.drawLine(mx, my, mx, my);

		// Tangents
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(1));
		double[] points = curve.getTangentPoints(mx, my);
		for (int i = 0; i < points.length; i += 2) {
			g.drawLine(mx, my, (int) Math.round(points[i]), (int) Math.round(points[i + 1]));
		}

	}

	private static final int BOR = 32;

	private void generateCurve() {
		Random r = new Random();
		curve = new Quad(BOR + r.nextInt(SIZE - 2 * BOR), BOR + r.nextInt(SIZE - 2 * BOR),
				BOR + r.nextInt(SIZE - 2 * BOR), BOR + r.nextInt(SIZE - 2 * BOR), BOR + r.nextInt(SIZE - 2 * BOR),
				BOR + r.nextInt(SIZE - 2 * BOR));
	}

}
