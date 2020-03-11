package golf_map_generator;

import main.Function2d;

public class MapGenUtils {
	
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
			for (int j=0; j < result[i].length; j++) result[i][j] = function.evaluate(i/100d, j/100d);
		return result;
	}
	
}
