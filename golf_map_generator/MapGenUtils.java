package golf_map_generator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

import golf_map_generator.PuttingCourseGenerator.FractalGenerationSettings;
import main.Function2d;
import main.FunctionalFunction2d;

import static golf_map_generator.Variables.FLAG_TEXTURE;
import static golf_map_generator.Variables.START_TEXTURE;

public class MapGenUtils {
	
	public static final double m = 100d;
	
	public static boolean gradient_display = false;
	
	/** Approaches the scaling of the matrix m by the given factor (a 2x2 * 2 gives a 3x3, a 100x100 * 2 gives a 199x199) */
	public static double[][] enlargeMatrix(double[][] m, int factor) {
		factor = factor - 1;
		int smallW = m.length, smallH = m[0].length;
		int bigW = smallW + (smallW-1)*factor;
		int bigH = smallH + (smallH-1)*factor;
		double[][] result = new double[bigW][bigH];
		int l = factor + 1;
		for (int i=0; i < smallW; i++) {
		    for (int j=0; j < smallH; j++) {
		      result[i*l][j*l] = m[i][j];
		      if (i < smallW-1) {
		        for (int k=1; k < l; k++) {
		          result[i*l+k][j*l] = ((l-k)*m[i][j] + k*m[i+1][j])/l;}}
		      if (j < smallH-1) {
		        for (int k=1; k < l; k++) {
		          result[i*l][j*l+k] = ((l-k)*m[i][j] + k*m[i][j+1])/l;}}}}
		  for (int i=0; i < smallW; i++) {
		    for (int j=0; j < smallH; j++) {
		      if (i < smallW-1 && j < smallH-1) {
		        for (int k=1; k < l; k++) {
		          for (int p=1; p < l; p++) {
		            result[i*l+k][j*l+p] = ((l-p)*result[i*l+k][j*l] + p*result[i*l+k][j*l+l] + (l-k)*result[i*l][j*l+p] + k*result[i*l+l][j*l+p])/(2*l);}}}
		  }} return result;
	}
	
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
	
	/** designed for matrices that only contain numbers between 0 and 1*/
	public static void applyRangeToMatrix(double[][] m, Range range) {
		for (int i=0; i < m.length; i++)
			for (int j=0; j < m[i].length; j++) m[i][j] = range.min + m[i][j]*(range.max - range.min);
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
	
	@SuppressWarnings("serial")
	public static JFrame displayCourse(PuttingCourse course) {
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		if (gradient_display) {
			for (int i=0; i < course.height_map.length; i++)
				for (int j=0; j < course.height_map[i].length; j++) {
					if (course.height_map[i][j] > max) max = course.height_map[i][j];
					if (course.height_map[i][j] < min) min = course.height_map[i][j];
				}
		}
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = course.courseWidth(), height = course.courseHeight();
		int scale_down = 1, scale_up = 1;
		if (width > screenSize.width || height > screenSize.height)
			while (width / scale_down > screenSize.width || height / scale_down > screenSize.height) scale_down++;
		else if (width*2 < screenSize.width || height*2 < screenSize.height)
			while (width * scale_up * 2 < screenSize.width || height * scale_up * 2 < screenSize.height) scale_up++;
		Color[][] paintmatrix = new Color[width * scale_up / scale_down][height * scale_up / scale_down];
		if (scale_down > 1) {
			for (int x=0; x < paintmatrix.length; x++)
				for (int y=0; y < paintmatrix[x].length; y++) {
					if (gradient_display) {
						double value = (course.height_map[x * scale_down][y * scale_down] - min)/(max-min);
						paintmatrix[x][y] = new Color((int)(50 * value),(int)(255 * value),(int)(200 * (1-value)));
					} else paintmatrix[x][y] = course.getMaterialAt(x * scale_down, y * scale_down).map_color;
				}
		} else {
			for (int y=0; y < course.courseHeight(); y++)
				for (int x=0; x < course.courseWidth(); x++) {
					for (int i=0; i < scale_up; i++)
						for (int j=0; j < scale_up; j++)
							if (gradient_display) {
								double value = (course.height_map[x][y] - min)/(max - min);
								paintmatrix[x * scale_up + i][y * scale_up + j] = new Color((int)(50 * value),(int)(255 * value),(int)(200 * (1-value)));
							} else paintmatrix[x * scale_up + i][y * scale_up + j] = course.getMaterialAt(x, y).map_color;
				}
		}
		final int scaleup = scale_up;
		final int scaledown = scale_down;
		JFrame frame = new JFrame("Display Course Map");
		frame.setSize(paintmatrix[0].length, paintmatrix.length);
		frame.add(new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				for (int i=0; i < paintmatrix.length; i++)
					for (int j=0; j < paintmatrix[i].length; j++) {
						g.setColor(paintmatrix[i][j]);
						g.fillRect(i, j, 1, 1);
					}
				float measure = 1f;
				while (measure * 100 * scaleup / scaledown < 50) measure *= 10;
				while (measure * 100 * scaleup / scaledown > 150) measure /= 10;
				g.setColor(Color.BLACK);
				g.fillRect(10, 10, (int)(measure * 100 * scaleup / scaledown), 5);
				g.fillRect(10, 5, 5, 15);
				g.fillRect((int)(10 + measure * 100 * scaleup / scaledown), 5, 5, 15);
				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
				g.drawString(measure+" meter", (int)(15 + measure * 30 * scaleup/ scaledown), 30);
				int flag_x = (int) (course.flag_position.get_x() * scaleup / scaledown);
				int flag_y = (int) (course.flag_position.get_y() * scaleup / scaledown);
				int strt_x = (int) (course.start_position.get_x() * scaleup / scaledown);
				int strt_y = (int) (course.start_position.get_y() * scaleup / scaledown);
				BufferedImage flag = null, start = null;
				try {
				    flag = ImageIO.read(new File(FLAG_TEXTURE));
				    start = ImageIO.read(new File(START_TEXTURE));
				} catch (IOException e) {
				}
				g.drawImage(flag, flag_x - (flag.getWidth()*1)/5, flag_y - (flag.getHeight()*5)/6, null);
				g.drawImage(start, strt_x - start.getWidth()/2, strt_y - start.getHeight()/2, null);
			}
		});
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		return frame;
	}
	
	public static void generationTesterFrame(PuttingCourseGenerator gen, int desired_size, int smoothing_factor, double roughness_height, double roughness_friction, double hole_tolerance, double maximum_velocity) {
		JFrame frame = displayCourse(gen.fractalGeneratedCourse(desired_size, smoothing_factor, roughness_height, roughness_friction, hole_tolerance, maximum_velocity));
		frame.addKeyListener(new KeyAdapter() {
			boolean pressed = false;
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !pressed) {
					frame.setVisible(false);
					generationTesterFrame(gen, desired_size, smoothing_factor, roughness_height, roughness_friction, hole_tolerance, maximum_velocity);
					pressed = true; }}
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) pressed = false; }
		});
	}
	public void generationTesterFrame(PuttingCourseGenerator gen, FractalGenerationSettings settings) {
		generationTesterFrame(gen, settings.desired_size, settings.smoothing_factor, settings.roughness_height, settings.roughness_friction, settings.hole_tolerance, settings.maximum_velocity);
	}
	
	public static void main(String[] args) {
		boolean use_function = false;
		boolean tester_frame = true;
		gradient_display	 = false;
		long seed = System.currentTimeMillis();
		FunctionalFunction2d function = (x, y) -> {return 1000*Math.sin(x) + 1000*Math.sin(y);};
		PuttingCourseGenerator gen = new PuttingCourseGenerator(seed);
		gen.setPathPreference(true);
		PuttingCourse test;
		if (!use_function) test = gen.fractalGeneratedCourse(2000, 50, 0.4, 0.6, 10, 50);
		else test = gen.functionGeneratedCourse(function, Function2d.getConstant(0.134), 2000, 2000, 10, 50);
		System.out.println("course is "+test.courseWidth()+"x"+test.courseHeight()+", generated with seed "+seed);
		if (tester_frame) generationTesterFrame(gen, 2000, 50, 0.4, 0.6, 10, 50);
		else displayCourse(test);
	}
	
}
