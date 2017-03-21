package dani6621;

/**
 * This class will contain attributes that guide the logic
 * behind collecting asteroids for the agent. 
 */
public class AsteroidCollectorChromosome extends AbstractChromosome{
	
	/**
	 * The number of components contained in the chromosome
	 */
	private final int ALLELE_NUMBER = 5;
	
	/**
	 * The mutation rate that will occur for each allele
	 */
	private final double MUTATION_RATE = 0.05;
	
	/**
	 * Dictates when ships goes back to refuel or attain energy
	 */
	public final int ENERGY_REFUEL_THRESHOLD;
	
	/**
	 * Dictates when ships returns to drop-off resources
	 */
	public final int CARGOHOLD_CAPACITY;
	
	/**
	 * Dictates what distance asteroids are searchable
	 */
	public final double MAXIMUM_DISTANCE_ASTEROID;
	
	/**
	 * Dictates a threshold of when to go after asteroid
	 */
	public final double ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD;
	
	/**
	 * Dictates the weight the angle will have when attempting to search for
	 * asteroids
	 */
	public final double ANGLE_WEIGHT;
	
	/**
	 * The constructor will create chromosome given parameters
	 * 
	 * @param energyRefuelThreshold
	 * @param cargoholdCapcacity
	 * @param maximumDistanceAsteroid
	 * @param asteroidDistanceResourceRatio
	 * @param angleWeight
	 */
	public AsteroidCollectorChromosome(int energyRefuelThreshold, int cargoholdCapcacity,
										double maximumDistanceAsteroid, double asteroidDistanceResourceRatio,
										double angleWeight) {
		super();
		ENERGY_REFUEL_THRESHOLD = energyRefuelThreshold;
		CARGOHOLD_CAPACITY = cargoholdCapcacity;
		MAXIMUM_DISTANCE_ASTEROID = maximumDistanceAsteroid;
		ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD = asteroidDistanceResourceRatio;
		ANGLE_WEIGHT = angleWeight;
	}
	
	/**
	 * The constructor will create chromosome given parameters
	 * 
	 * @param energyRefuelThreshold
	 * @param cargoholdCapcacity
	 * @param maximumDistanceAsteroid
	 * @param asteroidDistanceResourceRatio
	 * @param angleWeight
	 * @param fitness
	 */
	public AsteroidCollectorChromosome(int energyRefuelThreshold, int cargoholdCapcacity,
										double maximumDistanceAsteroid, double asteroidDistanceResourceRatio,
										double angleWeight, double fitness) {
		super(fitness);
		ENERGY_REFUEL_THRESHOLD = energyRefuelThreshold;
		CARGOHOLD_CAPACITY = cargoholdCapcacity;
		MAXIMUM_DISTANCE_ASTEROID = maximumDistanceAsteroid;
		ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD = asteroidDistanceResourceRatio;
		ANGLE_WEIGHT = angleWeight;
	}

	@Override
	public AbstractChromosome crossover(AbstractChromosome chromosome) {
		
		AsteroidCollectorChromosome child = null;
		
		if(chromosome instanceof AsteroidCollectorChromosome) {
			AsteroidCollectorChromosome mommy = (AsteroidCollectorChromosome) chromosome;
			int crossOverPoint = Utility.randomInteger(1, ALLELE_NUMBER - 1);
			
			if(crossOverPoint == 1) {
				child = new AsteroidCollectorChromosome(this.ENERGY_REFUEL_THRESHOLD, mommy.CARGOHOLD_CAPACITY, 
						mommy.MAXIMUM_DISTANCE_ASTEROID, mommy.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						mommy.ANGLE_WEIGHT);
			}
			else if(crossOverPoint == 2) {
				child = new AsteroidCollectorChromosome(this.ENERGY_REFUEL_THRESHOLD, this.CARGOHOLD_CAPACITY, 
						mommy.MAXIMUM_DISTANCE_ASTEROID, mommy.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						mommy.ANGLE_WEIGHT);
			}
			else if(crossOverPoint == 3) {
				child = new AsteroidCollectorChromosome(this.ENERGY_REFUEL_THRESHOLD, this.CARGOHOLD_CAPACITY, 
						this.MAXIMUM_DISTANCE_ASTEROID, mommy.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						mommy.ANGLE_WEIGHT);
			}
			else {
				child = new AsteroidCollectorChromosome(this.ENERGY_REFUEL_THRESHOLD, this.CARGOHOLD_CAPACITY, 
						this.MAXIMUM_DISTANCE_ASTEROID, this.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						mommy.ANGLE_WEIGHT);
			}
		}
		return child;
	}

	@Override
	public AbstractChromosome mutation() {
		
		double prob = Utility.randomDouble(0.0, 1.0);
		
		int energyThreshold = this.ENERGY_REFUEL_THRESHOLD;
		int cargohold = this.CARGOHOLD_CAPACITY;
		double maxDistance = this.MAXIMUM_DISTANCE_ASTEROID;
		double asteroidDistanceResourceRatio = this.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD;
		double angleWeight = this.ANGLE_WEIGHT;
		
		
		// Attempt to mutate the energy refuel
		if(prob < MUTATION_RATE) {
			energyThreshold = Utility.randomInteger(ChromosomeFactory.MIN_ENERGY_RANGE, 
					ChromosomeFactory.MAX_ENERGY_RANGE);
		}
		
		prob = Utility.randomDouble(0.0, 1.0);
		
		// Attempt to mutate the cargohold capacity
		if(prob < MUTATION_RATE) {
			cargohold = Utility.randomInteger(ChromosomeFactory.MIN_CARGOHOLD_RANGE, 
					ChromosomeFactory.MAX_CARGOHOLD_RANGE);
		}
		
		prob = Utility.randomDouble(0.0, 1.0);
		
		// Attempt to mutate the maximum distance to get asteroid
		if(prob < MUTATION_RATE) {
			maxDistance = Utility.randomDouble(ChromosomeFactory.MIN_DISTANCE_TO_ASTEROID_RANGE, 
					ChromosomeFactory.MAX_DISTANCE_TO_ASTEROID_RANGE);
		}
		
		prob = Utility.randomDouble(0.0, 1.0);
		
		// Attempt to mutate the asteroid distance versus resource ratio
		if(prob < MUTATION_RATE) {
			asteroidDistanceResourceRatio = 
					Utility.randomDouble(ChromosomeFactory.MIN_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE, 
					ChromosomeFactory.MAX_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE);
		}
		
		prob = Utility.randomDouble(0.0, 1.0);
		
		// Attempt to mutate the angle weight
		if(prob < MUTATION_RATE) {
			asteroidDistanceResourceRatio = Utility.randomDouble(ChromosomeFactory.MIN_ANGLE_WEIGHT_RANGE, 
											ChromosomeFactory.MAX_ANGLE_WEIGHT_RANGE);
		}
		
		return new AsteroidCollectorChromosome(energyThreshold, cargohold, maxDistance, 
												asteroidDistanceResourceRatio, angleWeight);
	}

	@Override
	public void fitness() {
	
	}
	
	/**
	 * Gets the fitness score
	 * 
	 * @return	the fitness score for the chromosome
	 */
	public double getFitnessScore() {
		return fitnessScore;
	}
}
