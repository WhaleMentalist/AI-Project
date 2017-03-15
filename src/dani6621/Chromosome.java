package dani6621;

/**
 * The class represents the encoding for the problem the agent needs
 * to learn and find a suiutable solution. The class will contain simple 
 * variable values that 'guide' the agent's actions such as the energy threshold
 * for refuel.
 */
public class Chromosome {
	
	/**
	 * Component that guides when ship is to refuel at 
	 * certain energy level
	 */
	public final double ENERGY_REFUEL_THRESHOLD;
	
	/**
	 * Component that will dictate when obstacles detected in the 
	 * 'AStar' path finding algorithm
	 */
	public final double OBSTACLE_DETECTION_THRESHOLD;
	
	/**
	 * Component that will dictate the maximum velocity the 
	 * ship shall follow
	 */
	public final double MAXIMUM_VELOCITY;
	
	/**
	 * Component that will dictate the minimum velocity the
	 * ship shall follow
	 */
	public final double MINIMUM_VELOCITY;
	
	/**
	 * Component that will guide when ship return collected 
	 * resources to the base
	 */
	public final int CARGOHOLD_CAPACITY;
	
	/**
	 * Component will dictate the maximum distance the search for
	 * an asteroid
	 */
	public final double MAXIMUM_DISTANCE_TO_ASTEROID;
	
	/**
	 * Component will dictate if an asteroid is 'worth' the effort
	 * based on the distance and resource
	 */
	public final double ASTEROID_RESOURCE_DISTANCE_RATIO_THRESHOLD;
	
	/**
	 * Component will guide the agent as to whether the competition
	 * is TOO CLOSE to a resource (i.e agent will be beaten and should find
	 * an alternative)
	 */
	public final double COMPETITION_DISTANCE_THRESHOLD;
	
	/**
	 * Component will guide the distance ship needs before 
	 * attempting to build another base
	 */
	public final double BASE_BUILD_DISTANCE_THRESHOLD;
	
	/**
	 * Component will dictate number of ships that can be built by agent
	 */
	public final int MAXIMUM_SHIP_NUMBER;
	
	/**
	 * 
	 * @param energyThreshold
	 * @param obstacleDetection
	 * @param maximumVelocity
	 * @param minimumVelocity
	 * @param cargoholdCapacity
	 * @param maximumDistanceToAsteroid
	 * @param asteroidResourceDistanceRatio
	 * @param competitionDistanceThreshold
	 * @param baseBuildDistanceThreshold
	 * @param maximumShipCount
	 */
	public Chromosome(double energyThreshold, double obstacleDetection,
			double maximumVelocity, double minimumVelocity, int cargoholdCapacity,
			double maximumDistanceToAsteroid, double asteroidResourceDistanceRatio,
			double competitionDistanceThreshold, double baseBuildDistanceThreshold,
			int maximumShipCount) {
		ENERGY_REFUEL_THRESHOLD = energyThreshold;
		OBSTACLE_DETECTION_THRESHOLD = obstacleDetection;
		MAXIMUM_VELOCITY = maximumVelocity;
		MINIMUM_VELOCITY = minimumVelocity;
		CARGOHOLD_CAPACITY = cargoholdCapacity;
		MAXIMUM_DISTANCE_TO_ASTEROID = maximumDistanceToAsteroid;
		ASTEROID_RESOURCE_DISTANCE_RATIO_THRESHOLD = asteroidResourceDistanceRatio;
		COMPETITION_DISTANCE_THRESHOLD = competitionDistanceThreshold;
		BASE_BUILD_DISTANCE_THRESHOLD = baseBuildDistanceThreshold;
		MAXIMUM_SHIP_NUMBER = maximumShipCount;
	}
	
}
