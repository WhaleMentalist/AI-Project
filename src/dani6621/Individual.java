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
	 * The fitness score the indivdual recieved
	 */
	private double fitnessScore;
	
	/**
	 * Basic constructor that initializes the <code>Individual</code>
	 * instance
	 * 
	 * @param asteroidCollector	a chromosome containing asteroid collection 
	 * 							policy
	 */
	public Individual(AsteroidCollectorChromosome asteroidCollector) {
		asteroidCollectorChromosome = asteroidCollector;
	}
	
	/**
	 * Constructor that will pass parameters pertaining to the alleles of each chromosome.
	 * 
	 * @param energyRefuelThreshold	the amount of fuel before ship needs to find fuel source
	 * @param cargoholdCapcacity	the amount of resources in cargohold before going back to base
	 * @param angleWeight	the weight the angle will have when considering an asteroid in some orientation from ship
	 * @param baseBuild	the minimum distance needed to build base
	 */
	public Individual(int energyRefuelThreshold, int cargoholdCapcacity, double angleWeight, double baseBuild) {
		asteroidCollectorChromosome = new AsteroidCollectorChromosome(energyRefuelThreshold, cargoholdCapcacity, angleWeight, baseBuild);
	}
	
	/**
	 * Constructor that will pass parameters pertaining to the alleles of each chromosome.
	 * 
	 * @param energyRefuelThreshold	the amount of fuel before ship needs to find fuel source
	 * @param cargoholdCapcacity	the amount of resources in cargohold before going back to base
	 * @param angleWeight	the weight the angle will have when considering an asteroid in some orientation from ship
	 * @param baseBuild	the minimum distance needed to build base
	 * @param fitness	the fitness score the individual recieved
	 */
	public Individual(int energyRefuelThreshold, int cargoholdCapcacity, double angleWeight, double baseBuild, double fitness) {
		asteroidCollectorChromosome = new AsteroidCollectorChromosome(energyRefuelThreshold, cargoholdCapcacity, angleWeight, baseBuild);
		fitnessScore = fitness;
	}
	
	/**
	 * Method will retrieve value of fitness score
	 * 
	 * @return	a the fitness value
	 */
	public double getFitnessScore() {
		return fitnessScore;
	}
	
	/**
	 * Method will set the fitness score for the individual
	 * 
	 * @param score	the score that will be set
	 */
	public void setFitnessScore(double score) {
		fitnessScore = score;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(asteroidCollectorChromosome + "\n");
		return builder.toString();
	}
}
