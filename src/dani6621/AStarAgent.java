package dani6621;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
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
public class AStarAgent extends TeamClient {
	
	/**
	 * Number of retries at forming contingency plan
	 * when graph search fails
	 */
	private static final int MAX_RETRIES = 3;
	
	/**
	 * Constant will delimit whether agent can build a base
	 */
	private static final double MINIMUM_BASE_PURCHASE_DISTANCE = 400.0;
	
	/**
	 * Amount of time to wait before creating a new map
	 */
	private static final int NEW_MAP_TIMESTEP = 15;

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
    private Chromosome chromosome;
    
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
        
        if(knowledge.getCurrentEnergy() < WorldState.LOW_ENERGY) { // Get energy when low
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
        else if(ship.getResources().getTotal() > WorldState.FULL_CARGO) { // Detect full cargo
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
        knowledge = new WorldState(space, ship);
    }
    
    /**
     * Cleanup and other operations when movement phase ends
     */
    @Override
    public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
    	unapproachableObject.clear(); // Clear the objects for the next timestep, the object could be reachable
    }
    
    /**
     * This will initialize any pieces of data teh agent needs before the game 
     * starts. An example is the chromosome or the navigator.
     */
    @Override
    public void initialize(Toroidal2DPhysics space) {
    	
    	// Create navigation and track list of objects that could not be approached
    	navigator = new Navigator();
    	unapproachableObject = new HashMap<UUID, AbstractObject>();
    	
    	ChromosomeBookeeper bookeeper = new ChromosomeBookeeper(); // Need to issue a request for data
    	chromosome = bookeeper.getAssignedChromosome(); // Retrieve the assigned chromosome
    	chromosome = null;
    	
    	if(chromosome == null) {
    		System.exit(0);
    	}
    }
    
    /**
     * 
     */
    @Override
    public void shutDown(Toroidal2DPhysics space) {

    }
    
    /**
     * 
     */
    @Override
    public Set<SpacewarGraphics> getGraphics() {
    	HashSet<SpacewarGraphics> graphics = new HashSet<SpacewarGraphics>();
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

                    if (minDist > MINIMUM_BASE_PURCHASE_DISTANCE) { // If the minimum distance is larger than constant then purchase a base
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
