package dani6621;

/**
 * This class will contain attributes that guide the logic
 * behind collecting asteroids for the agent. It will have its 
 * own implementation of crossover and mutation.
 */
public class AsteroidCollectorChromosome extends AbstractChromosome {
	
	/**
	 * Enumeration allows access to data members in 
	 * indexable manner. This is to keep code 
	 * readable.
	 */
	private enum Alleles {
		
		ENERGY_REFUEL_THRESHOLD_INDEX(0),
		CARGOHOLD_CAPACITY_INDEX(1),
		MAXIMUM_DISTANCE_ASTEROID_INDEX(2),
		ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD_INDEX(3),
		ANGLE_WEIGHT(4);
		
		public final int index;
		
		private Alleles(int value) {
			index = value;
		}
	}
	
	/**
	 * The number of components contained in the chromosome
	 */
	private final int ALLELE_NUMBER = 5;
	
	/**
	 * The mutation rate that will occur for each allele. In this case
	 * a 5% chance.
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
		
		System.out.println("In 'crossover' for 'AsteroidCollector'");
		
		AsteroidCollectorChromosome child = null;
		
		// Generate random crossover point (i.e better diversity)
		int crossoverPoint = Utility.randomInteger(0, ALLELE_NUMBER - 2);
		child = (AsteroidCollectorChromosome) this.crossoverHelper(chromosome, crossoverPoint);

		return child;
	}
	

	@Override
	protected AbstractChromosome crossoverHelper(AbstractChromosome parentTwo,
			int crossoverPoint) {
		
		// Change type to proper one for access to data members
		AsteroidCollectorChromosome p1 = (AsteroidCollectorChromosome) this;
		AsteroidCollectorChromosome p2 = (AsteroidCollectorChromosome) parentTwo;
		AsteroidCollectorChromosome child = null; // Hold reference to created child
		
		// Check if both are same type of chromosome and correct type
		if(this instanceof AsteroidCollectorChromosome 
				&& parentTwo instanceof AsteroidCollectorChromosome) {
			
			if(crossoverPoint == Alleles.ENERGY_REFUEL_THRESHOLD_INDEX.index) {
				child = new AsteroidCollectorChromosome(p1.ENERGY_REFUEL_THRESHOLD, p2.CARGOHOLD_CAPACITY, 
						p2.MAXIMUM_DISTANCE_ASTEROID, p2.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						p2.ANGLE_WEIGHT);
			}
			else if(crossoverPoint == Alleles.CARGOHOLD_CAPACITY_INDEX.index) {
				child = new AsteroidCollectorChromosome(p1.ENERGY_REFUEL_THRESHOLD, p1.CARGOHOLD_CAPACITY, 
						p2.MAXIMUM_DISTANCE_ASTEROID, p2.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						p2.ANGLE_WEIGHT);
			}
			else if(crossoverPoint == Alleles.MAXIMUM_DISTANCE_ASTEROID_INDEX.index) {
				child = new AsteroidCollectorChromosome(p1.ENERGY_REFUEL_THRESHOLD, p1.CARGOHOLD_CAPACITY, 
						p1.MAXIMUM_DISTANCE_ASTEROID, p2.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						p2.ANGLE_WEIGHT);
			}
			else if(crossoverPoint == Alleles.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD_INDEX.index) {
				child = new AsteroidCollectorChromosome(p1.ENERGY_REFUEL_THRESHOLD, p1.CARGOHOLD_CAPACITY, 
						p1.MAXIMUM_DISTANCE_ASTEROID, p1.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD, 
						p2.ANGLE_WEIGHT);
			}
			else {
				; // Blank on purpose
			}
		}
		
		return child;
	}
	
	@Override
	public AbstractChromosome mutation() {
		
		System.out.println("In 'mutation' for 'AsteroidCollector'");
		
		boolean[] mutationResult = new boolean[ALLELE_NUMBER];
		double prob; // Produce probability to imitate mutation in nature
		
		// Iterate through each allele to attempt mutation
		for(int i = 0; i < ALLELE_NUMBER; ++i) {
			prob = Utility.randomDouble(0.0, 1.0); // Generate uniform distribution random value
			
			// Mutation wil occur
			if(prob < MUTATION_RATE) {
				System.out.println("Mutation...");
				mutationResult[i] = true;
			}
			else { // Mutation will NOT occur
				mutationResult[i] = false;
			}
		}
		
		return this.mutationHelper(mutationResult);
	}
	
	@Override
	protected AbstractChromosome mutationHelper(boolean[] mutationResult) {
		
		// Maintain old values of chromosome
		int energyThreshold = this.ENERGY_REFUEL_THRESHOLD;
		int cargoholdCapacity = this.CARGOHOLD_CAPACITY;
		double maximumDistanceAsteroid = this.MAXIMUM_DISTANCE_ASTEROID;
		double asteroidDistanceVsResourceRatio = this.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD;
		double angleWeight = this.ANGLE_WEIGHT;
		
		
		// Below mutation result is checked
		if(mutationResult[Alleles.ENERGY_REFUEL_THRESHOLD_INDEX.index]) {
			energyThreshold = Utility.randomInteger(IndividualFactory.MIN_ENERGY_RANGE, 
					IndividualFactory.MAX_ENERGY_RANGE);
		}
		
		if(mutationResult[Alleles.CARGOHOLD_CAPACITY_INDEX.index]) {
			cargoholdCapacity = Utility.randomInteger(IndividualFactory.MIN_CARGOHOLD_RANGE, 
					IndividualFactory.MAX_CARGOHOLD_RANGE);
		}
		
		if(mutationResult[Alleles.MAXIMUM_DISTANCE_ASTEROID_INDEX.index]) {
			maximumDistanceAsteroid = Utility.randomDouble(IndividualFactory.MIN_DISTANCE_TO_ASTEROID_RANGE, 
					IndividualFactory.MAX_DISTANCE_TO_ASTEROID_RANGE);
		}
		
		if(mutationResult[Alleles.ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD_INDEX.index]) {
			asteroidDistanceVsResourceRatio = 
					Utility.randomDouble(IndividualFactory.MIN_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE, 
					IndividualFactory.MAX_ASTEROID_RESOURCE_DISTANCE_RATIO_RANGE);
		}
		
		if(mutationResult[Alleles.ANGLE_WEIGHT.index]) {
			angleWeight = Utility.randomDouble(IndividualFactory.MIN_ANGLE_WEIGHT_RANGE, 
					IndividualFactory.MAX_ANGLE_WEIGHT_RANGE);
		}
		
		// Construct new chromosome (this is due to immutable implementation)
		return new AsteroidCollectorChromosome(energyThreshold, cargoholdCapacity, maximumDistanceAsteroid, 
				asteroidDistanceVsResourceRatio, angleWeight);
	}
	
	/**
	 * Method will set the fitness for the particular chromosome
	 * 
	 * @param totalScore	the total score the agent recieved at the end of the game
	 */
	public void calculateFitness(double totalScore) {
		fitnessScore = totalScore;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(ENERGY_REFUEL_THRESHOLD + " " + CARGOHOLD_CAPACITY + " " +
						MAXIMUM_DISTANCE_ASTEROID + " " + ASTEROID_DISTANCE_VS_RESOURCE_RATIO_THRESHOLD + " " +
						ANGLE_WEIGHT);
		return builder.toString();
	}
}
