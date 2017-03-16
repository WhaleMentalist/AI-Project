package dani6621;

import java.util.Random;

/**
 * Simple utility class that offers functions that 
 * encapsulate operations and keep code clean by 
 * implementing repeated sections of code
 *
 */
public final class Utility {
	
	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static double randomDouble(double min, double max) {
		Random rand = new Random();
		return min + (max - min) * rand.nextDouble();
	}
	
	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randomInteger(int min, int max) {
		Random rand = new Random();
		return min + rand.nextInt((max - min) + 1);
	}
}
