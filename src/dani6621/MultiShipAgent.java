package dani6621;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dani6621.Navigator.NavigationFailureException;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Flag;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * Controls multiple ships using knowledge representation that maps ships to
 * assigned duties. 
 * 
 * @author dani6621
 *
 */
public class MultiShipAgent extends TeamClient {
	
	/**
	 * Used to help gather team ships
	 */
	public String TEAM_NAME;
	
	/**
	 * Establish when it is good time to start gathering flags...
	 * This allows agents to gather resources neccessary for base 
	 * building near alcove for maximum effect
	 */
	private static final int FLAG_GATHERING_TIME = 3000;
	
	/**
	 * Amount of time that must elapse before agent can replan navigation
	 */
	private static final int REPLAN_TIME_STEP = 30;
	
	/**
	 * Number of new plans that can form if search fails for navigation
	 */
	private static final int MAX_RETRIES = 3;
	
	/**
	 * Track if the program has initialized properly
	 */
	private boolean INITIALIZED = false;
	
	/**
	 * Flag if new ship was bought and needs to be assigned 
	 * navigator
	 */
	private boolean BOUGHT_SHIP = false;
	
	/**
	 * Debug mode for graphics
	 */
	public static final boolean DEBUG_MODE = false;
	
	/**
	 * Member will contain information pertaining to team actions
	 */
	private TeamKnowledge teamKnowledge;
	
	/**
	 * Member will contain world state information
	 */
	private WorldKnowledge knowledge;

	@Override
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
        knowledge = new WorldKnowledge(teamKnowledge, TEAM_NAME);
		
		if(!INITIALIZED) { // Initialize beginning ships with individual navigators
			Ship shipToken = null;
			for(Ship ship : knowledge.getTeamShips(space)) {
				teamKnowledge.assignShipToNavigator(ship, new Navigator(DEBUG_MODE));
				shipToken = ship;
			}
			teamKnowledge.assignBaseBuildingLocations(space, WorldKnowledge.getOtherTeamFlag(space, shipToken));
			INITIALIZED = true;
		}
		
		if(BOUGHT_SHIP) { // When ship is bought it must be assigned a navigator
			for(Ship ship : knowledge.getTeamShips(space)) {
				if(!(teamKnowledge.shipAssignedNavigator(ship))) {
					teamKnowledge.assignShipToNavigator(ship, new Navigator(DEBUG_MODE));
				}
			}
			BOUGHT_SHIP = false;
		}
		
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

        for (AbstractObject actionable : actionableObjects) { // Find ships and assign each an action to perform
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                actions.put(actionable.getId(), getShipAction(space, ship));
            }
            else { // Bases won't do anything for now
                actions.put(actionable.getId(), new DoNothingAction());
            }
        }
        
        
        return actions;
	}

	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		teamKnowledge.updateAssignments(space);
	}

	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		return null;
	}

	@Override
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, ResourcePile resourcesAvailable,
			PurchaseCosts purchaseCosts) {
		HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();
		Ship ship;
		
		// We can afford a base to purchase!
		if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable)) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if(actionableObject instanceof Ship) {
					ship = (Ship) actionableObject;
					if(teamKnowledge.getBaseBuilderUUID() != null && ship.getId().equals(teamKnowledge.getBaseBuilderUUID())) {
						if(space.findShortestDistance(ship.getPosition(), knowledge.getClosestBaseBuildingSite(space, ship)) 
								< WorldKnowledge.BASE_BUILD_THRESHOLD) {
							purchases.put(ship.getId(), PurchaseTypes.BASE);
							teamKnowledge.unassignBaseBuilder();
							System.out.println("Buying base");
						}
					}
				}
			}
		}
		
		// We can start buying ships after we have established convient bases
		if (purchaseCosts.canAfford(PurchaseTypes.SHIP, resourcesAvailable) && 
				knowledge.isBaseBuiltAtLocation(space, "Padawan_Daniel_and_Flood", teamKnowledge.getConvientBaseBuildingLocations()[0])
				&& knowledge.isBaseBuiltAtLocation(space, "Padawan_Daniel_and_Flood", teamKnowledge.getConvientBaseBuildingLocations()[1]) 
				&& knowledge.getTeamShips(space).size() < 5) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Base) {
					Base base = (Base) actionableObject;
					purchases.put(base.getId(), PurchaseTypes.SHIP);
					BOUGHT_SHIP = true;
					System.out.println("Buying ship!");
					break;
				}
			}
		}
		
		return purchases;
	}

	@Override
	public void initialize(Toroidal2DPhysics space) {
		teamKnowledge = new TeamKnowledge();
		TEAM_NAME = super.getTeamName();
		System.out.println(TEAM_NAME + " : initialized");
	}

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		return null;
	}
	
	/**
     * Function retrieve an action for the ship to perform based on the world 
     * state and the team state. If no action is found then the ship shall perform
     * a <code>DoNothingAction</code> as default
     * 
     * @param space the space containing the objects and the ship
     * @param ship the ship that will be assigned the action
     * @return an action for the ship to perform
     */
    public AbstractAction getShipAction(Toroidal2DPhysics space, Ship ship) {
        AbstractAction newAction = new DoNothingAction();
        
        if(teamKnowledge.getFlagCarrierUUID() == null && space.getCurrentTimestep() > FLAG_GATHERING_TIME) { // Need to assign flag carrier
        	Ship flagCarrier = knowledge.getFlagCarrier(space, ship);
        	if(flagCarrier != null) {
        		teamKnowledge.assignFlagCarrier(flagCarrier);
        	}
        }
        
        if(teamKnowledge.getBaseBuilderUUID() == null && space.getCurrentTimestep() > FLAG_GATHERING_TIME) { // Need to assign base builder
        	Ship baseBuilder = knowledge.getBaseBuilder(space, ship);
        	if(baseBuilder != null) {
        		teamKnowledge.assignBaseBuilder(ship);
        		System.out.println("Assigning base builder!");
        	}
        }
        
        // Flag Gatherer
        if(ship.getId().equals(teamKnowledge.getFlagCarrierUUID())) {
        	if(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) { // Low Energy State
            	retrieveEnergy(space, ship, knowledge);
                newAction = teamKnowledge.getTeamMemberAction(space, ship);
            }
        	else if(!(ship.isCarryingFlag())) { // Retrieving Flag State
        		retrieveFlag(space, ship, knowledge);
        		newAction = teamKnowledge.getTeamMemberAction(space, ship);
        		
        		if(newAction instanceof DoNothingAction) { // Algorithm couldn't form path to flag
            		transitToLocation(space, ship, knowledge, teamKnowledge.getConvientBaseBuildingLocations()[0]); // Let's just goto position near flag
            		newAction = teamKnowledge.getTeamMemberAction(space, ship);
            	}
        	}
        	else {
        		returnResources(space, ship, knowledge);
        		newAction = teamKnowledge.getTeamMemberAction(space, ship);
        	}
        }
        else if(ship.getId().equals(teamKnowledge.getBaseBuilderUUID())) { // Base Builder
        	if(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) { // Low Energy State
            	retrieveEnergy(space, ship, knowledge);
                newAction = teamKnowledge.getTeamMemberAction(space, ship);
            }
        	else { // Transit / Idle
        		transitBaseBuildingSite(space, ship, knowledge);
        		newAction = teamKnowledge.getTeamMemberAction(space, ship);
        	}
        }
        else { // Asteroid Gatherer
        	// Asteroid Gathering Component
            if(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) { // Low Energy State
            	retrieveEnergy(space, ship, knowledge);
                newAction = teamKnowledge.getTeamMemberAction(space, ship);
            }
            else if(ship.getResources().getTotal() > WorldKnowledge.RESOURCE_THRESHOLD) { // High Cargohold State
            	returnResources(space, ship, knowledge);
            	newAction = teamKnowledge.getTeamMemberAction(space, ship);
            }
            else { // Asteroid Gathering State
            	asteroidMine(space, ship, knowledge);
            	newAction = teamKnowledge.getTeamMemberAction(space, ship);
            	
            	if(newAction instanceof DoNothingAction) { // Algorithm couldn't find a suitable asteroid
            		retrieveEnergy(space, ship, knowledge); // Let's just get some energy to help
            		newAction = teamKnowledge.getTeamMemberAction(space, ship);
            	}
            }
        }
        return newAction;
    }
    
    /**
     * Method will compute asteroid for ship to go after
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * @param knowledge	the reference to world state
     */
    private void asteroidMine(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
        Set<UUID> failedAsteroids = new HashSet<UUID>(); // Maintain reference to asteroids that could not be approached
        Asteroid closestAsteroid = knowledge.getClosestAsteroid(space, ship, failedAsteroids); // Find closest asteroid to mine

        if(closestAsteroid != null) {
        	if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) { // Replan path at set time interval
        		for(int i = 0; ; ++i) {
        			try {
        				teamKnowledge.generateTeamMemberPath(space, ship, 
                    			closestAsteroid, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
        			}
        			catch(NavigationFailureException e) {
        				if(i < MAX_RETRIES) {
        					if(closestAsteroid != null) {
        						failedAsteroids.add(closestAsteroid.getId()); // Add asteroid to list of unapproachable
                				teamKnowledge.unassignAsteroidToShip(closestAsteroid); // Need to unassign asteroid in team knowledge
                				closestAsteroid = knowledge.getClosestAsteroid(space, ship, failedAsteroids); // Find closest asteroid to mine
                				teamKnowledge.generateTeamMemberPath(space, ship, 
                            			closestAsteroid, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
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
    
    private void retrieveEnergy(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
    	Set<UUID> failedSources = new HashSet<UUID>(); // Maintain reference to asteroids that could not be approached
        AbstractObject closestEnergySource = knowledge.getClosestEnergySource(space, ship, failedSources); // Find closest asteroid to mine

        if(closestEnergySource != null) {
        	if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) { // Replan path at set time interval
        		for(int i = 0; ; ++i) {
        			try {
        				teamKnowledge.generateTeamMemberPath(space, ship, 
        						closestEnergySource, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
        			}
        			catch(NavigationFailureException e) {
        				if(i < MAX_RETRIES) {
        					if(closestEnergySource != null) {
        						failedSources.add(closestEnergySource.getId()); // Add energy to list of unapproachable
                				teamKnowledge.unassignEnergyToShip(closestEnergySource); // Need to unassign energy in team knowledge
                				closestEnergySource = knowledge.getClosestEnergySource(space, ship, failedSources); // Find closest asteroid to mine
                				teamKnowledge.generateTeamMemberPath(space, ship, 
                						closestEnergySource, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
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
     * Function will provided actions that allow ship to get flag
     * 
     * @param space	a reference to space
     * @param ship	the ship going for the flag
     * @param knowledge	the world state
     */
    private void retrieveFlag(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
    	Flag closestFlag = WorldKnowledge.getOtherTeamFlag(space, ship);
    	
    	if(closestFlag != null) {
    		if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) {
    				try {
    					teamKnowledge.generateTeamMemberPath(space, ship, closestFlag, WorldKnowledge.getAllObstacles(space, ship));
    				}
    				catch(NavigationFailureException e) {

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
     * @param knowledge	the world state
     * 
     */
    private void returnResources(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
    	Set<UUID> failedBases = new HashSet<UUID>(); 
    	Base closestBase = knowledge.getClosestFriendlyBase(space, ship);
        
        if(closestBase != null) { // Goto base that was found
        	// Replan route
            if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) {
            	for(int i = 0; ; ++i) { // Allow contingnecy plan to form when failure occurs
                	try {
                		teamKnowledge.generateTeamMemberPath(space, ship, closestBase, 
                    			WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship));
                	}
                	catch(Navigator.NavigationFailureException e) { // After a number of retries give up
                		if(i < MAX_RETRIES) {
                			if(closestBase != null) {
                				// TODO: Figure out assignment...
                				failedBases.add(closestBase.getId()); // Add energy to list of unapproachable
                				teamKnowledge.generateTeamMemberPath(space, ship, 
                						closestBase, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
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
     * Function will form path to base build site
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * @param knowledge	the world state
     */
    private void transitBaseBuildingSite(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
    	Position position = knowledge.getClosestBaseBuildingSite(space, ship);
    	if(position != null) { // Goto base that was found
        	// Replan route
            if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) {
            	System.out.println("Going to build site");
                teamKnowledge.generateTeamMemberPath(space, ship, position, 
                    			WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship));
            }
        }
    }
    
    /**
     * Function will form path to location
     * 
     * @param space	a reference to space
     * @param ship	the reference to ship
     * @param knowledge	the world state
     * @param location 	the location ship wants to goto
     */
    private void transitToLocation(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge, Position location) {
    	// Replan route
        if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) {
            		teamKnowledge.generateTeamMemberPath(space, ship, location, 
                			WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship));
        }
    }
}