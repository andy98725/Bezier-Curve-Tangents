import java.awt.*;

public class FPSTracker {

	private long startTime;
	private final long[] FPSTimes;

	public FPSTracker(int avgCount) {
		FPSTimes = new long[avgCount];
	}

	public void startTiming() {
		startTime = System.nanoTime();
	}

	public void stopTiming() {
		long time = System.nanoTime() - startTime;
		for (int i = FPSTimes.length - 1; i > 0; i--) {
			FPSTimes[i] = FPSTimes[i - 1];
		}
		FPSTimes[0] = time;
	}
	
	private static final Font FNT = new Font("Helvetica", Font.PLAIN, 12);

	public void drawFPS(Graphics2D g) {
		// Only do once fully initialized
		if (FPSTimes[FPSTimes.length - 1] == 0)
			return;

		long avgTime = 0;
		for (long time : FPSTimes) {
			avgTime += time;
		}
		avgTime /= FPSTimes.length;
		double fps = 1E9 / avgTime;

		g.setColor(Color.BLACK);
		g.setFont(FNT);
		g.drawString("FPS: " + (int)fps, 0, 16);

	}

}
