package golf_map_generator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;

import main.Function2d;

public class MapGenUtils {
	
	public static final double m = 100d;
	
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
	
	@SuppressWarnings("serial")
	public static void displayCourse(PuttingCourse course) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = course.courseWidth(), height = course.courseHeight();
		int scale_down = 1, scale_up = 1;
		if (width > screenSize.width || height > screenSize.height)
			while (width / scale_down > screenSize.width || height / scale_down > screenSize.height) scale_down++;
		else if (width*2 < screenSize.width || height*2 < screenSize.height)
			while (width * scale_up * 2 < screenSize.width || height * scale_up * 2 < screenSize.height) scale_up++;
		Color[][] paintmatrix = new Color[width * scale_up / scale_down][height * scale_up / scale_down];
		if (scale_down > 1) {
			for (int y=0; y < paintmatrix.length; y++)
				for (int x=0; x < paintmatrix[y].length; x++) {
					paintmatrix[y][x] = course.getMaterialAt(x * scale_down, y * scale_down).map_color;
				}
		} else {
			for (int y=0; y < course.courseHeight(); y++)
				for (int x=0; x < course.courseWidth(); x++) {
					for (int i=0; i < scale_up; i++)
						for (int j=0; j < scale_up; j++)
							paintmatrix[y * scale_up + i][x * scale_up + j] = course.getMaterialAt(x, y).map_color;
				}
		}
		final int scaleup = scale_up;
		final int scaledown = scale_down;
		JFrame frame = new JFrame("Display Course Map");
		frame.setSize(paintmatrix[0].length, paintmatrix.length);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
			}
		});
		frame.repaint();
	}
	
	public static void main(String[] args) {
		long seed = System.currentTimeMillis();
		PuttingCourseGenerator gen = new PuttingCourseGenerator(seed, new Range(-10*m, 30*m), new Range(-1, 6));
		PuttingCourse test = gen.fractalGeneratedCourse(2000, 50, 0.4, 0.7, 10, 50);
		//PuttingCourse test = gen.functionGeneratedCourse(height, friction, course_width_cm, course_height_cm, hole_tolerance, maximum_velocity);
		System.out.println("course is "+test.courseWidth()+"x"+test.courseHeight()+", generated with seed "+seed);
		displayCourse(test);
	}
	
}
