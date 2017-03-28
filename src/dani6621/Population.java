package dani6621;

import java.util.Arrays;
import java.util.Comparator;


/**
 * All classes of type <code>Population</code> that helps with
 * organizing the data for genetic algorithm calculations. It will
 * contain <code>Individual</code> objects in a collection.
 *
 */
public class Population {
	
	/**
	 * Always keep best chromosome in next generation. About 5% of population size.
	 * Variable will always be at least one... This will help to at least maintain
	 * a steady upward trend.
	 */
	private static final int ELITE_POOL = (int) Math.floor((IndividualBookKeeper.POPULATION_COUNT * 0.05) + 1.0);
	
	/**
	 * Dictates the size of the tournament. This can affect how diverse population becomes, so
	 * experiment with caution (or fun).
	 */
	private static final int TOURNAMENT_SIZE = 4;
	
	/**
	 * The individuals in the population
	 */
	private Individual[] population;
	
	/**
	 * Contains the next generation of chromosome for algorithm
	 */
	private Individual[] newGeneration;
	
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
	 * Initialization constructor
	 * 
	 * @param pop	the population to operate on
	 */
	public Population(Individual[] pop) {
		population = pop;
		newGeneration = new Individual[IndividualBookKeeper.POPULATION_COUNT];
	}
	
	/**
	 * The function will select two parent individuals for the cross over 
	 * that will occur. This will issue a tournament based selection with 
	 * elitism.
	 * 
	 * @return an array of two elements containing the parents for cross over
	 */
	private Individual[] selection() {
		// Perform tournament selection
		Individual[] selected = new Individual[PAIR]; // Selected pair for crossover
		selected[FIRST_PARENT] = tournament();
		selected[SECOND_PARENT] = tournament();
		return selected;
	}
	
	/**
	 * Creates a tournament pool and selects individual with 
	 * highest fitness score
	 * 
	 * @return	the individual with highest fitness score
	 */
	private Individual tournament() {
		int popSize = population.length;
		Individual[] tournamentPool = new Individual[TOURNAMENT_SIZE]; // Allocate space for tournament
		int indexValue; // Store result of random number
		Individual candidate; // Store the winner of tournament when calculating
		
		// Select participants for tournament
		for(int i = 0; i < TOURNAMENT_SIZE; ++i) {
			indexValue = Utility.randomInteger(0, (popSize - 1));
			tournamentPool[i] = population[indexValue]; // Select random individual
		}
		
		candidate = tournamentPool[0]; // Start with first candidate as 'winner'
		
		// Now found the winner... (I know this is not the best way to implement)
		for(int j = 1; j < TOURNAMENT_SIZE; ++j) {
			// If we found individual with higher score then set candidate to new winner
			if(tournamentPool[j].getFitnessScore() > candidate.getFitnessScore()) {
				candidate = tournamentPool[j]; // Set to new one
			}
		}
		return candidate; // Return the winner of the jousting tournament
	}
	
	/**
	 * Function will apply elitism to the population before
	 * performing selection. This is to preserve good solutions
	 * against mutation and crossover.
	 */
	private void applyElitism() {
		
		Individual[] copyPopulation = population.clone(); // Create a new copy to preserve the randomness of population index
		
		// Sort population by fitness
		Arrays.sort(copyPopulation, new Comparator<Individual>() {
			@Override
			public int compare(Individual indOne, Individual indTwo) {
				return Double.compare(indOne.getFitnessScore(), indTwo.getFitnessScore());
			}
		});
		
		int popSize = population.length; // Useful reference for cleaner code reading
		
		// Must iterate and select top members of population
		for(int i = 0; i < ELITE_POOL; ++i) {
			System.out.println("Selected individual with score: " + copyPopulation[(popSize - 1) - i].getFitnessScore());
			newGeneration[i] = copyPopulation[(popSize - 1) - i];
		}
	}
	
	/**
	 * Creates new population (i.e generation) for next iteration of 
	 * genetic algorithm.
	 * 
	 * @return	the new generation
	 */
	public Individual[] createNextGeneration() {
		applyElitism(); // Apply elitism to presever good solutions
		Individual[] selected; // Selected individuals for crossover
		AsteroidCollectorChromosome newAsteroidCollector; // Store reference to new chromosome creation
		
		int currentGenerationSize = ELITE_POOL; // Size of next generation so far
		
		// While next generation isn't full continue the algorithm
		while(currentGenerationSize < IndividualBookKeeper.POPULATION_COUNT) {
			selected = selection(); // Perform selection
			newAsteroidCollector = (AsteroidCollectorChromosome) (selected[FIRST_PARENT].asteroidCollectorChromosome.
																	crossover(selected[SECOND_PARENT].asteroidCollectorChromosome));
			if(newAsteroidCollector != null) { // Crossover happened!
				newAsteroidCollector.mutation(); // Attempt mutation
				newGeneration[currentGenerationSize] = new Individual(newAsteroidCollector); // Put into new generation
				++currentGenerationSize; // Increment size of generation
			}
		}
		return newGeneration;
	}
}
