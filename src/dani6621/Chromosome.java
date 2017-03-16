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
	 * Component will guide the distance ship needs before 
	 * attempting to build another base
	 */
	public final double BASE_BUILD_DISTANCE_THRESHOLD;
	
	/**
	 * Component will dictate number of ships that can be built by agent
	 */
	public final int MAXIMUM_SHIP_NUMBER;
	
	/**
	 * Component will dictate how much the angle of the asteroid will
	 * affect its cost
	 */
	public final double ANGLE_WEIGHT;
	
	/**
	 * Basic constructor for chromosome generation
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
	 * @param angleWeight
	 */
	public Chromosome(double energyThreshold, double obstacleDetection,
			double maximumVelocity, double minimumVelocity, int cargoholdCapacity,
			double maximumDistanceToAsteroid, double asteroidResourceDistanceRatio, 
			double baseBuildDistanceThreshold, int maximumShipCount, 
			double angleWeight) {
		ENERGY_REFUEL_THRESHOLD = energyThreshold;
		OBSTACLE_DETECTION_THRESHOLD = obstacleDetection;
		MAXIMUM_VELOCITY = maximumVelocity;
		MINIMUM_VELOCITY = minimumVelocity;
		CARGOHOLD_CAPACITY = cargoholdCapacity;
		MAXIMUM_DISTANCE_TO_ASTEROID = maximumDistanceToAsteroid;
		ASTEROID_RESOURCE_DISTANCE_RATIO_THRESHOLD = asteroidResourceDistanceRatio;
		BASE_BUILD_DISTANCE_THRESHOLD = baseBuildDistanceThreshold;
		MAXIMUM_SHIP_NUMBER = maximumShipCount;
		ANGLE_WEIGHT = angleWeight;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Energy Threshold: " + ENERGY_REFUEL_THRESHOLD + "\n" +
						"Obstacle Detection: " + OBSTACLE_DETECTION_THRESHOLD + "\n" +
						"Max Velocity: " + MAXIMUM_VELOCITY + "\n" +
						"Min Velocity: " + MINIMUM_VELOCITY + "\n" + 
						"Cargohold Capacity: " + CARGOHOLD_CAPACITY + "\n" +
						"Max Distance To Asteroid: " + MAXIMUM_DISTANCE_TO_ASTEROID + "\n" +
						"Asteroid Resource vs Distance: " + ASTEROID_RESOURCE_DISTANCE_RATIO_THRESHOLD + "\n" +
						"Base Build Distance: " + BASE_BUILD_DISTANCE_THRESHOLD + "\n" + 
						"Max Ship Count: " + MAXIMUM_SHIP_NUMBER + "\n" + 
						"Angle Weight: " + ANGLE_WEIGHT + "\n");
		return builder.toString();
	}
	
}
