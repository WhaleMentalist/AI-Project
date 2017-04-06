package dani6621;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dani6621.GraphSearch.GraphSearchNode;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.ImmutableTeamInfo;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.graphics.StarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * The agent that will control the ship. It has a function that
 * will select the action the agent performs based on contents of
 * <code>WorldState</code> reference data member
 */
public class GeneticAlgorithmAgent extends TeamClient {
	
	/**
	 * Toggle value in order to see graphics on screen to help with debugging
	 */
	private static final boolean DEBUG_MODE = false;
	
	/**
	 * Toggle whether to activate agent in training mode or normal mode
	 */
	private static final boolean TRAINING_MODE = false;
	
	/**
	 * Error code for lack of chromosome assignment
	 */
	private static final int CHROMOSOME_ASSIGNMENT_FAILURE = -1;
	
	/**
	 * Number of retries at forming contingency plan
	 * when graph search fails
	 */
	private static final int MAX_RETRIES = 5;
	
	/**
	 * Amount of time to wait before creating a new map
	 */
	private static final int NEW_MAP_TIMESTEP = 10;

    /**
     * Represents how agent will perceive world state. You can
     * think of this as the percept of the agent.
     */
    private WorldState knowledge;
    
    /**
     * Holds the current objective of the agent as 
     * objective changes and the dynamics of the game 
     * also change
     */
    private Navigator navigator;
    
    /**
     * Holds the data that will govern the behavior of the 
     * agent. It has constants that guide the agent's reaction to the environment.
     * Ideally, the genetic algorithm will produce the optimal chromosome after
     * enough iterations (generations).
     */
    private IndividualBookKeeper bookKeeper;
    
    /**
     * Data member will contain objects that could not be 
     * path found by the graph search at the timestep. It will
     * be useful when forming a new plan when errors occur.
     * 
     * NOTE: For now, the data member will belong to this class.
     * 			This will change later. It will probably belong to
     * 			a knowledge representation for multi-agent
     * 			coordination later. Each agent would have their own
     * 			list of objects.
     */
    private Map<UUID, AbstractObject> unapproachableObject;
    
    /**
     * The individual containing the chromosomes that 
     * govern the agent's actions
     */
    private Individual assignedIndividual;
    
    /**
     * Debug navigation
     */
    private List<SpacewarGraphics> graphicsToAdd;

    /**
     * Assigns ships to asteroids and beacons, as described above
     */
    public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
                                                      Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

        for (AbstractObject actionable : actionableObjects) { // Find ships and assign each an action to perform
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                actions.put(actionable.getId(), getReflexAgentAction(space, ship));
            }
            else { // Bases won't do anything for now
                actions.put(actionable.getId(), new DoNothingAction());
            }
        }
        return actions;
    }
    
    /**
     * Function retrieve an action for the agent to perform based on the state of 
     * the <code>knowledge</code> data member
     * 
     * @param space the space containing the objects and the ship
     * @param ship the ship that will be assigned the action
     * @return an action for the ship to perform
     */
    public AbstractAction getReflexAgentAction(Toroidal2DPhysics space, Ship ship) {
        AbstractAction newAction = new DoNothingAction();
        perceive(space, ship, assignedIndividual);
        
        // Draw A* Path with debug (Got dead code warning, but it's intended)
        if(DEBUG_MODE && navigator.getCopyPath() != null) {
        	for(GraphSearchNode node : navigator.getCopyPath()) {
            	graphicsToAdd.add(new StarGraphics(2, Color.YELLOW, node.node.position));
            }
        }
        
        if(knowledge.getCurrentEnergy() < knowledge.LOW_ENERGY) { // Get energy when low
            
        	// Allow rendezvous behavior when ship has sufficient cargohold
        	if(ship.getResources().getTotal() > knowledge.RENDEZVOUS_CARGO_HOLD_CAPACITY && 
        			space.findShortestDistance(ship.getPosition(), knowledge.getClosestFriendlyBase().getPosition()) <
        						knowledge.MINIMAL_RENDEZVOUS_DISTANCE) { // Might be better off charging at base
        		returnResources(space, ship);
        	}
        	else { // Don't have enough resources to warrant return to base, simply find closest energy source
        		retrieveEnergy(space, ship);
        	}
        	
        	newAction = navigator.retrieveNavigationAction(space, knowledge, ship);
             	
            if(newAction instanceof DoNothingAction) { // Basically search doesn't yield solution so simply go straight to source!
            	AbstractObject source = knowledge.getClosestEnergySource();
            	newAction = new MoveAction(space, ship.getPosition(), source.getPosition(), 
            				knowledge.calculateInterceptVelocity(source));
            }
        }
        else if(ship.getResources().getTotal() > knowledge.FULL_CARGO) { // Detect full cargo
            
            returnResources(space, ship);	
            newAction = navigator.retrieveNavigationAction(space, knowledge, ship);
            	
            if(newAction instanceof DoNothingAction) { // If no solution found, go to base at all costs... (Dumb way)
            	Base closestBase = knowledge.getClosestFriendlyBase();
            	newAction = new MoveAction(space, ship.getPosition(), closestBase.getPosition(), 
            			knowledge.calculateInterceptVelocity(closestBase));
            }
        }
        else { // Perform asteroid mining
            asteroidMine(space, ship);
            newAction = navigator.retrieveNavigationAction(space, knowledge, ship);
        }
        
        return newAction;
    }
    
    /**
     * Method will compute asteroid for ship to go after
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     */
    private void asteroidMine(Toroidal2DPhysics space, Ship ship) {
    	 // Find closest asteroid to mine
        Asteroid closestAsteroid = knowledge.getMostEfficientMinableAsteroid(unapproachableObject);
        
        if(closestAsteroid != null) { // If we could find one cancel any move to random locations actions
        		
        	// Replan route
            if(space.getCurrentTimestep() % NEW_MAP_TIMESTEP == 0) {
            		
            	for(int i = 0; ; ++i) { // Allow contingnecy plan to form when failure occurs
               		try {
                		navigator.generateAStarPath(space, knowledge, ship, closestAsteroid, 
								knowledge.getObstacles());
                	}
                	catch(Navigator.NavigationFailureException e) { // After a number of retries give up
                			
                		if(i < MAX_RETRIES) {
                			if(closestAsteroid != null)
                				unapproachableObject.put(closestAsteroid.getId(), closestAsteroid); // Add as unsolvable
                			closestAsteroid = knowledge.getMostEfficientMinableAsteroid(unapproachableObject);
                			continue;
                		}
                		else {
                			break;
                		}
                	}
               		break;
                }
            }
        }
    }
    
    /**
     * Method will compute base for ship to return resources 
     * 
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * 
     */
    private void returnResources(Toroidal2DPhysics space, Ship ship) {
    	Base closestBase = knowledge.getClosestFriendlyBase(unapproachableObject);
        
        if(closestBase != null) { // Goto base that was found
        		
        	// Replan route
            if(space.getCurrentTimestep() % NEW_MAP_TIMESTEP == 0) {
            		
            	for(int i = 0; ; ++i) { // Allow contingnecy plan to form when failure occurs
                	try {
                		navigator.generateAStarPath(space, knowledge, ship, closestBase, 
                    			knowledge.getObstaclesExceptTeamBase());
                	}
                	catch(Navigator.NavigationFailureException e) { // After a number of retries give up
                			
                		if(i < MAX_RETRIES) {
                			if(closestBase != null)
                				unapproachableObject.put(closestBase.getId(), closestBase); // Add as unsolvable
                			closestBase = knowledge.getClosestFriendlyBase(unapproachableObject);
                			
                			if(closestBase != null) { // Might run out of bases to found, so check
                    			continue;
                			}
                			else { // Heh... We ran out of bases. Not much we can do besides performing another action
                				break;
                			}

                		}
                		else {
                			break;
                		}
                	}
                	break;
                }
            }
        }
    }
    
    /**
     * Method will issue command for ship to attain energy
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to the ship
     */
    private void retrieveEnergy(Toroidal2DPhysics space, Ship ship) {
    	AbstractObject source = knowledge.getClosestEnergySource(unapproachableObject);
        
        // Replan route
        if(space.getCurrentTimestep() % NEW_MAP_TIMESTEP == 0) {
        		
        	for(int i = 0; ; ++i) { // Allow contingnecy plan to form when failure occurs
            	try {
            		navigator.generateAStarPath(space, knowledge, ship, source, 
								knowledge.getObstaclesExceptTeamBase());
            	}
            	catch(Navigator.NavigationFailureException e) { // After a number of retries give up
            			
            		if(i < MAX_RETRIES) {
            			if(source != null)
            				unapproachableObject.put(source.getId(), source); // Add as unsolvable
            			source = knowledge.getClosestEnergySource(unapproachableObject); // Get new object
            			continue;
            		}
            		else {
                		break;
                	}
            	}
            	break;
            }
        }
    }

    /**
     * Allows agent to populate the <code>WorldState</code>
     * with data about its percepts
     *
     * @param space the object that contains environment agent will perceive
     * @param ship reference to ship
     * @param individual	the individual assigned to agent
     */
    private void perceive(Toroidal2DPhysics space, Ship ship, Individual individual) {
        knowledge = new WorldState(space, ship, individual);
    }
    
    /**
     * Cleanup and other operations when movement phase ends
     */
    @Override
    public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
    	if(space.getCurrentTimestep() % NEW_MAP_TIMESTEP == 0)
    		unapproachableObject.clear(); // Clear the objects for the next timestep, the object could be reachable
    }
    
    /**
     * This will initialize any pieces of data the agent needs before the game 
     * starts. An example is the chromosome or the navigator.
     */
    @Override
    public void initialize(Toroidal2DPhysics space) {
    	
    	// Create navigation and track list of objects that could not be approached
    	navigator = new Navigator(DEBUG_MODE);
    	unapproachableObject = new HashMap<UUID, AbstractObject>();
    	graphicsToAdd = new ArrayList<SpacewarGraphics>();
    	
    	if(TRAINING_MODE) {
    		// The 'IndividualBookKeeper' is much like a librarian with books
        	bookKeeper = new IndividualBookKeeper(); // Need to issue a request for data
        	
        	// If a chromosome was not assigned, simply terminate the program (i.e kill JVM)
        	if(!(bookKeeper.isAssignedChromosome())) {
        		System.exit(CHROMOSOME_ASSIGNMENT_FAILURE); // Terminate JVM process
        	}
        	
        	assignedIndividual = bookKeeper.getAssignedIndividual(); // Get the assigned individual
        	System.out.println(assignedIndividual.toString());
    	}
    	else { // Use best candidate from training... TODO: Replace with better one from generation 30...
    		System.out.println("Launching in non-training mode...");
    		assignedIndividual = new Individual(new AsteroidCollectorChromosome(2772, 4116, 
    				2.9380370693515476, 379.139308885403, 8492, 719.07326653848031));
    	}
    	
    }
    
    /**
     * Method will perform set of operations at game shutdown...
     * Basically, the genetic algorithm cleanup and calculations for 
     * fitness and generation creation
     */
    @Override
    public void shutDown(Toroidal2DPhysics space) {
    	if(TRAINING_MODE) {
    		double totalScore = 0.0;
        	
        	for(ImmutableTeamInfo info : space.getTeamInfo()) {
        		if(info.getTeamName().equals("Padawan Daniel and Flood")) {
        			totalScore = info.getScore();
        		}
        	}
        	bookKeeper.assignFitness(totalScore); // Assign fitness score to the assigned individual
        	bookKeeper.checkAssignedGeneration(); // Check if new generation needs to be created
    	}
    }
    
    /**
     * Draws the graphics on screen. Especially help when debugging...
     */
    @Override
    public Set<SpacewarGraphics> getGraphics() {
    	HashSet<SpacewarGraphics> graphics = new HashSet<SpacewarGraphics>();
    	
    	if(DEBUG_MODE) {
    		graphics.addAll(graphicsToAdd);
    		if(navigator.map.graphDrawing != null) {
    			graphics.addAll(navigator.map.graphDrawing);
    		}
    		graphicsToAdd.clear();
    	}
    		
		return graphics;
    }

    /**
     * Function will allow the ship to build bases once it is a certain
     * minimum distance away from all distances
     */
    @Override
    public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
                                                     Set<AbstractActionableObject> actionableObjects,
                                                     ResourcePile resourcesAvailable,
                                                     PurchaseCosts purchaseCosts) {

        HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();

        if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable)) {
            for (AbstractActionableObject actionableObject : actionableObjects) {
                if (actionableObject instanceof Ship) {
                    Ship ship = (Ship) actionableObject;
                    Set<Base> bases = space.getBases();

                    // Check distance from each base
                    double minDist = Double.MAX_VALUE;
                    for (Base base : bases) {
                        if (base.getTeamName().equalsIgnoreCase(getTeamName())) {
                            double distance = space.findShortestDistance(ship.getPosition(), base.getPosition());
                            if (distance < minDist) { // Set minimum distance if found
                                minDist = distance;
                            }
                        }
                    }

                    if (minDist > knowledge.BASE_BUILD_THRESHOLD) { // If the minimum distance is larger than constant then purchase a base
                        purchases.put(ship.getId(), PurchaseTypes.BASE);
                        break;
                    }
                }
            }
        }


        return purchases;
    }

    /**
     * The aggressive asteroid collector shoots if there is an enemy nearby!
     *
     * @param space the space containing objects and objects
     * @param actionableObjects the objects that can perform actions
     * @return map of object UUID to powerups
     */
    @Override
    public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
                                                           Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();
        return powerUps;
    }
}