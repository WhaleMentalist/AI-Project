package dani6621;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractChromosome mutation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fitness() {
		// TODO Auto-generated method stub
		
	}

}
