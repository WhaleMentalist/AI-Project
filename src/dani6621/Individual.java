package dani6621;

/**
 * The class represents the encoding of the individual. 
 * Each individual can be made up of multiple chromosomes (each
 * with their own fitness function). An individual will be rated on 
 * the cumulative performance of each chromosome (i.e sum of the fitness 
 * scores)
 */
public class Individual {
	
	/**
	 * Contains the chromosome that issues policies pertaining to
	 * asteroid collecting
	 */
	public final AsteroidCollectorChromosome asteroidCollectorChromosome;
	
	/**
	 * Contains chromosome that issues policies pertaining to
	 * navigation
	 */
	public final NavigationChromosome navigationChromosome;
	
	/**
	 * Basic constructor that initializes the <code>Individual</code>
	 * instance
	 * 
	 * @param asteroidCollector	a chromosome containing asteroid collection 
	 * 							policy
	 * @param navigation	a chromosome containing navigation policy
	 */
	public Individual(AsteroidCollectorChromosome asteroidCollector, NavigationChromosome navigation) {
		asteroidCollectorChromosome = asteroidCollector;
		navigationChromosome = navigation;
	}
	
	/**
	 * Constructor that will pass parameters pertaining to the alleles of each chromosome.
	 * 
	 * @param energyRefuelThreshold	the amount of fuel before ship needs to find fuel source
	 * @param cargoholdCapcacity	the amount of resources in cargohold before going back to base
	 * @param maximumDistanceAsteroid	the maximum distance to consider an asteroid as a candidate
	 * @param asteroidDistanceResourceRatio	the ratio required to consider an asteroid as a candidate
	 * @param angleWeight	the weight the angle will have when considering an asteroid in some orientation from ship
	 * @param maxVelocity	the maximum velocity of ship
	 * @param minVelocity	the minimum pursuit velocity of ship
	 * @param obstacleDetection	the range when obstacles are detected by path finding algorithm
	 */
	public Individual(int energyRefuelThreshold, int cargoholdCapcacity, double maximumDistanceAsteroid, 
			double asteroidDistanceResourceRatio, double angleWeight, double maxVelocity, double minVelocity, 
			double obstacleDetection) {
		asteroidCollectorChromosome = new AsteroidCollectorChromosome(energyRefuelThreshold, cargoholdCapcacity, maximumDistanceAsteroid, 
																	asteroidDistanceResourceRatio, angleWeight);
		navigationChromosome = new NavigationChromosome(maxVelocity, minVelocity, obstacleDetection);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(asteroidCollectorChromosome + " " + navigationChromosome + "\n");
		return builder.toString();
	}
}
