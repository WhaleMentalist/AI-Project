package dani6621;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * The class is responsible for containing a representation
 * of the team's current objectives (i.e gathering asteroids or base building). 
 * It will help to coordinate actions between the ships. It will contain items such 
 * as a mapping of ship to object for assigning. This class must preserve
 * and maintain the data in order to allow multi-agent coordination
 * 
 * @author dani6621
 *
 */
public class TeamKnowledge {
	
	/**
	 * Detect if ship has reached base
	 */
	private static final double HIT_BASE_DISTANCE = 10.0;
	
	/**
	 * Data structure will track what asteroids are assigned
	 */
	private HashMap<UUID, Ship> asteroidToShip;
	
	/**
	 * Data structure will track what bases are assigned
	 */
	private HashMap<UUID, Ship> baseToShip;
	
	/**
	 * Data structure will track what beacons are assigned
	 */
	private HashMap<UUID, Ship> energyToShip;
	
	/**
	 * Data structure will store ship to navigator
	 */
	private HashMap<UUID, Navigator> shipToNavigator;
	
	/**
	 * Holds the designated flag carrier for the team
	 */
	private UUID flagCarrier;
	
	/**
	 * Holds the designated base builder for the team
	 */
	private UUID baseBuilder;
	
	/**
	 * Initialization constructor
	 */
	public TeamKnowledge() {
		asteroidToShip = new HashMap<UUID, Ship>();
		baseToShip = new HashMap<UUID, Ship>();
		energyToShip = new HashMap<UUID, Ship>();
		shipToNavigator = new HashMap<UUID, Navigator>();
		flagCarrier = null;
		baseBuilder = null;
	}
	
	/**
	 * Assigns asteroid to a ship
	 * 	
	 * @param ship	the ship to be assigned
	 * @param asteroid	the asteroid that will be assigned
	 */
	public void assignAsteroidToShip(Ship ship, Asteroid asteroid) {
		asteroidToShip.put(asteroid.getId(), ship);
	}
	
	/**
	 * Function will remove asteroid mapping from the hash map. This
	 * allows the asteroid to be assigned
	 * 
	 * @param asteroid	the asteroid to unassign
	 */
	public void unassignAsteroidToShip(Asteroid asteroid) {
		asteroidToShip.remove(asteroid.getId());
	}
	
	/**
	 * Function will return if asteroid was assigned to ship
	 * 
	 * @param asteroid	the asteroid to check
	 * @return	the boolean flagging the result
	 */
	public boolean isAsteroidAssigned(Asteroid asteroid) {
		return asteroidToShip.containsKey(asteroid.getId());
	}
	
	/**
	 * Assigns base to a ship
	 * 	
	 * @param ship	the ship to be assigned
	 * @param base	the base that will be assigned
	 */
	public void assignBaseToShip(Ship ship, Base base) {
		baseToShip.put(base.getId(), ship);
	}
	
	/**
	 * Function will find suitable flag carrier based on current
	 * state of world and each ship
	 * 
	 * @return
	 */
	public void assignFlagCarrier(Ship ship) {
		flagCarrier = ship.getId();
	}
	
	/**
	 * Function returns UUID of flag carrier
	 * 
	 * @return	a <code>UUID</code> representing flag carrier
	 */
	public UUID getFlagCarrierUUID() {
		return flagCarrier;
	}
	
	/**
	 * Function will assign ship as base builder
	 */
	public void assignBaseBuilder(Ship ship) {
		baseBuilder = ship.getId();
	}
	
	/**
	 * Function returns UUID of base builder
	 * 
	 * @return	a <code>UUID</code> representing the base builder
	 */
	public UUID getBaseBuilderUUID() {
		return baseBuilder;
	}
	
	/**
	 * Assign energy source to ship
	 * 
	 * @param ship	the ship to be assigned
	 * @param source	the energy source that will be assigned
	 */
	public void assignEnergyToShip(Ship ship, AbstractObject source) {
		energyToShip.put(source.getId(), ship);
	}
	
	/**
	 * Function will remove energy mapping to ship
	 * 
	 * @param source	the source that will be removed from mapping
	 */
	public void unassignEnergyToShip(AbstractObject source) {
		energyToShip.remove(source.getId());
	}
	
	/**
	 * Function returns if source is assigned to ship
	 * 
	 * @param ship	the ship that is checking for assignment
	 * @param source	the energy source that is assigned
	 * @return	the result as boolean
	 */
	public boolean isEnergyAssigned(Ship ship, AbstractObject source) {
		boolean result = false;
		if(energyToShip.containsKey(source.getId()) &&
				!(ship.getId().equals(energyToShip.get(source.getId()).getId()))) { // Check if ship already has beacon
			result = true;
		}
		return result;
	}
	
	/**
	 * Assigns a navigator to ship
	 * 
	 * @param ship	the ship to assign a navigator
	 * @param navigator	the navigator assigned to ship
	 */
	public void assignShipToNavigator(Ship ship, Navigator navigator) {
		shipToNavigator.put(ship.getId(), navigator);
	}
	
	/**
	 * Function updates the assignments for team members
	 * 
	 * @param space	the reference to the space
	 */
	public void updateAssignments(Toroidal2DPhysics space) {
		updateAsteroidToShip(space);
		updateEnergyToShip(space);
		updateBaseToShip(space);
		// updateAssignFlagToShip(space);
		// updateAssignBaseToShip(space);
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
			AbstractObject goal, Set<AbstractObject> obstacles) {
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
	 * Function updates and cleans asteroid assignments. It will check if the action 
	 * is valid (i.e does object still exist)
	 */
	private void updateAsteroidToShip(Toroidal2DPhysics space) {
		List<Asteroid> finishedAsteroids = new ArrayList<Asteroid>();
		Asteroid asteroid;
		Ship ship; // Reference to ship assigned
		
		for (UUID asteroidId : asteroidToShip.keySet()) {
			asteroid = (Asteroid) space.getObjectById(asteroidId);
			ship = (Ship) space.getObjectById(asteroidToShip.get(asteroid.getId()).getId());
			if (asteroid == null || !(asteroid.isAlive()) || asteroid.isMoveable() || ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD ||
					!(asteroidId.equals(shipToNavigator.get(ship.getId()).getGoalObjectUUID()))) {
 				finishedAsteroids.add(asteroid);
			}
		}
		
		for (Asteroid asteroidElement : finishedAsteroids) { // Delete asteroid from map
			asteroidToShip.remove(asteroidElement.getId());
		}
	}
	
	// TODO: Get constants back into program for better readablility
	
	/**
	 * Function updates and cleans base assignments.
	 * 
	 * @param space
	 */
	private void updateBaseToShip(Toroidal2DPhysics space) {
		List<Base> finishedBase = new ArrayList<Base>();
		Ship ship; // Hold reference to ship going to base
		Base base; // Hold reference to base
		
		for (UUID baseID : baseToShip.keySet()) {
			base = (Base) space.getObjectById(baseID);
			ship = (Ship) space.getObjectById(baseToShip.get(base.getId()).getId());
			
			if(ship == null || !(ship.isAlive()) || ship.getResources().getTotal() == 0 && 
					space.findShortestDistance(ship.getPosition(), base.getPosition()) < HIT_BASE_DISTANCE 
					&& !(ship.isCarryingFlag())) {
				finishedBase.add(base); // Ship managed to get to base
				
				if(ship.getId().equals(flagCarrier)) // Flag carrier hit base so unassign flag carrier
					flagCarrier = null;
			}
		}
		
		for (Base baseElement : finishedBase) { // Delete base from map
			baseToShip.remove(baseElement.getId());
		}
	}
	
	/**
	 * Function will update the energy beacon assignments in the mapping data
	 * structure
	 * 
	 * @param space	a reference to space
	 */
	private void updateEnergyToShip(Toroidal2DPhysics space) {
		List<Beacon> finishedEnergy = new ArrayList<Beacon>();
		Ship ship;
		Beacon beacon;

		for (UUID beaconID : energyToShip.keySet()) {
			beacon = (Beacon) space.getObjectById(beaconID);
			ship = (Ship) space.getObjectById(energyToShip.get(beaconID).getId());
			if (beacon == null || !(beacon.isAlive()) || ship.getEnergy() > WorldKnowledge.ENERGY_THRESHOLD || !(ship.isAlive())
					|| !(beaconID.equals(shipToNavigator.get(ship.getId()).getGoalObjectUUID()))) {
				finishedEnergy.add(beacon);
			}
		}
		
		for (Beacon beaconElement : finishedEnergy) { // Delete beacon from map
			energyToShip.remove(beaconElement.getId());
		}
	}
}
