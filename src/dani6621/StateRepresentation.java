package dani6621;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Flag;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

public class StateRepresentation {
	
	/**
	 * Top right alcove spot
	 */
	private static final Position TOP_RIGHT = new Position(1250, 250);
	
	/**
	 * Bottom right alcove spot
	 */
	private static final Position BOTTOM_RIGHT = new Position(1250, 800);
	
	/**
	 * Top left alcove spot
	 */
	private static final Position TOP_LEFT = new Position(350, 250);
	
	/**
	 * Bottom left alcove spot
	 */
	private static final Position BOTTOM_LEFT = new Position(350, 800);
	
	/**
	 * Detect if ship has reached base
	 */
	public static final double HIT_BASE_DISTANCE = 30.0;
	
	/**
	 * Tells other parts of program that flagged was or was not returned
	 */
	private static boolean FLAG_RETURNED = false;

	/**
	 * Stores the location of good placement for base
	 */
	private Position[] convientBaseLocations;
	
	/**
	 * Stores the location for loitering 
	 */
	private Position[] loiterLocations;
	
	/**
	 * Stores the amount of resources in ship when executing instance
	 * of a plan
	 */
	private Map<UUID, Integer> shipToResourceCount;
	
	/**
	 * Stores the asteroid to ship assignment... Useful for 
	 * precondition application... It is also helps to mutate
	 * the state by applying an effect as well...
	 */
	private Map<UUID, UUID> asteroidToShip;
	
	/**
	 * Stores the ship to beacon assignment...
	 */
	private Map<UUID, UUID> beaconToShip;
	
	/**
	 * Store the ship to base assignment
	 */
	private Map<UUID, UUID> baseToShip;
	
	/**
	 * Store the ID for the first flag carrier
	 */
	private UUID flagCarrierOneID;
	
	/**
	 * Store the ID for the second flag carrier
	 */
	private UUID flagCarrierTwoID;
	
	/**
	 * Stores the current number of resources as a result of an effect
	 */
	private int totalResources;
	
	/**
	 * Stores the last number of resources as a result of an effect
	 */
	private int lastTotalResources;
	
	/**
	 * Stores the current number of flags as a result of an effect
	 */
	public int totalFlags;
	
	/**
	 * Stores the last number of flags as a result of an effect
	 */
	public int lastTotalFlags;
	
	/**
	 * Tracks what ship is carrying the flag...
	 */
	private UUID currentFlagCarrier;
	
	/**
	 * Data structure will store ship to navigator
	 */
	private HashMap<UUID, Navigator> shipToNavigator;
	
	/**
	 * Initialize the state representation with empty
	 * values
	 */
	public StateRepresentation() {
		convientBaseLocations = new Position[2];
		loiterLocations = new Position[2];
		shipToResourceCount = new HashMap<UUID, Integer>();
		asteroidToShip = new HashMap<UUID, UUID>();
		beaconToShip = new HashMap<UUID, UUID>();
		baseToShip = new HashMap<UUID, UUID>();
		flagCarrierOneID = null;
		flagCarrierTwoID = null;
		totalResources = 0;
		lastTotalResources = 0;
		totalFlags = 0;
		lastTotalFlags = 0;
		shipToNavigator = new HashMap<UUID, Navigator>();
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param state	the state to create a copy 
	 */
	public StateRepresentation(StateRepresentation state) {
		convientBaseLocations = state.convientBaseLocations;
		loiterLocations = state.loiterLocations;
		shipToResourceCount = new HashMap<UUID, Integer>(state.shipToResourceCount);
		asteroidToShip = new HashMap<UUID, UUID>(state.asteroidToShip);
		beaconToShip = new HashMap<UUID, UUID>(state.beaconToShip);
		baseToShip = new HashMap<UUID, UUID>(state.baseToShip);
		flagCarrierOneID = state.flagCarrierOneID;
		flagCarrierTwoID = state.flagCarrierTwoID;
		currentFlagCarrier = state.currentFlagCarrier;
		totalResources = state.totalResources;
		lastTotalResources = state.lastTotalResources;
		totalFlags = state.totalFlags;
		lastTotalFlags = state.lastTotalFlags;
		shipToNavigator = new HashMap<UUID, Navigator>(state.shipToNavigator);
	}
	
	/**
	 * Function will assign ship to a number of resources
	 * 	
	 * @param shipID	the ID of ship to assign resources
	 * @param resources	the number of resources ship will have
	 */
	public void assignShipToResourceCount(UUID shipID, int resources) {
		shipToResourceCount.put(shipID, resources);
	}
	
	/**
	 * Get the number of resource the ship has
	 * 
	 * @param shipID	the ID of ship to assign resources
	 * @return	the number of resources
	 */
	public int getResourceCount(UUID shipID) {
		return shipToResourceCount.get(shipID);
	}
	
	/**
	 * Function will add resources to current count
	 * NOTE: This should not throw a <code>NullPointerException</code>
	 * 
	 * @param shipID
	 * @param resources
	 * @return
	 */
	public void addResourceCount(UUID shipID, int resources) {
		shipToResourceCount.put(shipID, shipToResourceCount.get(shipID) + resources);
	}
	
	/**
	 * Function will map ship to asteroid. This is useful for
	 * precondition application... It is also useful for 
	 * tracking the amount of potenial resources ship could 
	 * get, thereby affecting when it should go back to base
	 * 
	 * @param space	a reference to space
	 * @param shipID	ship UUID
	 * @param asteroidID	asteroid UUID
	 */
	public void assignAsteroidToShip(Toroidal2DPhysics space, UUID shipID, UUID asteroidID) {
		if(!(isAsteroidAssigned(asteroidID))) {
			asteroidToShip.put(asteroidID, shipID);
			addResourceCount(shipID, ((Asteroid) space.getObjectById(asteroidID)).getResources().getTotal()); 
		}
	}
	
	/**
	 * Function checks if asteroid is assigned
	 * 
	 * @param asteroidID	the asteroid to check
	 * @return	a boolean of result
	 */
	public boolean isAsteroidAssigned(UUID asteroidID) {
		return asteroidToShip.containsKey(asteroidID);
	}
	
	/**
	 * 
	 * @param asteroidID
	 */
	public void unassignAsteroidToShip(UUID asteroidID) {
		asteroidToShip.remove(asteroidID);
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNumberOfAssignedAsteroids() {
		return asteroidToShip.size();
	}
	
	/**
	 * 
	 * @param shipID
	 * @param beaconID
	 */
	public void assignBeaconToShip(UUID shipID, UUID beaconID) {
		beaconToShip.put(shipID, beaconID);
	}
	
	/**
	 * 
	 * @param beaconID
	 * @return
	 */
	public boolean isBeaconAssigned(UUID beaconID) {
		return beaconToShip.containsKey(beaconID);
	}
	
	/**
	 * 
	 * @param beaconID
	 */
	public void unassignBeaconToShip(UUID beaconID) {
		beaconToShip.remove(beaconID);
	}
	
	/**
	 * 
	 * @param shipID
	 * @param baseID
	 */
	public void assignBaseToShip(UUID shipID, UUID baseID) {
		baseToShip.put(baseID, shipID);
		if(shipToResourceCount.get(shipID) != null) {
			totalResources += shipToResourceCount.get(shipID); // Add ship resource in cargo to total
		}
		shipToResourceCount.put(shipID, 0); // Returning to base empties resources
	}
	
	/**
	 * 
	 * @param baseID
	 * @return
	 */
	public boolean isBaseAssigned(UUID baseID) {
		return baseToShip.containsKey(baseID);
	}	
	
	/**
	 * 
	 * @param baseID
	 */
	public void unassignBaseToShip(UUID baseID) {
		baseToShip.remove(baseID);
	}

	/**
	 * 
	 * @param shipID
	 */
	public void assignFlagCarrierOneID(UUID shipID) {
		flagCarrierOneID = shipID;
	}
	
	/**
	 * 
	 * @return
	 */
	public UUID getFlagCarrierOneID() {
		return flagCarrierOneID;
	}
	
	/**
	 * 
	 * @param shipID
	 */
	public void assignFlagCarrierTwo(UUID shipID) {
		flagCarrierTwoID = shipID;
	}
	
	/**
	 * 
	 * @return
	 */
	public UUID getFlagCarrierTwoID() {
		return flagCarrierTwoID;
	}
	
	/**
	 * Assigns a navigator to ship
	 * 
	 * @param shipID	the ship ID to assign a navigator
	 * @param navigator	the navigator assigned to ship
	 */
	public void assignShipToNavigator(UUID shipID, Navigator navigator) {
		shipToNavigator.put(shipID, navigator);
	}
	
	/**
	 * Function will detect if ship has navigator assigned to it
	 * @param ship	the ship that wil be checked
	 * @return	a boolean of the result
	 */
	public boolean shipAssignedNavigator(Ship ship) {
		return shipToNavigator.containsKey(ship.getId());
	}
	
	/**
	 * Function will generate a path to goal for the given ship
	 * 
	 * @param space	a reference to the space
	 * @param ship	the ship that requested the path
	 * @param goal	the goal ship wants to reach
	 * @param obstacles	the obstacles that could impede ship
	 */
	public void generateTeamMemberPath(Toroidal2DPhysics space, Ship ship, 
			Position goal, Set<AbstractObject> obstacles) {
		shipToNavigator.get(ship.getId()).generateAStarPath(space, ship, goal, obstacles);
	}
	
	/**
	 * Retrieves the action for the ship from the navigator
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that needs the action
	 * @param goal	the goal object the ship desires
	 * @param obstacles	the obstacles that may impede ship
	 * @param isLoiter	determines if ship should loiter at end location
	 * @return	an action that gets ship closer to goal
	 */
	public AbstractAction getTeamMemberAction(Toroidal2DPhysics space, Ship ship, boolean isLoiter) {
		AbstractAction action;
		
		if(isLoiter) {
			action = shipToNavigator.get(ship.getId()).retrieveNavigationActionLoiter(space, ship);
		}
		else {
			action = shipToNavigator.get(ship.getId()).retrieveNavigationAction(space, ship);
		}
		return action;
	}
	
	/**
	 * Clears all the mapping relations
	 */
	public void clear() {
		asteroidToShip.clear();
		beaconToShip.clear();
		baseToShip.clear();
		totalResources = 0;
		lastTotalResources = 0;
		totalFlags = 0;
		lastTotalFlags = 0;
		
		// Clear navigator for each ship
		for(UUID shipID : shipToNavigator.keySet()) {
			shipToNavigator.put(shipID, new Navigator(false)); // TODO: Put in way to pass debug setting
		}
	}
	
	/**
	 * Function will attempt to use location of flag to discern flag spawn location, which
	 * then figure out base building locations. This occurs ONLY once in the program (i.e 
	 * during intialization)
	 * 
	 * @param space	a reference to space
	 * @param flag	the flag object to observe
	 */
	public void assignBaseBuildingLocations(Toroidal2DPhysics space, Flag flag) {
		Position flagPosition = flag.getPosition();
		Position flagSpawn = null;
		double shortestDist = Double.MAX_VALUE;
		double dist;
		
		dist = space.findShortestDistance(flagPosition, TOP_RIGHT);
		if(dist < shortestDist) {
			flagSpawn = TOP_RIGHT;
			shortestDist = dist;
		}
		
		dist = space.findShortestDistance(flagPosition, TOP_LEFT);
		if(dist < shortestDist) {
			flagSpawn = TOP_LEFT;
			shortestDist = dist;
		}
		
		dist = space.findShortestDistance(flagPosition, BOTTOM_RIGHT);
		if(dist < shortestDist) {
			flagSpawn = BOTTOM_RIGHT;
			shortestDist = dist;
		}
		
		dist = space.findShortestDistance(flagPosition, BOTTOM_LEFT);
		if(dist < shortestDist) {
			flagSpawn = BOTTOM_LEFT;
			shortestDist = dist;
		}
		
		if(flagSpawn.getX() > 1000.0) { // Flag spawned on right side
			if(flagSpawn.getY() < 500.0) { // Flag spawned top
				System.out.println("Top-Right");
				convientBaseLocations[0] = new Position(flagSpawn.getX() + 100.0, flagSpawn.getY());
				convientBaseLocations[1] = new Position(flagSpawn.getX() + 100.0, flagSpawn.getY() + 550.0);
				loiterLocations[0] = new Position(TOP_RIGHT.getX(), TOP_RIGHT.getY());
				loiterLocations[1] = new Position(BOTTOM_RIGHT.getX(), BOTTOM_RIGHT.getY());
			}
			else { // Flag spawned bottom
				System.out.println("Bottom-Right");
				convientBaseLocations[0] = new Position(flagSpawn.getX() + 100.0, flagSpawn.getY() - 550.0);
				convientBaseLocations[1] = new Position(flagSpawn.getX() + 100.0, flagSpawn.getY());
				loiterLocations[0] = new Position(TOP_RIGHT.getX(), TOP_RIGHT.getY());
				loiterLocations[1] = new Position(BOTTOM_RIGHT.getX(), BOTTOM_RIGHT.getY());
			}
		}
		else { // Flag spawned on left side
			if(flagSpawn.getY() < 500.0) { // Flag spawned top
				System.out.println("Top-Left");
				convientBaseLocations[0] = new Position(flagSpawn.getX() - 100.0, flagSpawn.getY());
				convientBaseLocations[1] = new Position(flagSpawn.getX() - 100.0, flagSpawn.getY() + 550.0);
				loiterLocations[0] = new Position(TOP_LEFT.getX(), TOP_LEFT.getY());
				loiterLocations[1] = new Position(BOTTOM_LEFT.getX(), BOTTOM_LEFT.getY());
			}
			else { // Flag spawned bottom
				System.out.println("Bottom-Left");
				convientBaseLocations[0] = new Position(flagSpawn.getX() - 100.0, flagSpawn.getY() - 550.0);
				convientBaseLocations[1] = new Position(flagSpawn.getX() - 100.0, flagSpawn.getY());
				loiterLocations[0] = new Position(TOP_LEFT.getX(), TOP_LEFT.getY());
				loiterLocations[1] = new Position(BOTTOM_LEFT.getX(), BOTTOM_LEFT.getY());
			}
		}
		System.out.println(convientBaseLocations[0].toString() + "     " + convientBaseLocations[1].toString());
	}
	
	/**
	 * Function returns the list of convient base locations
	 * 
	 * @return	the positions of the base building spots
	 */
	public Position[] getConvientBaseBuildingLocations() {
		return convientBaseLocations;
	}
	
	/**
	 * Function returns the list containing the loiter locations
	 * for ships
	 * 
	 * @return	the positions of the loiter locations
	 */
	public Position[] getLoiterLocations() {
		return loiterLocations;
	}
	
	/**
	 * Function will return index in convient base location that 
	 * flag spawn is closest to
	 * 
	 * @param space	a reference to space
	 * @return
	 */
	public int flagSpawnLocation(Toroidal2DPhysics space) {
		Position flagPosition = WorldKnowledge.getOtherTeamFlag(space).getPosition();
		int flagSpawn = -1;
		double shortestDist = Double.MAX_VALUE;
		double dist;
		
		dist = space.findShortestDistance(flagPosition, TOP_RIGHT);
		if(dist < shortestDist) {
			flagSpawn = 0;
			shortestDist = dist;
		}
		
		dist = space.findShortestDistance(flagPosition, TOP_LEFT);
		if(dist < shortestDist) {
			flagSpawn = 0;
			shortestDist = dist;
		}
		
		dist = space.findShortestDistance(flagPosition, BOTTOM_RIGHT);
		if(dist < shortestDist) {
			flagSpawn = 1;
			shortestDist = dist;
		}
		
		dist = space.findShortestDistance(flagPosition, BOTTOM_LEFT);
		if(dist < shortestDist) {
			flagSpawn = 1;
			shortestDist = dist;
		}
		
		return flagSpawn;
	}
	
	/**
	 * 
	 * @param value
	 */
	public static void setFlagReturned(boolean value) {
		FLAG_RETURNED = value;
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean getFlagReturned() {
		return FLAG_RETURNED;
	}
	
	/**
	 * Function determines if the <code>StateRepresentation</code> is
	 * in a goal state. This will help to determine if a search can stop...
	 *
	 * @param space	a reference to space
	 * @return	a boolean result if the state is in a particular goal state
	 */
	public boolean isGoalState(Toroidal2DPhysics space) {
		boolean result = false;
		
		if(totalResources > lastTotalResources || 
				getNumberOfAssignedAsteroids() >= WorldKnowledge.getMineableAsteroids(space).size()) { // An effect has given us more resources or no more asteroids
			lastTotalResources = totalResources;
			result = true;
		}
		else if(totalFlags > lastTotalFlags) { // More flags is a goal
			lastTotalFlags = totalFlags;
			result = true;
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setCurrentFlagCarrier(UUID value) {
		currentFlagCarrier = value;
	}
	
	/**
	 * 
	 * @return
	 */
	public UUID getCurrentFlagCarrier() {
		return currentFlagCarrier;
	}
	
	/**
	 * 
	 */
	public void incrementTotalFlag() {
		++totalFlags;
	}
} 
