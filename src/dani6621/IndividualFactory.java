package dani6621;

/**
 * The class is a design concept that encapsulates the production of a 
 * individuals. It will be useful for generating the initial population for the
 * genetic algorithm. This will create the individual with random data (i.e uniform
 * distribution of randomness)
 */
public class IndividualFactory {
	
	/**
	 * Minimum range for energy threshold constant when generating
	 * random values
	 */
	public static final int MIN_ENERGY_RANGE = 500;
	
	/**
	 * Maximum range for energy threshold constant when generating
	 * random values
	 */
	public static final int MAX_ENERGY_RANGE = 4500;
	
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
	public static final double MAX_BASE_BUILD_DISTANCE_RANGE = 1000.0;
	
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
	 * Minimal range for rendezvous cargohold capacity
	 */
	public static final int MIN_RENDEZVOUS_CARGOHOLD_RANGE = 0;
	
	/**
	 * Maximum range for rendezvous cargohold capacity
	 */
	public static final int MAX_RENDEZVOUS_CARGOHOLD_RANGE = 9000;
	
	/**
	 * Minimal range for rendezvous distance
	 */
	public static final double MIN_RENDEZVOUS_DISTANCE = 100.0;
	
	/**
	 * Maximum range for rendezvous distance
	 */
	public static final double MAX_RENDEZVOUS_DISTANCE = 1000.0;
	
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
		int rendezvousCargohold = Utility.randomInteger(MIN_RENDEZVOUS_CARGOHOLD_RANGE, MAX_RENDEZVOUS_CARGOHOLD_RANGE);
		double rendezvousDistance = Utility.randomDouble(MIN_RENDEZVOUS_DISTANCE, MAX_RENDEZVOUS_DISTANCE);
			
		asteroidCollector =  new AsteroidCollectorChromosome(energyThreshold, cargoholdCapacity, angleWeight, baseBuild,
																rendezvousCargohold, rendezvousDistance);
		
		return new Individual(asteroidCollector);
	}
}
