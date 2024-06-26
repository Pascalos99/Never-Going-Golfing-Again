package com.mygdx.game.courses;

import com.mygdx.game.parser.Function2d;

@Deprecated
public class MapGenUtils {
	
	static class FractalGenerationSettings {
		public final int desired_size, smoothing_factor;
		public final double roughness_height, roughness_friction, hole_tolerance, maximum_velocity, gravity;
		public FractalGenerationSettings(int desired_size, int smoothing_factor, double roughness_height, double roughness_friction, double hole_tolerance, double maximum_velocity, double gravity) {
			this.desired_size = desired_size;
			this.smoothing_factor = smoothing_factor;
			this.roughness_height = roughness_height;
			this.roughness_friction = roughness_friction;
			this.hole_tolerance = hole_tolerance;
			this.maximum_velocity = maximum_velocity;
			this.gravity = gravity;
		}
	}

	public static final double m = 100d;
	
	public static boolean gradient_display = false;
	

	
	public static double[][] fill(int width, int height, double filler) {
		double[][] result = new double[width][height];
		for (int i=0; i < result.length; i++)
			for (int j=0; j < result[i].length; j++) result[i][j] = filler;
		return result;
	}
	
	public static double[][] fill(int width, int height, Function2d function) {
		double[][] result = new double[width][height];
		for (int i=0; i < result.length; i++)
			for (int j=0; j < result[i].length; j++) result[i][j] = function.evaluate((double)i, (double)j);
		return result;
	}
	
	public static int floor(double x) {
		return (int)x;
	}
	public static double modulus(double x) {
		return (x < 0)? -x : x;
	}
	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	}
	
	public static void brushPaint(int centre_x, int centre_y, double value, double radius, double[][] m) {
		int low_x = (int)(centre_x - radius); 	int high_x = (int)(centre_x + radius + 1);
		int low_y = (int)(centre_y - radius); 	int high_y = (int)(centre_y + radius + 1);
		if (low_x < 0) low_x = 0; 				if (high_x >= m.length) high_x = m.length - 1;
		if (low_y < 0) low_y = 0; 				if (high_y >= m[0].length) high_y = m[0].length - 1;
		for (int x = low_x; x < high_x; x++)
			for (int y = low_y; y < high_y; y++) {
				double distance = distance(centre_x, centre_y, x, y);
				if (distance > radius) continue;
				double ratio = (radius - distance)/radius;
				m[x][y] = ratio * value + (1 - ratio) * m[x][y];
			}
	}
	
//	@SuppressWarnings("serial")
//	public static JFrame displayCourse(PuttingCourse course, double course_width, double course_height, double stepsize) {
//		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
//		if (gradient_display) {
//			for (double i=0; i < course_width; i+=stepsize)
//				for (double j=0; j < course_height; j+=stepsize) {
//					if (course.getHeightAt(i, j) > max) max = course.getHeightAt(i, j);
//					if (course.getHeightAt(i, j) < min) min = course.getHeightAt(i, j);
//				}
//		}
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		int width = (int)(course_width / stepsize), height = (int)(course_height / stepsize);
//		int scale_down = 1, scale_up = 1;
//		if (width > screenSize.width || height > screenSize.height)
//			while (width / scale_down > screenSize.width || height / scale_down > screenSize.height) scale_down++;
//		else if (width*2 < screenSize.width || height*2 < screenSize.height)
//			while (width * scale_up * 2 < screenSize.width || height * scale_up * 2 < screenSize.height) scale_up++;
//		Color[][] paintmatrix = new Color[width * scale_up / scale_down][height * scale_up / scale_down];
//		if (scale_down > 1) {
//			for (int x=0; x < paintmatrix.length; x++)
//				for (int y=0; y < paintmatrix[x].length; y++) {
//					if (gradient_display) {
//						double value = (course.getHeightAt(x * scale_down, y * scale_down) - min)/(max-min);
//						paintmatrix[x][y] = new Color((int)(50 * value),(int)(255 * value),(int)(200 * (1-value)));
//					} else paintmatrix[x][y] = course.getMaterialAt(x * scale_down, y * scale_down).map_color;
//				}
//		} else {
//			for (int y=0; y < height; y++)
//				for (int x=0; x < width; x++) {
//					for (int i=0; i < scale_up; i++)
//						for (int j=0; j < scale_up; j++)
//							if (gradient_display) {
//								double value = (course.getHeightAt(x*stepsize, y*stepsize) - min)/(max - min);
//								paintmatrix[x * scale_up + i][y * scale_up + j] = new Color((int)(50 * value),(int)(255 * value),(int)(200 * (1-value)));
//							} else paintmatrix[x * scale_up + i][y * scale_up + j] = course.getMaterialAt(x*stepsize, y*stepsize).map_color;
//				}
//		}
//		final int scaleup = scale_up;
//		final int scaledown = scale_down;
//		JFrame frame = new JFrame("Display Course Map");
//		frame.setSize(paintmatrix[0].length, paintmatrix.length);
//		frame.add(new JComponent() {
//			@Override
//			public void paintComponent(Graphics g) {
//				for (int i=0; i < paintmatrix.length; i++)
//					for (int j=0; j < paintmatrix[i].length; j++) {
//						g.setColor(paintmatrix[i][j]);
//						g.fillRect(i, j, 1, 1);
//					}
//				float measure = 1f;
//				while (measure * 100 * scaleup / scaledown < 50) measure *= 10;
//				while (measure * 100 * scaleup / scaledown > 150) measure /= 10;
//				g.setColor(Color.BLACK);
//				g.fillRect(10, 10, (int)(measure * 100 * scaleup / scaledown), 5);
//				g.fillRect(10, 5, 5, 15);
//				g.fillRect((int)(10 + measure * 100 * scaleup / scaledown), 5, 5, 15);
//				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
//				g.drawString(measure+" meter", (int)(15 + measure * 30 * scaleup/ scaledown), 30);
//				int flag_x = (int) (course.flag_position.get_x() * scaleup / scaledown);
//				int flag_y = (int) (course.flag_position.get_y() * scaleup / scaledown);
//				int strt_x = (int) (course.start_position.get_x() * scaleup / scaledown);
//				int strt_y = (int) (course.start_position.get_y() * scaleup / scaledown);
//				BufferedImage flag = null, start = null;
//				try {
//				    flag = ImageIO.read(new File(FLAG_TEXTURE));
//				    start = ImageIO.read(new File(START_TEXTURE));
//				} catch (IOException e) {
//				}
//				//g.drawImage(flag, flag_x - 2*(flag.getWidth()*1)/5, flag_y - 2*(flag.getHeight()*5)/6, 32, 32, null);
//				//g.drawImage(start, strt_x - 2*start.getWidth()/2, strt_y - 2*start.getHeight()/2, 34, 34, null);
//			}
//		});
//		frame.setVisible(true);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setResizable(false);
//		return frame;
//	}
//
//	public static void generationTesterFrame(PuttingCourseGenerator gen, int desired_size, int smoothing_factor, double roughness_height, double roughness_friction, double hole_tolerance, double maximum_velocity, double gravity) {
//		JFrame frame = displayCourse(gen.fractalGeneratedCourse(desired_size, smoothing_factor, roughness_height, roughness_friction, hole_tolerance, maximum_velocity, gravity), BOUNDED_WORLD_SIZE, BOUNDED_WORLD_SIZE, 0.05);
//		frame.addKeyListener(new KeyAdapter() {
//			boolean pressed = false;
//			public void keyPressed(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_ENTER && !pressed) {
//					frame.setVisible(false);
//					generationTesterFrame(gen, desired_size, smoothing_factor, roughness_height, roughness_friction, hole_tolerance, maximum_velocity, gravity);
//					pressed = true; }}
//			public void keyReleased(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_ENTER) pressed = false; }
//		});
//	}
//	public void generationTesterFrame(PuttingCourseGenerator gen, MapGenUtils.FractalGenerationSettings settings) {
//		generationTesterFrame(gen, settings.desired_size, settings.smoothing_factor, settings.roughness_height, settings.roughness_friction, settings.hole_tolerance, settings.maximum_velocity, settings.gravity);
//	}
	
	static int approximate_required_detail(int size) {
		int r = 0, t = size;
		while (t > 0) {
			r++;
			t >>= 1; }
		if (r==0) return 1;
		int lower = 1 << (r - 1);
		int higher = 1 << r;
		if (size - lower <= higher - size) return r - 1;
		return r;
	}

}
