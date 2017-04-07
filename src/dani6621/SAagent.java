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
public class SAAgent extends TeamClient {
	
	/**
	 * For easier debugging using graphical reperesentation of nodes and 
	 * path finding
	 */
	private static final boolean DEBUG_MODE = false;
	
	private static final boolean TRAINING_MODE = false;
	
	/**
	 * Number of retries at forming contingency plan
	 * when graph search fails
	 */
	private static final int MAX_RETRIES = 3;
	
	/**
	 * Constant will delimit whether agent can build a base
	 */
	private static double minimum_base_purchase_distance = 400.0;
	
	/**
	 * Constant that will add a multiple to ship speed to try and
	 * score more points
	 */
	private static double speed_multiplier = 1;
	
	/**
	 * Constant that will prioritize asteroids that are closer to the
	 * direction the ship is already heading
	 */
	private static double angle_weight = 1;
	
	/**
	 * Amount of time to wait before creating a new map
	 */
	private static final int NEW_MAP_TIMESTEP = 15;
	
	/**
	 * Number of clusters to use in the k-means clustering algorithm
	 */
	//private static final int K_CLUSTERS = 1;
	
	/**
	 * Number of timesteps to use before performing K-means clustering
	 */
	//private static final int CLUSTER_TIMESTEPS = 500;
	
	/**
	 * Cluster object that will be used for filtering target asteroids to
	 * control agent movements such that it moves to the highest density
	 * regions of the map.
	 */
	//private Cluster targetCluster;
	
	
    /**
     * Represents how agent will perceive world state. You can
     * think of this as the percept of the agent.
     */
    private SAWorldState knowledge;
    
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
    private SABookKeeper bookKeeper;
    
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
        perceive(space, ship);
        
     // Draw A* Path with debug (Got dead code warning, but it's intended)
        if(DEBUG_MODE && navigator.getCopyPath() != null) {
        	for(GraphSearchNode node : navigator.getCopyPath()) {
            	graphicsToAdd.add(new StarGraphics(2, Color.YELLOW, node.node.position));
            }
        }
        
        /*  - Code for K-Means Clustering
        if(space.getCurrentTimestep()%CLUSTER_TIMESTEPS == 0){   	
        	double dispersion = Double.MAX_VALUE;
        	//Five random restarts.  This value is arbitrarily selected.
        	for(int i = 0;i < 3;i++){
        		Cluster testCluster = knowledge.kmeansClustering(K_CLUSTERS);
        		double testDispersion = testCluster.calculateDispersion(space);
        		if(testDispersion < dispersion){
        			targetCluster = new Cluster(testCluster);
        			dispersion = testDispersion;
        		}
        	}
        }*/
        
        if(knowledge.getCurrentEnergy() < SAWorldState.LOW_ENERGY) { // Get energy when low
            AbstractObject source = knowledge.getClosestEnergySource(unapproachableObject);

            if(source == null) { // Didn't find a source
            	newAction = new DoNothingAction(); // Prevent ship from killing self
            }
            else { // Otherwise go to the energy source
            	
            	// Replan route
            	if(space.getCurrentTimestep() % NEW_MAP_TIMESTEP == 0) {
            		
            		for(int i = 0; ; ++i) { // Allow contingnecy plan to form when failure occurs
                		try {
                			navigator.generateAStarPath(space, knowledge, ship, source, 
									knowledge.getObstaclesExceptTeamBase());
                		}
                		catch(Navigator.NavigationFailureException e) { // After a number of retries give up
                			
                			if(i < MAX_RETRIES) {
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
            	
            	newAction = navigator.retrieveNavigationAction(space, knowledge, ship);
            	
            	if(newAction instanceof DoNothingAction) { // Basically search doesn't yield solution so simply go straight to source!
            		source = knowledge.getClosestEnergySource();
            		newAction = new MoveAction(space, ship.getPosition(), source.getPosition(), 
            				knowledge.calculateInterceptVelocity(source));
            	}
            }
        }
        else if(ship.getResources().getTotal() > SAWorldState.FULL_CARGO) { // Detect full cargo
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
            	
            	newAction = navigator.retrieveNavigationAction(space, knowledge, ship);
            	
            	if(newAction instanceof DoNothingAction) { // If no solution found, go to base at all costs... (Dumb way)
            		closestBase = knowledge.getClosestFriendlyBase();
            		newAction = new MoveAction(space, ship.getPosition(), closestBase.getPosition(), 
            				knowledge.calculateInterceptVelocity(closestBase));
            	}
            }
        }
        else { // Perform asteroid mining
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
                
            newAction = navigator.retrieveNavigationAction(space, knowledge, ship);
        }
        
        return newAction;
    }

    /**
     * Allows agent to populate the <code>WorldState</code>
     * with data about its percepts
     *
     * @param space the object that contains environment agent will perceive
     */
    private void perceive(Toroidal2DPhysics space, Ship ship) {
        knowledge = new SAWorldState(space, ship);
        knowledge.SPEED_MULTIPLIER = speed_multiplier;
        knowledge.ANGLE_WEIGHT = angle_weight;
    }
    
    /**
     * Cleanup and other operations when movement phase ends
     */
    @Override
    public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
    	unapproachableObject.clear(); // Clear the objects for the next timestep, the object could be reachable
    }
    
    /**
     * This will initialize any pieces of data the agent needs before the game 
     * starts. Also calls the SABookKeeper constructor.
     */
    @Override
    public void initialize(Toroidal2DPhysics space) {
    	
    	// Create navigation and track list of objects that could not be approached
    	navigator = new Navigator(DEBUG_MODE);
    	unapproachableObject = new HashMap<UUID, AbstractObject>();
    	graphicsToAdd = new ArrayList<SpacewarGraphics>();
    	
    	if(TRAINING_MODE) {
	    	// Simulated annealing bookkeper
	    	bookKeeper = new SABookKeeper(); // Call initialize function which reads file
	    	minimum_base_purchase_distance = bookKeeper.getThreshold();
	    	speed_multiplier = bookKeeper.getSpeedMultiplier();
	    	angle_weight = bookKeeper.getAngleWeight();
	    	
	    	System.out.println("Distance Threshold is: " + minimum_base_purchase_distance);
	    	System.out.println("Speed Multiplier is: " +speed_multiplier);
	    	System.out.println("Angle Weight is: " + angle_weight);
    	}
    	else {
    		minimum_base_purchase_distance = 517.0;
    		speed_multiplier = 1.513;
    		angle_weight = 3.367;
    		System.out.println("Non-training mode...");
    		System.out.println("Distance Threshold is: " + minimum_base_purchase_distance);
	    	System.out.println("Speed Multiplier is: " +speed_multiplier);
	    	System.out.println("Angle Weight is: " + angle_weight);
    	}
    	
    	
    }
    
    /**
     * Method will perform set of operations at game shutdown...
     * Basically, the genetic algorithm cleanup and calculations
     */

    @Override
    public void shutDown(Toroidal2DPhysics space) {
    	if(TRAINING_MODE) {
    		double totalScore = 0;
        	
        	for(ImmutableTeamInfo info : space.getTeamInfo()) {
        		if(info.getTeamName().equals("Padawan Daniel and Flood")) {
        			totalScore = info.getScore();
        		}
        	}
        	
        	//Update the simulated annealing file
        	bookKeeper.assignFitness(totalScore);
    	}
    }

    /**
     * 
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
     * minimum distance away from all distances.
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

                    if (minDist > minimum_base_purchase_distance) { // If the minimum distance is larger than constant then purchase a base
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
