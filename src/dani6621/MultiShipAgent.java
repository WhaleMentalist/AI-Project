package dani6621;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.objects.resources.ResourceTypes;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

public class MultiShipAgent extends TeamClient {
	
	/**
	 * Hold the team name in simulation
	 */
	public static String TEAM_NAME;
	
	/**
	 * The amount of time before navigation needs to replan
	 */
	public static final int NAVIGATION_REPLAN_TIMESTEP = 20;
	
	/**
	 * The amount of time before the multi-agent planner replans
	 */
	public static final int REPLAN_MULTIAGENT_ACTIONS = 300;
	
	/**
	 * Flag the debug mode
	 */
	public static final boolean DEBUG_MODE = false;
	
	/**
	 * Helps to do first initialization of team member
	 * navigation
	 */
	private static boolean INITIALIZED = false;
	
	/**
	 * Flags if the next strategy phase in planner was executed
	 */
	private static boolean NEXT_PHASE_ISSUED = false;
	
	/**
	 * Flags whether planner needs to do TOTAL replan based 
	 * on a trigger occuring (i.e phase change)
	 */
	private static boolean REPLAN_TRIGGER = false;
	
	/**
	 * Flags if a ship was bought
	 */
	private static boolean BOUGHT_SHIP = false;
	
	/**
	 * Contains domain knowledge of the team
	 */
	private StateRepresentation state;
	
	/**
	 * The planner that directs the ship actions
	 */
	private Planner planner;
	
	/**
	 * This will track how many 'Double Base Heal Powerups' 
	 * were bought at a particular base
	 */
	private Integer[] doubleBaseHealpowerUpAmount;

	@Override
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		
		if(!INITIALIZED) { // Provide navigators to each ship
			System.out.println("Performing Initialization");
			WorldKnowledge.setTeamName(TEAM_NAME);
			for(Ship ship : WorldKnowledge.getTeamShips(space)) {
				state.assignShipToNavigator(ship.getId(), new Navigator(DEBUG_MODE));
				planner.assignShipToActionQueue(ship.getId());
			}
			state.assignBaseBuildingLocations(space, WorldKnowledge.getOtherTeamFlag(space));
			INITIALIZED = true; // Initialization performed!
		}
		
		if(BOUGHT_SHIP) { // When ship is bought it must be assigned a navigator and action queue
			for(Ship ship : WorldKnowledge.getTeamShips(space)) {
				if(!(state.shipAssignedNavigator(ship))) {
					state.assignShipToNavigator(ship.getId(), new Navigator(DEBUG_MODE));
					planner.assignShipToActionQueue(ship.getId());
				}
			}
			BOUGHT_SHIP = false;
		}
		
		// Need to clear planner to do replan...
		if(space.getCurrentTimestep() % REPLAN_MULTIAGENT_ACTIONS == 0 || REPLAN_TRIGGER) {
			planner.clear();
			REPLAN_TRIGGER = false;
		}
		
		Map<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

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
		
		for (AbstractObject actionable : actionableObjects) { // Find ships and assign each an action to perform
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                planner.checkCurrentAction(space, ship.getId()); // Ensure current action is valid
                planner.checkReplanFlagCarriers(space);
            }
        }
	}

	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();
		Base base;
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Base) {
				base = (Base) actionable;
				
				if(space.findShortestDistance(base.getPosition(), state.getConvientBaseBuildingLocations()[0]) 
						< WorldKnowledge.BASE_AT_LOCATION_THRESHOLD) {
					if(doubleBaseHealpowerUpAmount[0] <= doubleBaseHealpowerUpAmount[1]) {
						// Check for 'Double Base Healing Speed' powerup
						if (base.isValidPowerup(SpaceSettlersPowerupEnum.DOUBLE_BASE_HEALING_SPEED)) {
							System.out.println("Base using 'Double Base Healing Speed' powerup...At top base...");
							powerUps.put(base.getId(), SpaceSettlersPowerupEnum.DOUBLE_BASE_HEALING_SPEED);
							doubleBaseHealpowerUpAmount[0] = ++doubleBaseHealpowerUpAmount[0];
						}
					}
				}
				else if(space.findShortestDistance(base.getPosition(), state.getConvientBaseBuildingLocations()[1])
						< WorldKnowledge.BASE_AT_LOCATION_THRESHOLD) {
					if(doubleBaseHealpowerUpAmount[1] <= doubleBaseHealpowerUpAmount[0]) {
						// Check for 'Double Base Healing Speed' powerup
						if (base.isValidPowerup(SpaceSettlersPowerupEnum.DOUBLE_BASE_HEALING_SPEED)) {
							System.out.println("Base using 'Double Base Healing Speed' powerup... At bottom base...");
							powerUps.put(base.getId(), SpaceSettlersPowerupEnum.DOUBLE_BASE_HEALING_SPEED);
							doubleBaseHealpowerUpAmount[1] = ++doubleBaseHealpowerUpAmount[1];
						}
					}
				}
			}
		}
		return powerUps;
	}

	@Override
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, ResourcePile resourcesAvailable,
			PurchaseCosts purchaseCosts) {
		HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();
		
		// Detect when to switch planner's strategy...
		if(!(NEXT_PHASE_ISSUED) && resourcesAvailable.getResourceQuantity(ResourceTypes.WATER) >= Planner.WATER_RESOURCE_LEVEL &&
				resourcesAvailable.getResourceQuantity(ResourceTypes.FUEL) >= Planner.FUEL_RESOURCE_LEVEL &&
				resourcesAvailable.getResourceQuantity(ResourceTypes.METALS) >= Planner.METAL_RESOURCE_LEVEL &&
				NEXT_PHASE_ISSUED == false) {
			System.out.println("Swtiching to flag gathering phase...");
			planner.setAsteroidGatheringPhase(false);
			REPLAN_TRIGGER = true;
			NEXT_PHASE_ISSUED = true;
		}
		
		Ship ship;
		
		// We can afford a base to purchase!
		if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable) && !(planner.getAsteroidGatheringPhase())) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if(actionableObject instanceof Ship) {
					ship = (Ship) actionableObject;
					Position closestBaseSite = WorldKnowledge.getClosestBaseBuildingSite(space, ship, state);
					if(closestBaseSite != null && space.findShortestDistance(ship.getPosition(), closestBaseSite) 
							< WorldKnowledge.BASE_BUILD_THRESHOLD) {
						purchases.put(ship.getId(), PurchaseTypes.BASE);
						System.out.println("Buying base");
					}
				}
			}
		}
		
	
		// We can start buying ships after we have established convient bases
		if (purchaseCosts.canAfford(PurchaseTypes.SHIP, resourcesAvailable) && !(planner.getAsteroidGatheringPhase()) && 
				WorldKnowledge.isBaseBuiltAtLocation(space, state.getConvientBaseBuildingLocations()[0])
				&& WorldKnowledge.isBaseBuiltAtLocation(space, state.getConvientBaseBuildingLocations()[1]) 
				&& WorldKnowledge.getTeamShips(space).size() < 4) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Base) {
					Base base = (Base) actionableObject;
					if(base.isHomeBase()) {
						purchases.put(base.getId(), PurchaseTypes.SHIP);
						BOUGHT_SHIP = true;
						System.out.println("Buying ship at 'Home Base'!");
						break;
					}
				}
			}
		}
		
		if (purchaseCosts.canAfford(PurchaseTypes.POWERUP_DOUBLE_BASE_HEALING_SPEED, resourcesAvailable) && 
				WorldKnowledge.isBaseBuiltAtLocation(space, state.getConvientBaseBuildingLocations()[0])
				&& WorldKnowledge.isBaseBuiltAtLocation(space, state.getConvientBaseBuildingLocations()[1])) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Base) {
					Base base = (Base) actionableObject;
					if(space.findShortestDistance(base.getPosition(), state.getConvientBaseBuildingLocations()[0]) 
							< WorldKnowledge.BASE_AT_LOCATION_THRESHOLD) {
						if(doubleBaseHealpowerUpAmount[0] <= doubleBaseHealpowerUpAmount[1]) {
							purchases.put(base.getId(), PurchaseTypes.POWERUP_DOUBLE_BASE_HEALING_SPEED);
							System.out.println("Base buying double healing powerup... Top Base...");
							break;
						}
					}
					else if(space.findShortestDistance(base.getPosition(), state.getConvientBaseBuildingLocations()[1])
								< WorldKnowledge.BASE_AT_LOCATION_THRESHOLD) {
						if(doubleBaseHealpowerUpAmount[1] <= doubleBaseHealpowerUpAmount[0]) {
							purchases.put(base.getId(), PurchaseTypes.POWERUP_DOUBLE_BASE_HEALING_SPEED);
							System.out.println("Base buying double healing powerup... Bottom Base...");
							break;
						}
					}
				}
			}
		}
		
		return purchases;
	}

	@Override
	public void initialize(Toroidal2DPhysics space) {
		state = new StateRepresentation(); // Representation that contains important doman knowledge
		planner = new Planner(state); // Create planner to direct other ships
		TEAM_NAME = super.getTeamName();
		System.out.println("Initialized: " + TEAM_NAME);
		doubleBaseHealpowerUpAmount = new Integer[2]; // Only two convient locations
		
		// Start bases at zero times used
		doubleBaseHealpowerUpAmount[0] = 0;
		doubleBaseHealpowerUpAmount[1] = 0;
	}

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		INITIALIZED = false;
		NEXT_PHASE_ISSUED = false;
		REPLAN_TRIGGER = false;
		BOUGHT_SHIP = false;
		state = new StateRepresentation(); // Representation that contains important doman knowledge
		planner = new Planner(state); // Create planner to direct other ships
		TEAM_NAME = super.getTeamName();
		System.out.println("Shutting Down: " + TEAM_NAME);
		doubleBaseHealpowerUpAmount = new Integer[2]; // Only two convient locations
		
		// Start bases at zero times used
		doubleBaseHealpowerUpAmount[0] = 0;
		doubleBaseHealpowerUpAmount[1] = 0;
		planner.setAsteroidGatheringPhase(true);
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
    	// Replan the multiagent coordination in planner... With a set of event triggers...
    	if(space.getCurrentTimestep() % REPLAN_MULTIAGENT_ACTIONS == 0 || REPLAN_TRIGGER) {
        	planner.formulatePlan(space, ship.getId()); // Create a plan for the ship!
        	REPLAN_TRIGGER = false;
    	}
		return planner.getShipAction(space, ship.getId());
    }
}


/*package dani6621;

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

*//**
 * Controls multiple ships using knowledge representation that maps ships to
 * assigned duties. 
 * 
 * @author dani6621
 *
 *//*
public class MultiShipAgent extends TeamClient {
	
	*//**
	 * Used to help gather team ships
	 *//*
	public String TEAM_NAME;
	
	*//**
	 * Establish when it is good time to start gathering flags...
	 * This allows agents to gather resources neccessary for base 
	 * building near alcove for maximum effect
	 *//*
	private static final int FLAG_GATHERING_TIME = 3000;
	
	*//**
	 * Amount of time that must elapse before agent can replan navigation
	 *//*
	private static final int REPLAN_TIME_STEP = 30;
	
	*//**
	 * Number of new plans that can form if search fails for navigation
	 *//*
	private static final int MAX_RETRIES = 3;
	
	*//**
	 * Track if the program has initialized properly
	 *//*
	private boolean INITIALIZED = false;
	
	*//**
	 * Flag if new ship was bought and needs to be assigned 
	 * navigator
	 *//*
	private boolean BOUGHT_SHIP = false;
	
	*//**
	 * Debug mode for graphics
	 *//*
	public static final boolean DEBUG_MODE = false;
	
	*//**
	 * Member will contain information pertaining to team actions
	 *//*
	private StateRepresentation teamKnowledge;
	
	*//**
	 * Member will contain world state information
	 *//*
	private WorldKnowledge knowledge;
	
	*//**
	 * Centralized agent that controls ships
	 *//*
	private Planner planner;

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
	
	*//**
     * Function retrieve an action for the ship to perform based on the world 
     * state and the team state. If no action is found then the ship shall perform
     * a <code>DoNothingAction</code> as default
     * 
     * @param space the space containing the objects and the ship
     * @param ship the ship that will be assigned the action
     * @return an action for the ship to perform
     *//*
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
    
    *//**
     * Method will compute asteroid for ship to go after
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * @param knowledge	the reference to world state
     *//*
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
    
    *//**
     * Function will provided actions that allow ship to get flag
     * 
     * @param space	a reference to space
     * @param ship	the ship going for the flag
     * @param knowledge	the world state
     *//*
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
    
    *//**
     * Method will compute base for ship to return resources 
     * 
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * @param knowledge	the world state
     * 
     *//*
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
    
    *//**
     * Function will form path to base build site
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * @param knowledge	the world state
     *//*
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
    
    *//**
     * Function will form path to location
     * 
     * @param space	a reference to space
     * @param ship	the reference to ship
     * @param knowledge	the world state
     * @param location 	the location ship wants to goto
     *//*
    private void transitToLocation(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge, Position location) {
    	// Replan route
        if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) {
            		teamKnowledge.generateTeamMemberPath(space, ship, location, 
                			WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship));
        }
    }
}
*/