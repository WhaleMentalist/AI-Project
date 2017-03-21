package dani6621;

/**
 * Abstract class that all <code>Chromosome</code> implementations
 * will extend. The class will contain functionality neccessary for
 * chromosomes such as selection, crossover, and mutation. Each different
 * type of <code>Chromosome</code> may implement the functionality in slighty 
 * different ways.
 *
 */
public abstract class AbstractChromosome {
	
	/**
	 * The fitness value for the chromosome (i.e score at the end of the game)
	 */
	public final double fitnessScore;
	
	/**
	 * Empty constructor
	 */
	public AbstractChromosome() {
		fitnessScore = Double.NEGATIVE_INFINITY; // Flag unassigned value for fitness function
	}
	
	/**
	 * Constructor wil create chromosome the fitness score
	 * 
	 * @param fitness	the fitness score for the chromosome
	 */
	public AbstractChromosome(double fitness) {
		fitnessScore = fitness;
	}
	
	/**
	 * Method will take instance of <code>AbstractChromosome</code> and 
	 * perform crossover on <code>chromosome</code> parameter.
	 * 
	 * @param chromosome	the other <code>AbstractChromosome</code> that 
	 * 						will be crossed with the instance
	 * 
	 * @return	a new <code>AbstractChromosome</code> that is the result of 
	 * 			of the crossover
	 */
	public abstract AbstractChromosome crossover(AbstractChromosome chromosome);
	
	/**
	 * Method will take instance of <code>AbstractChromosome</code> and perform
	 * mutation
	 * 
	 * @return	a new <code>AbstractChromosome</code> that is the result of the 
	 * 			mutation (if any)
	 */
	public abstract AbstractChromosome mutation();
	
	/**
	 * 
	 */
	public abstract void fitness();
}
