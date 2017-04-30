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
	 * Number of spawn points flag can be spawned at on other 
	 * team
	 */
	private static final int NUMBER_FLAG_SPAWN = 2;
	
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
	 * Stores the location of good placement for base
	 */
	private Position[] convientBaseLocations;
	
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
	 * Store the ID for the flag carrier
	 */
	private UUID flagCarrierID;
	
	/**
	 * Store the ID of base builder
	 */
	private UUID baseBuilderID;
	
	/**
	 * Data structure will store ship to navigator
	 */
	private HashMap<UUID, Navigator> shipToNavigator;
	
	public StateRepresentation() {
		convientBaseLocations = new Position[2];
		shipToResourceCount = new HashMap<UUID, Integer>();
		asteroidToShip = new HashMap<UUID, UUID>();
		beaconToShip = new HashMap<UUID, UUID>();
		baseToShip = new HashMap<UUID, UUID>();
		flagCarrierID = null;
		baseBuilderID = null;
		shipToNavigator = new HashMap<UUID, Navigator>();
	}
	
	/**
	 * 
	 * @param shipID
	 * @param resources
	 */
	public void assignShipToResourceCount(UUID shipID, int resources) {
		shipToResourceCount.put(shipID, resources);
	}
	
	/**
	 * 
	 * @param shipID
	 * @return
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
	 * 
	 * @param asteroidID
	 * @return
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
	public void assignFlagCarrierID(UUID shipID) {
		flagCarrierID = shipID;
	}
	
	/**
	 * 
	 * @return
	 */
	public UUID getFlagCarrierID() {
		return flagCarrierID;
	}
	
	/**
	 * 
	 * @return
	 */
	public UUID getBaseBuilderID() {
		return baseBuilderID;
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
	 * @return	an action that gets ship closer to goal
	 */
	public AbstractAction getTeamMemberAction(Toroidal2DPhysics space, Ship ship) {
		return shipToNavigator.get(ship.getId()).retrieveNavigationAction(space, ship);
	}
	
	/**
	 * Clears all the mapping relations
	 */
	public void clear() {
		System.out.println("Clearing the state");
		asteroidToShip.clear();
		beaconToShip.clear();
		baseToShip.clear();
		flagCarrierID = null;
		baseBuilderID = null;
		
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
				convientBaseLocations[0] = new Position(flagSpawn.getX() + 200.0, flagSpawn.getY());
				convientBaseLocations[1] = new Position(flagSpawn.getX() + 200.0, flagSpawn.getY() + 550.0);
			}
			else { // Flag spawned bottom
				System.out.println("Bottom-Right");
				convientBaseLocations[0] = new Position(flagSpawn.getX() + 200.0, flagSpawn.getY());
				convientBaseLocations[1] = new Position(flagSpawn.getX() + 200.0, flagSpawn.getY() - 550.0);
			}
		}
		else { // Flag spawned on left side
			if(flagSpawn.getY() < 500.0) { // Flag spawned top
				System.out.println("Top-Left");
				convientBaseLocations[0] = new Position(flagSpawn.getX() - 200.0, flagSpawn.getY());
				convientBaseLocations[1] = new Position(flagSpawn.getX() - 200.0, flagSpawn.getY() + 550.0);
			}
			else { // Flag spawned bottom
				System.out.println("Bottom-Left");
				convientBaseLocations[0] = new Position(flagSpawn.getX() - 200.0, flagSpawn.getY());
				convientBaseLocations[1] = new Position(flagSpawn.getX() - 200.0, flagSpawn.getY() - 550.0);
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
} 
