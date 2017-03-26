package dani6621;
/**
 * All classes of type <code>Population</code> that helps with
 * organizing the data for genetic algorithm calculations. It will
 * contain <code>Individual</code> objects in a collection.
 *
 */
public class Population {
	
	/**
	 * The individuals in the population
	 */
	private Individual[] population;
	
	/**
	 * Used when generating parents to crossover
	 */
	private static final int PAIR = 2;
	
	/**
	 * Index location of first parent
	 */
	private static final int FIRST_PARENT = 0;
	
	/**
	 * Index location of second parent
	 */
	private static final int SECOND_PARENT = 1;
	
	/**
	 * The sum of the fitness score for population
	 */
	private double sumTotalScore;
	
	/**
	 * Contains the weights for each of the individuals
	 */
	private Double[] populationWeights;
	
	/**
	 * Initialization constructor
	 * 
	 * @param pop	the population to operate on
	 */
	public Population(Individual[] pop) {
		population = pop;
		sumTotalScore = 0;
		
		for(Individual ind : population) {
			System.out.println(ind.toString());
			sumTotalScore += ind.getFitnessScore();
		}
		
		populationWeights = populationWeights();
	}
	
	/**
	 * Generates weights for each of the individuals. 
	 * 
	 * @return	an array of <code>Double</code> maps the 
	 * 			probablity based on the fitness of the 
	 * 			individuals.
	 */
	private Double[] populationWeights() {
		int populationSize = population.length;
		double min = 0.0; // Minimum range so far
		Double[] weights = new Double[populationSize];
		
		// Iterate through each individual
		for(int i = 0; i < populationSize; ++i) {
			weights[i] = min + (population[i].getFitnessScore() / sumTotalScore);
			min = weights[i]; // Set minimum to max of previous weight
		}
		
		return weights;
	}
	
	/**
	 * The function will select two parent individuals for the cross over 
	 * that will occur.
	 * 
	 * @return an array of two elements containing the parents for cross over
	 */
	private Individual[] selection() {
		int populationSize = population.length;
		Individual[] selected = new Individual[PAIR];
		double prob;
		
		for(int i = 0; i < PAIR; ++i) {
			prob = Utility.randomDouble(0.0, 1.0); // Using utility to produce range of random values (i.e uniform)
			for(int j = 0; j < populationSize; ++j) { 
				if(prob < populationWeights[j]) { // If probablity is less than upper bound then it is selected!
					selected[i] = population[j];
					break;
				}
			}
		}
		return selected;
	}
	
	/**
	 * Creates new population (i.e generation) for next iteration of 
	 * genetic algorithm.
	 * 
	 * @return	the new generation
	 */
	public Individual[] createNextGeneration() {
		int populationSize = population.length;
		Individual[] newGeneration = new Individual[populationSize]; // Allocate space for new generation	
		Individual[] selected; // The individuals selected for crossover
		
		// Declare variable to hold reference to newly created chromosome
		AsteroidCollectorChromosome newAsteroidCollector;
		
		// Fill next generation population
		for(int i = 0; i < populationSize; ++i) {
			selected = selection(); // Select two for crossover
			
			// Crossover chromosomes
			newAsteroidCollector = (AsteroidCollectorChromosome) (selected[FIRST_PARENT].asteroidCollectorChromosome.
										crossover(selected[SECOND_PARENT].asteroidCollectorChromosome)).mutation();
			
			// Create new individual from breeding
			newGeneration[i] = new Individual(newAsteroidCollector); 
		}
		
		return newGeneration;
	}
}
