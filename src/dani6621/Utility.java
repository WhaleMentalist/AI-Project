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
	 * Generates a random double value that is in the specified range
	 * 
	 * @param min	the bottom range value
	 * @param max	the top range value
	 * @return	a random double
	 */
	public static double randomDouble(double min, double max) {
		Random rand = new Random();
		return min + (max - min) * rand.nextDouble();
	}
	
	/**
	 * Generates a random integer value that is in the specified range
	 * 
	 * @param min	the bottom range value
	 * @param max	the top range value
	 * @return	a random integer
	 */
	public static int randomInteger(int min, int max) {
		Random rand = new Random();
		return min + rand.nextInt((max - min) + 1);
	}
}
