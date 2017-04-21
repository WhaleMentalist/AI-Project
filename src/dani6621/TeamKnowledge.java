package dani6621;

import java.util.HashMap;
import java.util.UUID;

import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Flag;
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
	 * Data structure will track what asteroids are assigned
	 */
	private HashMap<UUID, Ship> asteroidToShip;
	
	/**
	 * Data structure will track what bases are assigned
	 */
	private HashMap<UUID, Ship> baseToShip;
	
	/**
	 * Data structure will track what flags are assigned
	 */
	private HashMap<UUID, Ship> flagToShip;
	
	/**
	 * Initialization constructor
	 */
	public TeamKnowledge() {
		asteroidToShip = new HashMap<UUID, Ship>();
		baseToShip = new HashMap<UUID, Ship>();
		flagToShip = new HashMap<UUID, Ship>();
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
	 * Assigns base to a ship
	 * 	
	 * @param ship	the ship to be assigned
	 * @param base	the base that will be assigned
	 */
	public void assignBaseToShip(Ship ship, Base base) {
		baseToShip.put(base.getId(), ship);
	}

	/**
	 * Assigns flag to a ship
	 * 	
	 * @param ship	the ship to be assigned
	 * @param flag	the flag that will be assigned
	 */
	public void assignFlagToShip(Ship ship, Flag flag) {
		flagToShip.put(flag.getId(), ship);
	}
	
	/**
	 * Function updates the assignments for team members
	 * 
	 * @param space	the reference to the space
	 */
	public void updateAssignments(Toroidal2DPhysics space) {
		updateAssignAsteroidToShip(space);
		updateAssignFlagToShip(space);
		updateAssignBaseToShip(space);
	}
	
	/**
	 * Function updates and cleans asteroid assignments. It will check if the action 
	 * is valid (i.e does object still exist)
	 */
	private void updateAssignAsteroidToShip(Toroidal2DPhysics space) {
		for(UUID id : asteroidToShip.keySet()) {
			if(space.getObjectById(id) == null || !(space.getObjectById(id).isAlive())) { // Object no longer in game
				asteroidToShip.remove(id); // Remove it from map
			}
		}
	}
	
	// TODO: Get constants back into program for better readablility
	
	/**
	 * Function updates and cleans base assignments.
	 * 
	 * @param space
	 */
	private void updateAssignBaseToShip(Toroidal2DPhysics space) {
		Ship ship;
		for(UUID id : baseToShip.keySet()) {
			ship = baseToShip.get(id); // Get ship assigned
			if(ship.getResources().getTotal() == 0 && ship.getEnergy() > 2000.0 && !(ship.isCarryingFlag())) {
				baseToShip.remove(id); // Ship managed to get to base
			}
		}
	}
	
	/**
	 * Function updates and cleans flag assignments. It will check if action
	 * is valid (i.e does object still exist)
	 * 
	 * @param space	the reference to the space
	 */
	private void updateAssignFlagToShip(Toroidal2DPhysics space) {
		for(UUID id : flagToShip.keySet()) {
			if(space.getObjectById(id) == null || !(space.getObjectById(id).isAlive())) { // Object no longer in game
				flagToShip.remove(id); // Remove it from map
			}
		}
	}
}
