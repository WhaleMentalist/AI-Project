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
 * Class is designed to hold data on chromosomes, create an initial 
 * population, generate new generations (selection, crossover, and mutation),
 * assign chromosomes to agent, regulate access to files containing chromosomes 
 * among processes, and keep information of chromosomes updated.
 *
 */
public class ChromosomeBookKeeper {
	
	/**
	 * This is the amount of chromosomes that will be produced 
	 * in each generation
	 */
	public static final int POPULATION_COUNT = 100;
	
	/**
	 * Constant used to ensure proper chromosome made in factory
	 */
	private static final String ASTEROID_COLLECTOR_STRING = "ASTEROIDCOLLECTOR";
	
	/**
	 * Variable will help cut extension off the file name
	 */
	private static final int EXTENSION_CUTOFF = 4;
	
	/**
	 * Variable holds name of directory containing knowledge files
	 */
	private static final String KNOWLEDGE_DIRECTORY_NAME = "/knowledge/";
	
	/**
	 * The name delimiting each knowledge file
	 */
	private static final String KNOWLEDGE_FILE_BASE_NAME = "generation";
	
	/**
	 * The file extension for knowlodge files
	 */
	private static final String EXTENSION = ".txt";
	
	/**
	 * Number of tokens for unassigned chromosome
	 */
	private static final int UNASSIGNED = 10;
	
	/**
	 * Holds the path to file holding latest generation
	 */
	private String latestGeneration;
	
	/**
	 * Hold the chromosome that was assigned. It will correspond to a 
	 * line within the file
	 */
	private int assignedChromosomeID;
	
	/**
	 * The assigned chromosome
	 */
	private Chromosome assignedChromosome;
	
	/**
	 * Basic constructor
	 */
	public ChromosomeBookKeeper() {
		latestGeneration = "";
		assignedChromosomeID = -1;
		assignedChromosome = null;
		initialize(); // Always run initialization to check existance and/or prepare file structure
		assignChromosome(); // Attempt to assign chromosome
	}
	
	/**
	 * Method creates neccessary directory and initial population 
	 * if it is needed. It will also find the latest generation file to
	 * start assigning a chromosome to the program.
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
		int latestGenerationNumber = -1; // Hold latest generation found
		
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
				if(generationNumber > latestGenerationNumber) {
					latestGenerationNumber = generationNumber;
					latestGeneration = filePath; // Set data member to value
				}
			}
		}
		
		// If we never found a file, then we need to initialize the initial population
		if(latestGeneration.equals("")) {
			
			System.out.println("No population file found... Creating initial population...");
			
			// Create path to initial file we will create - notice it uses '0' for first generation
			latestGeneration = new String(projectPath.toString() + KNOWLEDGE_DIRECTORY_NAME + KNOWLEDGE_FILE_BASE_NAME + "0" +
											EXTENSION);
			
			try {
				// Open for writing
				FileOutputStream outputStream = new FileOutputStream(new File(latestGeneration), false);
				FileChannel fileChannel = outputStream.getChannel();
				
				System.out.println("File Channel opened for write. Attempting to aquire lock (in blocking mode)...");
				FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false); // Lock whole file... No shared lock...
				System.out.println("Lock aquired... Preparing to write...");
				System.out.println("Lock is shared: " + lock.isShared());
				
				for(int i = 0; i < POPULATION_COUNT; ++i) {
					fileChannel.write(ByteBuffer.
							wrap(ChromosomeFactory.createChromosome(ASTEROID_COLLECTOR_STRING).
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
			System.out.println("File found: " + latestGeneration);
		}
	}
	
	/**
	 * Method will assign a chromosome by finding first unassigned 
	 * instance in the 'latestGeneration' file. If it does it will
	 * need to rewrite whole file in order to mark chromosome as 
	 * used. This has to do with how files work (i.e no programming langauage could
	 * fix this problem). It shouldn't cause too much delay, as the file sizes are 
	 * small (100 lines at most).
	 */
	private void assignChromosome() {
		
		try {
			
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(latestGeneration);
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
			
			int chromosomeCounter = 1; // Counter to track chromosomes
			
			// Read each line (i.e a chromosome) and find first instance of an unassigned
			while(line != null) {
				
				String[] tokens = line.split("\\s+"); // Split on any number of whitespace
				
				// Check if chromosome is unassigned and if the chromosome hasn't already been assigned
				if(tokens != null && tokens.length == UNASSIGNED && assignedChromosomeID < 1) {
					line += " A\n"; // Append an assigned token at end of 'line'
					assignedChromosomeID = chromosomeCounter;
					
					// Construct the 'Chromosome' object from file data
					assignedChromosome = new Chromosome(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[1]),
													Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]),
													Integer.parseInt(tokens[4]), Double.parseDouble(tokens[5]), 
													Double.parseDouble(tokens[6]), Double.parseDouble(tokens[7]),
													Integer.parseInt(tokens[8]), Double.parseDouble(tokens[9]));
				}
				else { // Got to append a return line
					line += "\n";
				}
				
				fileChannel.write(ByteBuffer.wrap(line.getBytes()));
				line = bufferedReader.readLine();
				++chromosomeCounter;
			}
			
			System.out.println("Chromosome assignment: " + assignedChromosomeID);
			
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method will assign a fitness to the <code>assignedChromosome</code>.
	 * This will require the whole file to be re-written.
	 * 
	 * @param score	the score the agent recieved at the end of the game
	 */
	public void assignFitness(double score) {
		try {
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(latestGeneration);
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
			
			int currentChromosome = 1;
			
			while(line != null) {
				
				if(currentChromosome == assignedChromosomeID) {
					line += " " + score + "\n";
				}
				else { // Got to append a return line
					line += "\n";
				}
				
				fileChannel.write(ByteBuffer.wrap(line.getBytes()));
				line = bufferedReader.readLine();
				++currentChromosome;
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
	 * Method will check the <code>latestGeneration</code> file and 
	 * if the file has all the chromosomes assigned a fitness score
	 * then the algorithm will perform selection, mutation, and 
	 * crossover.
	 */
	public void checkLatestGeneration() {
		
		try {
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(latestGeneration);
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
			
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method retrieves the assigned chromosome
	 * 
	 * @return	a <code>Chromosome</code> object
	 */
	public Chromosome getAssignedChromosome() {
		return assignedChromosome;
	}
	
	/**
	 * Method will check if the book keeper instance has 
	 * managed to get an unassigned chromosome
	 * 
	 * @return a <code>boolean</code> value that flags 
	 * 			results
	 */
	public boolean isAssignedChromosome() {
		return (assignedChromosome == null);
	}
	
	/**
	 * 
	 * @return
	 */
	public AbstractChromosome[] getAllChromosome() {
		
		try {
			// This is just a convention you have to follow if you want to make read and write synchronized
			File file = new File(latestGeneration);
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
			
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
