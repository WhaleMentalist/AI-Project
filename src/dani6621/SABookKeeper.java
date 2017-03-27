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

import dani6621.Individual;
import dani6621.IndividualFactory;
import dani6621.Population;
import dani6621.Utility;




/**
 * Class is designed to relevant data to run simulated annealing.  In this particular version,
 * the simulated annealing seeks to find the optimal value for the minimum distance required from 
 * and old base to construct a new base.
 * 
 * This class creates a new directory in the current working directory and a .txt file
 * that stores all simulations performed so far.  Within the .txt file, there are 4 possible
 * columns: the base_build_threshold, the temperature, the score of the simulation for that
 * particular threshold, and a tag (B) to indicate if the threshold is the current threshold.
 * 
 * At the end of each simulation, a new distance threshold is created an appended (along
 * with the temperature of the next simulation) to the end of the .txt file so that the 
 * next simulation can pick up right where the previous one left off.  When the SABookKeeper
 * class is initalized, the program will refer to this text and initialize its relevant data
 * member to the most recent parameters
 * 
 * If a threshold produces a better game score than the last best threshold, then this move
 * is accepted.  The next threshold value is determined by adding (or subtracting) a random number
 * between -80 and 80 to the last best threshold
 * 
 * The cooling schedule is calculated as follows
 * Temperature = Temperature * 0.95
 * 
 * Inferior moves are accepting if the following condition is met:
 * Random Number < exp((current Threshold - best Threshold) / temperature)
 * 
 * The initial temperature was set at 1000.  This means a threshold that scores 1000 less than
 * the last best score will be accepted with ~36% probability
 *
 */
public class SABookKeeper {
	
	/**
	 * The file extension for knowledge files
	 */
	private static final String EXTENSION = ".txt";
	
	/**
	 * Variable holds name of directory containing knowledge files
	 */
	private static final String KNOWLEDGE_DIRECTORY_NAME = "/SimulatedAnnealing/";
	
	/**
	 * The name delimiting each knowledge file
	 */
	private static final String KNOWLEDGE_FILE_BASE_NAME = "generation";
		
	/**
	 * Holds the path to file holding assigned generation
	 */
	private String assignedGeneration;
	
	/**
	 * Threshold value that we are solving for in simulated annealing
	 */
	public int base_build_threshold;
	
	/**
	 * Temperature value for simulated annealing
	 */
	private double temperature;
	
	/**
	 * Last accepted threshold value.  Somewhat of a misnomer...
	 */
	private int best_threshold;
	
	/**
	 * The score produced by the last accepted threshold
	 */
	private double best_score;

	
	/**
	 * Basic constructor
	 */
	public SABookKeeper() {
		assignedGeneration = "";
		base_build_threshold = -1;
		temperature = 1000;
		best_threshold = 0;
		best_score = -1;
		
		initialize(); // Always run initialization to check existance and/or prepare file structure
	}
	
	/**
	 * Method creates necessary directory and initial population 
	 * if it is needed. It will also find the latest generation file to
	 * start assigning an individual to the program.
	 */
	private void initialize() {
		
		//File Flag
		boolean fileFlag = false;
		
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
			
			// If we find the file
			if(fileName.contains(KNOWLEDGE_FILE_BASE_NAME)) {		
				fileFlag = true;
			}
		}
		
		assignedGeneration = new String(projectPath.toString() + KNOWLEDGE_DIRECTORY_NAME + KNOWLEDGE_FILE_BASE_NAME + 
				EXTENSION);
		
		// If we never found a file, then we need to initialize the value
		if(!fileFlag) {
			
			System.out.println("No population file found... Randomly assigning ...");
			
			//Store file path

			try {
				// Open for writing
				FileOutputStream outputStream = new FileOutputStream(new File(assignedGeneration), false);
				FileChannel fileChannel = outputStream.getChannel();
								
				//Create initial round 0 dummy round
				//B tag indicates best round so far for comparison
				String gen0 = "0 0 -1 B\n";
				fileChannel.write(ByteBuffer.wrap(gen0.getBytes()));
	
				//Create intial random values
				//Temperature always starts at 1000
				int base_threshold = Utility.randomInteger(50, 1000);
				base_build_threshold = base_threshold;
				String gen1 = base_threshold + " 1000.0";
				
				fileChannel.write(ByteBuffer.wrap(gen1.getBytes()));
				
				outputStream.close(); // Close stream (also closes associated channel)... Also it releases the lock
				System.out.println("Intial value set...");
	
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else { // Found population file, create next iteration
			System.out.println("File found: " + assignedGeneration);
		
			//Read last values from the file
			readNextRound();		
		}
	}
	
	/**
	 * Private member method that will look at SimulatedAnnealing/generations.txt
	 * and find the most recently accepted threshold value (as indicated by the lowest
	 * B tag in the .txt file), the threshold value for the next simulation, and the 
	 * current temperature of the simulation.
	 */
	private void readNextRound(){
		
		try {
			File file = new File(assignedGeneration);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"); // Open in read-write mode
			FileChannel fileChannel = randomAccessFile.getChannel(); // Get file channel associated
			

			byte[] byteArray = new byte[(int) randomAccessFile.length()]; // Allocate space for file data
			ByteBuffer buffer = ByteBuffer.wrap(byteArray); // Put data into a buffer
			fileChannel.read(buffer); // Read data from channel into buffer
			
			InputStream inputStream = new ByteArrayInputStream(buffer.array()); // Put buffer into input stream
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // Treat it like a normal file
			String line = bufferedReader.readLine(); // Read a line to start off
			fileChannel.position(0); // Set at start of the file
			
			boolean sentinel = true;
			
			// Read each line (i.e a individual) and find first instance of an unassigned
			while(sentinel) {
				
				String nextLine = bufferedReader.readLine();
				String[] tokens = line.split("\\s+"); // Split on any number of whitespace

				//Find best values.  These values will be overridden in there is a lower
				//line with a B tag.
				if(tokens.length>3){
					best_threshold = Integer.parseInt(tokens[0]);
					best_score = Double.parseDouble(tokens[2]);
				}
						
				//null indicates the end of the file
				//Order is BASE_BUILD_THRESHOLD, TEMPERATURE, SCORE, TAG
				//If the next line is null, then we have our initial new value		
				if(nextLine == null){
					
					//Read in the threshold value from last round.
					//A new round is created at the end of each evaluation
					//call
					base_build_threshold = Integer.parseInt(tokens[0]);
					temperature = Double.parseDouble(tokens[1]);					
					sentinel = false; //breaks loop	
				}
				line = nextLine; //move to next line
			}
			//Housekeeping
			bufferedReader.close();
			inputStream.close();
			randomAccessFile.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}			
		
	}
	
	
	/**
	 * Method will assign a fitness to the current threshold and temperature
	 * This is appended to the end of the file.  Also updates the temperature
	 * via the cooling schedule, decides whether to "accept" the most recent 
	 * threhold as the best, and generates a new threshold value for the next
	 * iteration.
	 * 
	 * @param score	the score the agent received at the end of the game
	 */
	public void assignFitness(double score) {
		
		try {
			// This is just a convention you have to follow if you want to make read and write synchronized
			//open output stream in append mode.
			FileOutputStream outputStream = new FileOutputStream(new File(assignedGeneration), true);
			FileChannel fileChannel = outputStream.getChannel();
			
			//Determine if the solution has improved our best
			if(score > best_score){
				
				String add = " "+score+" B\n";
				best_score = score;
				best_threshold = base_build_threshold;
				
				fileChannel.write(ByteBuffer.wrap(add.getBytes()));
			}
			
			//If score isn't better, then we generate a new move with a probability
			//determined by the temperature
			else{
				double guess = Math.random();
				double cutoff = Math.exp((score-best_score)/temperature);
				//If Math.random (0-1) is LESS THAN our energy change, then we 
				//make the move, otherwise, we keep our previous best.  Note that 
				//as score-best_score increases, cutoff decreases, thus making moves
				//less probable.  We can also see that decreasing temperature will
				//have the exact same effect.
				if(guess<cutoff){
					String add = " "+score+" B\n";
					best_score = score;					
					fileChannel.write(ByteBuffer.wrap(add.getBytes()));			
				}
				else{
					String add = " "+score+"\n";
					fileChannel.write(ByteBuffer.wrap(add.getBytes()));					
				}
			}
			
			//Generate a new threshold based on our best value and decrement the
			//temperature according to the cooling schedule. Here, we will have
			//the cooling schedule be T *= .95.  Write these values to the file
			//Threshold can be in the interval of +/- 80 timesteps from the 
			//best threshold.  Each value within this interval is distributed 
			//according to a uniform distribution.
			int newThreshold = best_threshold + Utility.randomInteger(-80,80);
			if(newThreshold<0){
				newThreshold = 1;
			}
			temperature *= .95;
			
			String nextGen = newThreshold + " " + temperature;
			fileChannel.write(ByteBuffer.wrap(nextGen.getBytes()));			
			outputStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	
	/**
	 * getter method for the current threshold
	 * @return threshold
	 */
	public int getThreshold(){
		return base_build_threshold;
	}
	

}