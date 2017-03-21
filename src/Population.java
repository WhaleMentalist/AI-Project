import dani6621.AbstractChromosome;
import dani6621.Utility;

/**
 * All classes of type <code>Population</code>
 * will extend this class. It will contain functionality 
 * neccessary for the genetic algorithm
 *
 */
public abstract class Population {
	
	/**
	 * Used when generating parents to crossover
	 */
	private static final int PAIR = 2;
	
	/**
	 * The population in the file (i.e generation)
	 */
	private AbstractChromosome[] population;
	
	/**
	 * The sum of the fitness score for population
	 */
	private double sumTotalScore;
	
	/**
	 * Initialize population with an array of chromosomes
	 * 
	 * @param pop	the array of chromosomes that will be used 
	 * 				for generation creation
	 */
	public Population(AbstractChromosome[] pop) {
		population = pop;
		sumTotalScore = 0;
		
		for(AbstractChromosome chromosome : population) {
			sumTotalScore += chromosome.fitnessScore;
		}
	}
	
	/**
	 * Generates weights for each of the chromosomes. 
	 * 
	 * @return	an array of <code>Double</code> maps the 
	 * 			probablity based on the fitness of the 
	 * 			chromosome.
	 */
	public Double[] populationWeights() {
		int populationSize = population.length;
		double min = 0.0; // The minimum range value
		Double[] weights = new Double[populationSize]; // Create 1-1 with chromosomes
		
		// Iterate through each chromosome
		for(int i = 0; i < populationSize; ++i) {
			weights[i] = min + (population[i].fitnessScore / sumTotalScore);
			min = weights[i];
		}
		
		return weights;
	}
	
	/**
	 * The function will select two parent chromosomes for the cross over 
	 * that will occur.
	 * 
	 * @return an array of two elements containing the parents for cross over
	 */
	public AbstractChromosome[] selection() {
		int populationSize = population.length;
		AbstractChromosome[] selected = new AbstractChromosome[PAIR];
		Double[] weights = populationWeights(); // Get weights for chromosomes
		double prob;
		
		for(int i = 0; i < PAIR; ++i) {
			prob = Utility.randomDouble(0.0, 1.0); // Using it because IT'S THERE
			
			for(int j = 0; j < populationSize; ++j) { 
				
				if(prob < weights[j]) { // If probablity is less than upper bound then it is selected!
					selected[i] = population[j];
					break;
				}
			}
		}
		return selected;
	}
}
