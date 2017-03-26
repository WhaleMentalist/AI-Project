package dani6621;

/**
 * The class is a design concept that encapsulates the production of a 
 * individuals. It will be useful for generating the initial population for the
 * genetic algorithm. This will create the individual with random data (i.e uniform
 * distribution of randomness)
 */
public class IndividualFactory {
	
	/**
	 * Maximum range for energy threshold constant when generating
	 * random values
	 */
	public static final int MAX_ENERGY_RANGE = 4500;
	
	/**
	 * Minimum range for energy threshold constant when generating
	 * random values
	 */
	public static final int MIN_ENERGY_RANGE = 500;
	
	/**
	 * Minimum range for cargohold capacity constant when generating
	 * random values
	 */
	public static final int MIN_CARGOHOLD_RANGE = 200;
	
	/**
	 * Maximum range for cargohold capacity constant when generating
	 * random values
	 */
	public static final int MAX_CARGOHOLD_RANGE = 10000;
	
	/**
	 * Minimum range for base building distance constant when generating
	 * random values
	 */
	public static final double MIN_BASE_BUILD_DISTANCE_RANGE = 100.0;
	
	/**
	 * Maximum range for base building distance constant when generating
	 * random values
	 */
	public static final double MAX_BASE_BUILD_DISTANCE_RANGE = 800.0;
	
	/**
	 * Minimum range for ship building constant when generating
	 * random values
	 */
	public static final int MIN_SHIP_COUNT_RANGE = 1;
	
	/**
	 * Maximum range for ship building constant when generating
	 * random values
	 */
	public static final int MAX_SHIP_COUNT_RANGE = 5;
	
	/**
	 * Minimum range for angle weight constant when generating
	 * random values
	 */
	public static final double MIN_ANGLE_WEIGHT_RANGE = 1.0;
	
	/**
	 * Maximum range for angle weight constant when generating
	 * random values
	 */
	public static final double MAX_ANGLE_WEIGHT_RANGE = 20.0;
	
	/**
	 * Method will create <code>Individual</code>
	 * 
	 * @return	the created individual
	 */
	public static Individual createIndividual() {
		
		AsteroidCollectorChromosome asteroidCollector;
		
		int energyThreshold = Utility.randomInteger(MIN_ENERGY_RANGE, MAX_ENERGY_RANGE);
		int cargoholdCapacity = Utility.randomInteger(MIN_CARGOHOLD_RANGE, MAX_CARGOHOLD_RANGE);
		double angleWeight = Utility.randomDouble(MIN_ANGLE_WEIGHT_RANGE, MAX_ANGLE_WEIGHT_RANGE);
		double baseBuild = Utility.randomDouble(MIN_BASE_BUILD_DISTANCE_RANGE, MAX_BASE_BUILD_DISTANCE_RANGE);
			
		asteroidCollector =  new AsteroidCollectorChromosome(energyThreshold, cargoholdCapacity, angleWeight, baseBuild);
		
		return new Individual(asteroidCollector);
	}
}
