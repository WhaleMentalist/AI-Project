package dani6621;

/**
 * The class is a design concept that encapsulates the production of a 
 * chromosome. It will be useful for generating the initial population for the
 * genetic algorithm
 */
public class ChromosomeFactory {
	
	/**
	 * Maximum range for energy threshold constant when generating
	 * random values
	 */
	private static final double MAX_ENERGY_RANGE = 4500.0;
	
	/**
	 * Minimum range for energy threshold constant when generating
	 * random values
	 */
	private static final double MIN_ENERGY_RANGE = 500.0;
	
	/**
	 * Maximum range for obstacle detection threshold constant when generating
	 * random values
	 */
	private static final double MAX_OBSTACLE_DETECTION_RANGE = 20.0;
	
	/**
	 * Minimum range for obstacle detection threshold constant when generating
	 * random values
	 */
	private static final double MIN_OBSTACLE_DETECTION_RANGE = 0.1;
	
	/**
	 * Maximum range for max velocity value constant when generating
	 * random values
	 */
	private static final double MAX_MAX_VELOCITY_RANGE = 300.0;
	
	/**
	 * Minimum range for max velocity value constant when generating
	 * random values
	 */
	private static final double MIN_MAX_VELOCITY_RANGE = 20.0;
	
	/**
	 * Maximum range for minimum velocity value constant when generating
	 * random values
	 */
	private static final double MAX_MIN_VELOCITY_RANGE = 100.0;
	
	/**
	 * Minimum range for minimum velocity value constant when generating
	 * random values
	 */
	private static final double MIN_MIN_VELOCITY_RANGE = 0.0;
	
	/**
	 * The value will help to detect if the maximum velocity and minimum velocity
	 * value generated are too close together
	 */
	private static final double VELOCITIES_TOO_CLOSE_MULTIPLIER = 0.8;
	
	/**
	 * Minimum range for cargohold capacity constant when generating
	 * random values
	 */
	private static final int MIN_CARGOHOLD_RANGE = 200;
	
	/**
	 * Maximum range for cargohold capacity constant when generating
	 * random values
	 */
	private static final int MAX_CARGOHOLD_RANGE = 10000;
	
	/**
	 * Minimum range for distance to asteroid constant when 
	 * generating random values
	 */
	private static final double MIN_DISTANCE_TO_ASTEROID_RANGE = 100.0;
	
	/**
	 * Maximum range for distance to asteroid constant when 
	 * generating random values
	 */
	private static final double MAX_DISTANCE_TO_ASTEROID_RANGE = 1000.0;
	
	/**
	 * Minimum range for asteroid resource versus distance ratio when
	 * generating random values
	 */
	private static final double MIN_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE = 0.5;
	
	/**
	 * Maximum range for asteroid resource versus distance ratio when
	 * generating random values
	 */
	private static final double MAX_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE = 6.0;
	
	/**
	 * Minimum range for base building distance constant when generating
	 * random values
	 */
	private static final double MIN_BASE_BUILD_DISTANCE_RANGE = 100.0;
	
	/**
	 * Maximum range for base building distance constant when generating
	 * random values
	 */
	private static final double MAX_BASE_BUILD_DISTANCE_RANGE = 800.0;
	
	/**
	 * Minimum range for ship building constant when generating
	 * random values
	 */
	private static final int MIN_SHIP_COUNT_RANGE = 1;
	
	/**
	 * Maximum range for ship building constant when generating
	 * random values
	 */
	private static final int MAX_SHIP_COUNT_RANGE = 5;
	
	/**
	 * Minimum range for angle weight constant when generating
	 * random values
	 */
	private static final double MIN_ANGLE_WEIGHT_RANGE = 1.0;
	
	/**
	 * Maximum range for angle weight constant when generating
	 * random values
	 */
	private static final double MAX_ANGLE_WEIGHT_RANGE = 20.0;
	
	public static Chromosome createChromosome() {
		double energyThreshold = Utility.randomDouble(MIN_ENERGY_RANGE, MAX_ENERGY_RANGE);
		double obstacleDetectionThreshold = Utility.randomDouble(MIN_OBSTACLE_DETECTION_RANGE, MAX_OBSTACLE_DETECTION_RANGE);
		double maximumVelocity = Utility.randomDouble(MIN_MAX_VELOCITY_RANGE, MAX_MAX_VELOCITY_RANGE);
		double minimumVelocity = Utility.randomDouble(MIN_MIN_VELOCITY_RANGE, MAX_MIN_VELOCITY_RANGE);
		
		// Check if minimum velocity is larger or within too close range
		if(minimumVelocity > (maximumVelocity * VELOCITIES_TOO_CLOSE_MULTIPLIER)) {
			minimumVelocity = maximumVelocity * VELOCITIES_TOO_CLOSE_MULTIPLIER; // Lower minimum velocity to 80% of max velocity
		}
		
		int cargoholdCapacity = Utility.randomInteger(MIN_CARGOHOLD_RANGE, MAX_CARGOHOLD_RANGE);
		double maximumDistanceToAsteroid = Utility.randomDouble(MIN_DISTANCE_TO_ASTEROID_RANGE, MAX_DISTANCE_TO_ASTEROID_RANGE);
		double asteroidResourceDistanceRatioThreshold = Utility.randomDouble(MIN_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE, 
																			MAX_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE);
		double baseBuildDistanceThreshold = Utility.randomDouble(MIN_BASE_BUILD_DISTANCE_RANGE, MAX_BASE_BUILD_DISTANCE_RANGE);
		int maximumShipNumber = Utility.randomInteger(MIN_SHIP_COUNT_RANGE, MAX_SHIP_COUNT_RANGE);
		double angleWeight = Utility.randomDouble(MIN_ANGLE_WEIGHT_RANGE, MAX_ANGLE_WEIGHT_RANGE);
		
		return new Chromosome(energyThreshold, obstacleDetectionThreshold, 
				maximumVelocity, minimumVelocity, cargoholdCapacity, 
				maximumDistanceToAsteroid, asteroidResourceDistanceRatioThreshold, 
				baseBuildDistanceThreshold, maximumShipNumber, angleWeight);
	}
}
