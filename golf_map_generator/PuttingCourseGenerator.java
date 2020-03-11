package golf_map_generator;

import static golf_map_generator.Material.*;
import static golf_map_generator.Variables.*;

import java.util.Random;

import main.Vector2d;

public class PuttingCourseGenerator {
	
	Random random;
	
	public PuttingCourseGenerator(long seed) {
		random = new Random(seed);
	}

	/** This method will generate a material heat-map given a height and friction map.<br>
	 * It will therefor not be able to account for obstacles, as they cannot be gathered from this data.<br>
	 * Flag and start positions will also be inserted, given the hole_tolerance is > 0
	 * @return {@code null} if the heat-map sizes do not match up. */
	public static int[][] generate_materials(double[][] height, double[][] friction, Vector2d flag, Vector2d start, double hole_tolerance) {
		if (height.length != friction.length || height[0].length != friction[0].length) return null;
		int[][] result = new int[height.length][height[0].length];
		for (int i=0; i < height.length; i++) {
			for (int j=0; j < height[i].length; j++) {
				double z_value = height[i][j];
				double f_value = friction[i][j];
				if (flag.is_contained_in(i, j, hole_tolerance)) result[i][j] = FLAG.index;
				else if (start.is_contained_in(i, j, 0.01)) result[i][j] = STARTING_POINT.index; // TODO perfect when the is_contained_in method functions properly
				else if (z_value < 0) result[i][j] = WATER.index;
				else if (f_value <= 0) result[i][j] = ICE.index;
				else if (f_value >= SAND_FRICTION) result[i][j] = SAND.index;
				else if (z_value >= MOUNTAIN_HEIGHT) result[i][j] = MOUNTAIN.index;
				else if (z_value >= HILL_HEIGHT) result[i][j] = HILL.index;
				else result[i][j] = GRASS.index;
			}
		}
		return result;
	}
	
	public double[][] fractalMap(int detail, double roughness) {
		int divisions = 1 << detail;
		double[][] heatmap = new double[divisions+1][divisions+1];
		heatmap[0][0] = rnd();
		heatmap[0][divisions] = rnd();
		heatmap[divisions][0] = rnd();
		heatmap[divisions][divisions] = rnd();
		double rough = roughness;
		for (int i = 0; i < detail; ++ i) {
		      int r = 1 << (detail - i), s = r >> 1;
		      for (int j = 0; j < divisions; j += r)
		        for (int k = 0; k < divisions; k += r)
		          heatmap = diamond (heatmap,j, k, r, rough);
		      if (s > 0)
		        for (int j = 0; j <= divisions; j += s)
		          for (int k = (j + s) % r; k <= divisions; k += r)
		        	  heatmap = square (heatmap, divisions,j - s, k - s, r, rough);
		      rough *= roughness;
		}
		double min = heatmap[0][0]; double max = min;
	    for (int i = 0; i <= divisions; ++ i)
	      for (int j = 0; j <= divisions; ++ j)
	        if (heatmap[i][j] < min) min = heatmap[i][j];
	        else if (heatmap[i][j] > max) max = heatmap[i][j];
		for (int i=0; i < heatmap.length; i++) {
			for (int j=0; j < heatmap[0].length; j++) {
				double alt = heatmap[i][j];
				heatmap[i][j] = (alt - min) / (max - min);
			}
		}
		return heatmap;
	}
	
	private double[][] diamond (double[][] terrain, int x, int y, int side, double scale) {
	    if (side > 1) {
	      int half = side / 2;
	      double avg = (terrain[x][y] + terrain[x + side][y] +
	        terrain[x + side][y + side] + terrain[x][y + side]) * 0.25;
	      terrain[x + half][y + half] = avg + rnd () * scale;
	    } return terrain;
	}
	    
	private double[][] square (double[][] terrain, int divisions, int x, int y, int side, double scale) {
	    int half = side / 2;
	    double avg = 0.0, sum = 0.0;
	    if (x >= 0)
	    { avg += terrain[x][y + half]; sum += 1.0; }
	    if (y >= 0)
	    { avg += terrain[x + half][y]; sum += 1.0; }
	    if (x + side <= divisions)
	    { avg += terrain[x + side][y + half]; sum += 1.0; }
	    if (y + side <= divisions)
	    { avg += terrain[x + half][y + side]; sum += 1.0; }
	    terrain[x + half][y + half] = avg / sum + rnd () * scale;
	    return terrain;
	}
	private double rnd () {
		return 2. * random.nextDouble () - 1.0;
  	}
	
}
