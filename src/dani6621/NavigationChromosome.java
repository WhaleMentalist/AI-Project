package dani6621;

/**
 * 
 *
 */
public class NavigationChromosome extends AbstractChromosome {
	
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
	 * Component that will dictate when obstacles detected in the 
	 * 'AStar' path finding algorithm
	 */
	public final double OBSTACLE_DETECTION_THRESHOLD;
	
	/**
	 * Constructor will create chromosome with passed parameters
	 * 
	 * @param maxVelocity	the maximum velocity of ship
	 * @param minVelocity	the minimum velocity of ship
	 * @param obstacleDetection	the detection range of the ship for obstacles (i.e A* algorithm)
	 */
	public NavigationChromosome(double maxVelocity, double minVelocity, double obstacleDetection) {
		super();
		MAXIMUM_VELOCITY = maxVelocity;
		MINIMUM_VELOCITY = minVelocity;
		OBSTACLE_DETECTION_THRESHOLD = obstacleDetection;
	}

	@Override
	public AbstractChromosome crossover(AbstractChromosome chromosome) {
		return this;
	}
	
	@Override
	protected AbstractChromosome crossoverHelper(AbstractChromosome parentTwo, int crossoverPoint) {
		return this;
	}

	@Override
	public AbstractChromosome mutation() {
		return this;
	}
	
	@Override
	protected AbstractChromosome mutationHelper(boolean[] mutationResult) {
		return this;
	}
	
	/**
	 * Method will calculate the fitness of the chromosome
	 * 
	 * @param totalScore	the total score at end of game
	 * @param damageRecieved	the damage the agent recieved at end of game
	 */
	public void calculateFitness(double totalScore, double damageRecieved) {
		fitnessScore = 0; // Placeholder
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(MAXIMUM_VELOCITY + " " + MINIMUM_VELOCITY + " " +
						OBSTACLE_DETECTION_THRESHOLD);
		return builder.toString();
	}
}
