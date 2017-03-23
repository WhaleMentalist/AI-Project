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
	 * The fitness value for the chromosome (i.e score at the end of the game or other attributes)
	 */
	protected double fitnessScore;
	
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
	 * Help method will implement crossover arithmetic and keep 
	 * <code>crossover</code> method cleaner 
	 * 
	 * @param parentTwo the second parent
	 * @param crossoverPoint	the crossover point when exhanging alleles
	 * 
	 * @return	a child chromosome as a result of the crossover
	 */
	protected abstract AbstractChromosome crossoverHelper(AbstractChromosome parentTwo, int crossoverPoint);
	
	/**
	 * Method will take instance of <code>AbstractChromosome</code> and perform
	 * mutation
	 * 
	 * @return	a new <code>AbstractChromosome</code> that is the result of the 
	 * 			mutation (if any)
	 */
	public abstract AbstractChromosome mutation();
	
	/**
	 * Method will help to mutate correct data members as mapped by the parameter passed
	 * 
	 * @param mutationResult	the values of mutation results
	 * @return	a new chromosome that may OR may NOT be mutated
	 */
	protected abstract AbstractChromosome mutationHelper(boolean[] mutationResult);
	
	/**
	 * Method returns fitness score
	 * 
	 * @return	the fitness score 
	 */
	public double getFitnessScore() {
		return fitnessScore;
	}
}
