package dani6621;

import java.util.HashMap;
import java.util.UUID;

import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Flag;

/**
 * The class is responsible for containing a representation
 * of the team's actions. It will help to coordinate actions
 * between the ships. It will contain items such as a mapping
 * of ship to object for assigning. This class must preserve
 * and maintain the data in order to allow multi-agent 
 * coordination
 * 
 * @author dani6621
 *
 */
public class TeamKnowledge {
	
	/**
	 * Data structure will track what asteroids are assigned
	 */
	private HashMap<UUID, Asteroid> shipToAsteroidAssignemnt;
	
	/**
	 * Data structure will track what bases are assigned
	 */
	private HashMap<UUID, Base> shipToBaseAssignment;
	
	/**
	 * Data structure will track what flags are assigned
	 */
	private HashMap<UUID, Flag> shipToFlagAssignment;
}
