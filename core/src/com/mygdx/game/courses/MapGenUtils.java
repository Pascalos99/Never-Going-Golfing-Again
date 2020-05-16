package com.mygdx.game.courses;

import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.parser.ArrayFunction2d;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.parser.FunctionalFunction2d;

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

import static com.mygdx.game.courses.Material.*;
import static com.mygdx.game.utils.Variables.*;

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
	
	public static void testFunction(Function2d f, double x1, double y1, double x2, double y2, double interval_x, double interval_y) {
		double y = y1, x = x1;
		for (;y <= y2; y += interval_y) {
			if (y==y1) {
				String str = " y  x / ";
				for (; x <= x2; x += interval_x) {
					str += String.format("% .2f",x);
					if (x <= x2-interval_x) str += " .";
				} System.out.println(str);
				String line = "";
				while (line.length() < str.length() + 2) line += "-";
				System.out.println(line);
			} x = x1;
			for (;x <= x2; x += interval_x) {
				if (x==x1) System.out.format("% .2f [ ", y);
				System.out.format("% .2f",f.evaluate(x, y));
				if (x <= x2-interval_x) System.out.print(" |");
			} System.out.println(" ]");
		}
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
			for (int i=0; i < course.course_width; i++)
				for (int j=0; j < course.course_height; j++) {
					if (course.getHeightAt(i, j) > max) max = course.getHeightAt(i, j);
					if (course.getHeightAt(i, j) < min) min = course.getHeightAt(i, j);
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
						double value = (course.getHeightAt(x * scale_down, y * scale_down) - min)/(max-min);
						paintmatrix[x][y] = new Color((int)(50 * value),(int)(255 * value),(int)(200 * (1-value)));
					} else paintmatrix[x][y] = course.getMaterialAt(x * scale_down, y * scale_down).map_color;
				}
		} else {
			for (int y=0; y < course.courseHeight(); y++)
				for (int x=0; x < course.courseWidth(); x++) {
					for (int i=0; i < scale_up; i++)
						for (int j=0; j < scale_up; j++)
							if (gradient_display) {
								double value = (course.getHeightAt(x, y) - min)/(max - min);
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
				//g.drawImage(flag, flag_x - 2*(flag.getWidth()*1)/5, flag_y - 2*(flag.getHeight()*5)/6, 32, 32, null);
				//g.drawImage(start, strt_x - 2*start.getWidth()/2, strt_y - 2*start.getHeight()/2, 34, 34, null);
			}
		});
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		return frame;
	}
	
	public static void generationTesterFrame(PuttingCourseGenerator gen, int desired_size, int smoothing_factor, double roughness_height, double roughness_friction, double hole_tolerance, double maximum_velocity, double gravity) {
		JFrame frame = displayCourse(gen.fractalGeneratedCourse(desired_size, smoothing_factor, roughness_height, roughness_friction, hole_tolerance, maximum_velocity, gravity));
		frame.addKeyListener(new KeyAdapter() {
			boolean pressed = false;
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !pressed) {
					frame.setVisible(false);
					generationTesterFrame(gen, desired_size, smoothing_factor, roughness_height, roughness_friction, hole_tolerance, maximum_velocity, gravity);
					pressed = true; }}
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) pressed = false; }
		});
	}
	public void generationTesterFrame(PuttingCourseGenerator gen, MapGenUtils.FractalGenerationSettings settings) {
		generationTesterFrame(gen, settings.desired_size, settings.smoothing_factor, settings.roughness_height, settings.roughness_friction, settings.hole_tolerance, settings.maximum_velocity, settings.gravity);
	}
	
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

	public static ArrayFunction2d functionFromArray(double[][] m, Function2d out_of_bounds_value) {
		double[][] m2 = new double[m.length + 2][m[0].length + 2];
		for (int i=0; i < m2.length; i++)
			for (int j=0; j < m2[i].length; j++)
				if (i==0 || j==0 || i == m2.length-1 || j == m2.length-1) m2[i][j] = out_of_bounds_value.evaluate(i-1, j-1);
				else m2[i][j] = m[i-1][j-1];
		
		return new ArrayFunction2d() {
			double[][] original = m;
			double[][] array = m2;
			Function2d function = out_of_bounds_value;
			@Override
			public Vector2d gradient(double x, double y) {
				if (x >= array.length - 2|| y >= array.length - 2 || x < -1 || y < -1) return function.gradient(x, y);
				x++; y++;
				double p = floor(x), q= floor(x+1);
				double r = floor(y), s = floor(y+1);
				double T = array[floor(x)][floor(y)];
				double U = array[floor(x)][floor(y+1)]; 
				double V = array[floor(x+1)][floor(y)];
				double W = array[floor(x+1)][floor(y+1)];
				return new Vector2d(
						(r - y) * (U - W) + (s - y) * (V - T),
						p * (V - W) + q * (U - T) + x * (T - U - V + W)
					); // I trust WolframAlpha on this one...
			}
			@Override
			public double evaluate(double x, double y) {
				if (x >= array.length - 2|| y >= array.length - 2 || x < -1 || y < -1) return function.evaluate(x, y);
				x++; y++;
				double x1 = floor(x), x2 = floor(x+1);
				double y1 = floor(y), y2 = floor(y+1);
				
				double Q11 = array[floor(x)][floor(y)];
				double Q12 = array[floor(x)][floor(y+1)]; 
				double Q21 = array[floor(x+1)][floor(y)];
				double Q22 = array[floor(x+1)][floor(y+1)];
				
				double fx1 = (x2 - x) * Q11 + (x - x1) * Q21;
				double fx2 = (x2 - x) * Q12 + (x - x1) * Q22;
				return (y2 - y) * fx1 + (y - y1) * fx2;
			}
			@Override
			public double[][] getArray() {
				return original;
			}
		};
	}

	public static int evaluateMaterial(double x, double y, Function2d height, Function2d friction, Vector2d flag, Vector2d start, double hole_tolerance) {
		double z_value = height.evaluate(x, y);
		double f_value = friction.evaluate(x, y);
		if (distance(flag.get_x(), flag.get_y(), x, y) <= hole_tolerance) return FLAG.index;
		if (distance(start.get_x(), start.get_y(), x, y) <= 1) return STARTING_POINT.index;
		if (z_value < 0) return WATER.index;
		if (f_value <= ICE_FRICTION) return ICE.index;
		if (f_value >= SAND_FRICTION) return SAND.index;
		if (z_value >= MOUNTAIN_HEIGHT) return MOUNTAIN.index;
		if (z_value >= HILL_HEIGHT) return HILL.index;
		return GRASS.index;
	}

	public static void main(String[] args) {
		boolean use_function = false;
		boolean tester_frame = !use_function;
		gradient_display	 = false;
		long seed = System.currentTimeMillis();
		FunctionalFunction2d function = (x, y) -> {return Math.sin(x) + Math.sin(y);};
		PuttingCourseGenerator gen = new PuttingCourseGenerator(seed);
		gen.setPathPreference(true);
		PuttingCourse test;
		if (!use_function) test = gen.fractalGeneratedCourse(50, 1, 0.4, 0.6, 10, 50, 9.812);
		else test = gen.functionGeneratedCourse(function, Function2d.getConstant(0.134), 50, 50, 1, 50, 9.812);
		System.out.println("course is "+test.courseWidth()+"x"+test.courseHeight()+", generated with seed "+seed);
		if (tester_frame) generationTesterFrame(gen, 50, 1, 0.4, 0.6, 1, 50, 9.812);
		else displayCourse(test);
	}
}
