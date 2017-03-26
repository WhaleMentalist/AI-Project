package dani6621;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class is designed to hold data on individuals, create an initial 
 * population, generate new generations (selection, crossover, and mutation),
 * assign individuals to agent, regulate access to files containing individuals 
 * among processes, and keep information of individuals updated.
 *
 */
public class IndividualBookKeeper {
	
	/**
	 * This is the amount of chromosomes that will be produced 
	 * in each generation
	 */
	public static final int POPULATION_COUNT = 6;
	
	/**
	 * The file extension for knowlodge files
	 */
	private static final String EXTENSION = ".txt";
	
	/**
	 * Variable will help cut extension off the file name
	 */
	private static final int EXTENSION_CUTOFF = EXTENSION.length();
	
	/**
	 * Variable holds name of directory containing knowledge files
	 */
	private static final String KNOWLEDGE_DIRECTORY_NAME = "/knowledge/";
	
	/**
	 * The name delimiting each knowledge file
	 */
	private static final String KNOWLEDGE_FILE_BASE_NAME = "generation";
	
	/**
	 * Number of tokens for unassigned individual
	 */
	private static final int UNASSIGNED = 4;
	
	/**
	 * Number of tokens for individual assigned a score
	 */
	private static final int SCORE_ASSIGNED = 6;
	
	/**
	 * Holds the path to file holding assigned generation
	 */
	private String assignedGeneration;
	
	/**
	 * Holds the generation number the book keeper is on
	 */
	private int assignedGenerationNumber;
	
	/**
	 * Hold the individual that was assigned. It will correspond to a 
	 * line within the file
	 */
	private int assignedIndividualID;
	
	/**
	 * The assigned individual
	 */
	private Individual assignedIndividual;
	
	/**
	 * Basic constructor
	 */
	public IndividualBookKeeper() {
		assignedGeneration = "";
		assignedGenerationNumber = -1;
		assignedIndividualID = -1;
		assignedIndividual = null;
		initialize(); // Always run initialization to check existance and/or prepare file structure
		assignIndividual(); // Attempt to assign individual
	}
	
	/**
	 * Method creates neccessary directory and initial population 
	 * if it is needed. It will also find the latest generation file to
	 * start assigning an individual to the program.
	 */
	private void initialize() {
		
		// Retrieve project base path
		Path projectPath = Paths.get("").toAbsolutePath().getParent();
		
		// Create directory in project path containing knowledge files (NOTE: This will NOT overwrite if it already exists)
		new File(projectPath.toString() + KNOWLEDGE_DIRECTORY_NAME).mkdirs();
		
		// Get path of knowledge directory
		File knowledgeDirectory = new File(projectPath.toString() + KNOWLEDGE_DIRECTORY_NAME);
		
		// Get list of files inside knowledge direcotry
		File[] files = knowledgeDirectory.listFiles();
		
		String filePath = ""; // Path to file
		String fileName = ""; // Name of the file
		int generationNumber = -1; // Hold current generation number, if found, for comparison
		
		for(File file : files) {
			filePath = file.getAbsolutePath(); // Get path of 'file'
			fileName = file.getName(); // Name of 'file'
			
			// If we find the file (delimited by predetermined name)
			if(fileName.contains(KNOWLEDGE_FILE_BASE_NAME)) {
				
				try {
					generationNumber = Integer.parseInt(fileName.substring(KNOWLEDGE_FILE_BASE_NAME.length(), 
							fileName.length() - EXTENSION_CUTOFF));
				}
				catch(NumberFormatException e) { // If an exception occurs... Skip the file!
					continue;
				}
				
				
				// If we find an even later generation value
				if(generationNumber > assignedGenerationNumber) {
					assignedGenerationNumber = generationNumber;
					assignedGeneration = filePath; // Set data member to value
				}
			}
		}
		
		// If we never found a file, then we need to initialize the initial population
		if(assignedGeneration.equals("") && assignedGenerationNumber == -1) {
			
			System.out.println("No population file found... Creating initial population...");
			
			// Create path to initial file we will create - notice it uses '0' for first generation
			assignedGenerationNumber = 0;
			assignedGeneration = new String(projectPath.toString() + KNOWLEDGE_DIRECTORY_NAME + KNOWLEDGE_FILE_BASE_NAME + 
										assignedGenerationNumber + EXTENSION);
			
			try {
				// Open for writing
				FileOutputStream outputStream = new FileOutputStream(new File(assignedGeneration), false);
				FileChannel fileChannel = outputStream.getChannel();
				
				System.out.println("File Channel opened for write. Attempting to aquire lock (in blocking mode)...");
				FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false); // Lock whole file... No shared lock...
				System.out.println("Lock aquired... Preparing to write...");
				System.out.println("Lock is shared: " + lock.isShared());
				
				for(int i = 0; i < POPULATION_COUNT; ++i) {
					fileChannel.write(ByteBuffer.
							wrap(IndividualFactory.createIndividual().
							toString().getBytes()));
				}
				
				outputStream.close(); // Close stream (also closes associated channel)... Also it releases the lock
				System.out.println("Population creation complete...");
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else { // Found population file
			System.out.println("File found: " + assignedGeneration);
		}
	}
	
	/**
	 * Method will assign a individual by finding first unassigned 
	 * instance in the 'assignedGeneration' file. If it does it will
	 * need to rewrite whole file in order to mark individual as 
	 * used. This has to do with how files work (i.e no programming langauage could
	 * fix this problem). It shouldn't cause too much delay, as the file sizes are 
	 * small (100 lines at most).
	 */
	private void assignIndividual() {
		
		try {
			
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(assignedGeneration);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"); // Open in read-write mode
			FileChannel fileChannel = randomAccessFile.getChannel(); // Get fiel channel associated
			
			System.out.println("File Channel opened for read. Attempting to aquire lock (in blocking mode)...");
			FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false);
			System.out.println("Lock aquired... Preparing to read...");
			System.out.println("Lock is shared: " + lock.isShared());
			
			byte[] byteArray = new byte[(int) randomAccessFile.length()]; // Allocate space for file data
			ByteBuffer buffer = ByteBuffer.wrap(byteArray); // Put data into a buffer
			fileChannel.read(buffer); // Read data from channel into buffer
			
			InputStream inputStream = new ByteArrayInputStream(buffer.array()); // Put buffer into input stream
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // Treat it like a normal file
			String line = bufferedReader.readLine(); // Read a line to start off
			fileChannel.position(0); // Set at start of the file
			
			int individualCounter = 1; // Counter to track individuals
			
			// Read each line (i.e a individual) and find first instance of an unassigned
			while(line != null) {
				
				String[] tokens = line.split("\\s+"); // Split on any number of whitespace
				
				// Check if chromosome is unassigned and if the chromosome hasn't already been assigned
				if(tokens != null && tokens.length == UNASSIGNED && assignedIndividualID < 1) {
					line += " A\n"; // Append an assigned token at end of 'line'
					assignedIndividualID = individualCounter;
					// Construct the 'Individual' object from file data
					assignedIndividual = new Individual(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]),
													Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
				}
				else { // Got to append a return line
					line += "\n";
				}
				
				fileChannel.write(ByteBuffer.wrap(line.getBytes()));
				line = bufferedReader.readLine();
				++individualCounter;
			}
			
			System.out.println("Individual assignment: " + assignedIndividualID);
			
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method will assign a fitness to the <code>assignedIndividual</code>.
	 * This will require the whole file to be re-written.
	 * 
	 * @param score	the score the agent recieved at the end of the game
	 * @param damageRecieved	the amount of damage the agent recieved
	 */
	public void assignFitness(double score) {
		
		try {
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(assignedGeneration);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"); // Open in read-write mode
			FileChannel fileChannel = randomAccessFile.getChannel(); // Get fiel channel associated
			
			System.out.println("From 'assignFitness' function...");
			System.out.println("File Channel opened for read. Attempting to aquire lock (in blocking mode)...");
			FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false);
			System.out.println("Lock aquired... Preparing to read...");
			System.out.println("Lock is shared: " + lock.isShared());
						
			byte[] byteArray = new byte[(int) randomAccessFile.length()]; // Allocate space for file data
			ByteBuffer buffer = ByteBuffer.wrap(byteArray); // Put data into a buffer
			fileChannel.read(buffer); // Read data from channel into buffer
						
			InputStream inputStream = new ByteArrayInputStream(buffer.array()); // Put buffer into input stream
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // Treat it like a normal file
			String line = bufferedReader.readLine(); // Read a line to start off
			fileChannel.position(0); // Set at start of the file
			
			int currentChromosome = 1;
			double totalFitness;
			
			while(line != null) {
				
				if(currentChromosome == assignedIndividualID) {
					assignedIndividual.asteroidCollectorChromosome.calculateFitness(score);
					totalFitness = assignedIndividual.asteroidCollectorChromosome.getFitnessScore();
					line += " " + totalFitness + "\n";
				}
				else { // Got to append a return line
					line += "\n";
				}
				
				fileChannel.write(ByteBuffer.wrap(line.getBytes()));
				line = bufferedReader.readLine();
				++currentChromosome;
			}
			
			System.out.println("Finished assigning fitness...");
			
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method will check the <code>assignedGeneration</code> file and 
	 * if the file has all the individuals assigned a fitness score
	 * then the algorithm will perform selection, mutation, and 
	 * crossover.
	 */
	public void checkAssignedGeneration() {
		
		try {
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(assignedGeneration);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"); // Open in read-write mode
			FileChannel fileChannel = randomAccessFile.getChannel(); // Get file channel associated
			
			System.out.println("From 'checkAssignedGeneration' function...");
			System.out.println("File Channel opened for read. Attempting to aquire lock (in blocking mode)...");
			FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false);
			System.out.println("Lock aquired... Preparing to read...");
			System.out.println("Lock is shared: " + lock.isShared());
						
			byte[] byteArray = new byte[(int) randomAccessFile.length()]; // Allocate space for file data
			ByteBuffer buffer = ByteBuffer.wrap(byteArray); // Put data into a buffer
			fileChannel.read(buffer); // Read data from channel into buffer
						
			InputStream inputStream = new ByteArrayInputStream(buffer.array()); // Put buffer into input stream
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // Treat it like a normal file
			String line = bufferedReader.readLine(); // Read a line to start off
			boolean allAssigned = true; // Assume all values assigned, but we check
			String[] tokens; // Hold tokens of line
			
			Individual[] individuals = new Individual[POPULATION_COUNT]; // Allocate for storage of current generation
			int individualCounter = 0; // Keep track of where we are at in file and for indexing
			
			// Go to the end of the file
			while(line != null) {
				tokens = line.split("\\s+"); // Split on any number of whitespace
				
				if(tokens != null && tokens.length != SCORE_ASSIGNED) {
					allAssigned = false;
					break; // Found instance that is not assigned we can stop
				}
				
				// Create individual from file data
				individuals[individualCounter] = new Individual(Integer.parseInt(tokens[0]), 
						Integer.parseInt(tokens[1]), Double.parseDouble(tokens[2]), 
						Double.parseDouble(tokens[3]), Double.parseDouble(tokens[5]));
				
				line = bufferedReader.readLine(); // Read next line
				++individualCounter;
			}
			
			System.out.println("Check complete...");
			
			// We need to create a new generation
			if(allAssigned) {
				
				// Construct population objects to perform genetic algorithm calculations
				Population currentGeneration = new Population(individuals);
				createNextGeneration(currentGeneration);
			}
			
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method will create next generation and write them to a file
	 * 
	 * @param currentGeneration	the population containing individuals to breed
	 */
	public void createNextGeneration(Population currentGeneration) {
		
		Individual[] nextGeneration = currentGeneration.createNextGeneration(); // Making babies
		String newGeneration; // Store path
		int newGenerationNumber = assignedGenerationNumber + 1; // Create next number using current data
		
		// Retrieve project base path
		Path projectPath = Paths.get("").toAbsolutePath().getParent();
		
		// Create path to new generation file
		newGeneration = new String(projectPath.toString() + KNOWLEDGE_DIRECTORY_NAME + KNOWLEDGE_FILE_BASE_NAME + 
									newGenerationNumber + EXTENSION);
		
		System.out.println("Creating next generation... Using directory: " + newGeneration);
		
		try {
			// Open for writing
			FileOutputStream outputStream = new FileOutputStream(new File(newGeneration), false);
			FileChannel fileChannel = outputStream.getChannel();
			
			System.out.println("File Channel opened for write. Attempting to aquire lock (in blocking mode)...");
			FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false); // Lock whole file... No shared lock...
			System.out.println("Lock aquired... Preparing to write...");
			System.out.println("Lock is shared: " + lock.isShared());
			
			// Write new generation in file
			for(int i = 0; i < POPULATION_COUNT; ++i) {
				fileChannel.write(ByteBuffer.
						wrap(nextGeneration[i].toString().getBytes()));
			}
			
			outputStream.close(); // Close stream (also closes associated channel)... Also it releases the lock
			System.out.println("Population creation complete...");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method retrieves the assigned chromosome
	 * 
	 * @return	a <code>Individual</code> object
	 */
	public Individual getAssignedIndividual() {
		return assignedIndividual;
	}
	
	/**
	 * Method will check if the book keeper instance has 
	 * managed to get an unassigned individual
	 * 
	 * @return a <code>boolean</code> value that flags 
	 * 			results
	 */
	public boolean isAssignedChromosome() {
		return (assignedIndividual != null);
	}
	
	/**
	 * Method will retrieve the list of all individuals in the 
	 * lastest generation file.
	 * 
	 * @return
	 */
	public Individual[] getAllIndividuals() {
		
		Individual[] individuals = new Individual[POPULATION_COUNT];
		
		try {
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(assignedGeneration);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"); // Open in read-write mode
			FileChannel fileChannel = randomAccessFile.getChannel(); // Get fiel channel associated
						
			System.out.println("File Channel opened for read. Attempting to aquire lock (in blocking mode)...");
			FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false);
			System.out.println("Lock aquired... Preparing to read...");
			System.out.println("Lock is shared: " + lock.isShared());
						
			byte[] byteArray = new byte[(int) randomAccessFile.length()]; // Allocate space for file data
			ByteBuffer buffer = ByteBuffer.wrap(byteArray); // Put data into a buffer
			fileChannel.read(buffer); // Read data from channel into buffer
						
			InputStream inputStream = new ByteArrayInputStream(buffer.array()); // Put buffer into input stream
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // Treat it like a normal file
			String line = bufferedReader.readLine(); // Read a line to start off
			
			// Go to the end of the file
			while(line != null) {
				String[] tokens = line.split("\\s+"); // Split on any number of whitespace
				
				// Make sure we got tokens and are of sufficient length
				if(tokens != null && tokens.length == SCORE_ASSIGNED) {
					
				}
			}
 			
			fileChannel.position(0); // Set at start of the file
			
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return individuals;
	}
}
