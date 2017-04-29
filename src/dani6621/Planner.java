package dani6621;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * Class will 
 * 
 * @author dani6621
 *
 */
public class Planner {
	
	/**
	 * Denotes when planner should soley assign ships to 
	 * gathering resources
	 */
	private static boolean ASTEROID_GATHERING_PHASE = true;
	
	/**
	 * Specifies high level actions that planner can issue...
	 * This will be stored into a stack and subsequent function
	 * will be called
	 *
	 */
	public enum ActionEnum {
		GET_FLAG, RETURN_TO_BASE, GET_ASTEROID,
		BUILD_BASE, BUY_SHIP, GET_ENERGY;
	}

	/**
	 * The list of actions each ship performs
	 */
	private Map<UUID, Queue<HighLevelAction>> shipToActionQueue;
	
	/**
	 * Holds reference to state that will be mutated as planner transverses possible actions
	 */
	private StateRepresentation state;
	
	/**
	 * Basic constructor
	 * @param teamInfo
	 */
	public Planner(StateRepresentation teamInfo) {
		state = teamInfo;
		shipToActionQueue = new HashMap<UUID, Queue<HighLevelAction>>(); 
	}
	
	/**
	 * Function will create a plan for the specified ship. It will
	 * be guided by the state stored in the class. It will also 
	 * mutate the state based on the proposed ship actions
	 * 
	 * @param space	a reference to space
	 * @param shipID	the UUID of ship
	 */
	public void formulatePlan(Toroidal2DPhysics space, UUID shipID) {
		Ship ship = (Ship) space.getObjectById(shipID);
		state.assignShipToResourceCount(ship.getId(), ship.getResources().getTotal()); // Initialize intial resource count to ship current cargo state
		emptyShipActionQueue(shipID);
		
		if(ASTEROID_GATHERING_PHASE) { // Get enough asteroids in order to optimize flag gathering
			Asteroid closestAsteroid;
			// Check if ship can get full cargo and also check if there are more asteroids to even assign...
			while((ship.getResources().getTotal() < WorldKnowledge.RESOURCE_THRESHOLD 
				&& state.getResourceCount(shipID) < WorldKnowledge.RESOURCE_THRESHOLD) 
				&& state.getNumberOfAssignedAsteroids() < WorldKnowledge.getMineableAsteroids(space).size()) {
				closestAsteroid = WorldKnowledge.getClosestAsteroid(space, ship, state);
				if(closestAsteroid != null) {
					state.assignAsteroidToShip(space, shipID, closestAsteroid.getId());
					shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.GET_ASTEROID, closestAsteroid.getId())); 
				}
				else {
					break; // Means we have run out of asteroids
				}
			}
			
			Base closestBase = WorldKnowledge.getClosestFriendlyBase(space, ship); // Get base to return to...
			shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.RETURN_TO_BASE, closestBase.getId()));
			state.assignBaseToShip(shipID, closestBase.getId());
		}
		else {
			// Where we assign flag gathering and base building to optimize flag count
		}
		
		System.out.println("Plan for: " + shipID);
		
		for(HighLevelAction a : shipToActionQueue.get(shipID)) {
			System.out.println(shipID + " : " + a.actionType + "," + a.goalObject);
		}
	}
	
	/**
	 * 
	 * @param shipID
	 * @return
	 */
	public AbstractAction getShipAction(Toroidal2DPhysics space, UUID shipID) {
		Ship ship = (Ship) space.getObjectById(shipID);
		AbstractObject goalObject;
		AbstractAction action = new DoNothingAction();
		HighLevelAction highLevelAction = shipToActionQueue.get(shipID).peek();
		
		// Check for 'null'
		if(highLevelAction != null) {
			if(highLevelAction.actionType == ActionEnum.GET_ASTEROID) { // Need to get asteroid
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0 && goalObject != null) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstacles(space, ship));
				}
				action = state.getTeamMemberAction(space, ship);
			}
			else if(highLevelAction.actionType == ActionEnum.RETURN_TO_BASE) { // Return to base
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship));
				}
				action = state.getTeamMemberAction(space, ship);
			}
		}
		
		return action;
	}
	
	/**
	 * Assigns a navigator to ship
	 * 
	 * @param shipID	the ship ID to assign a navigator
	 * @param navigator	the navigator assigned to ship
	 */
	public void assignShipToActionQueue(UUID shipID) {
		shipToActionQueue.put(shipID, new LinkedList<HighLevelAction>());
	}
	
	/**
	 * 
	 * @param shipID
	 */
	public void emptyShipActionQueue(UUID shipID) {
		shipToActionQueue.get(shipID).clear();
	}
	
	/**
	 * Function will detect if ship has navigator assigned to it
	 * 
	 * @param ship	the ship that wil be checked
	 * @return	a boolean of the result
	 */
	public boolean shipAssignedNavigator(UUID shipID) {
		return shipToActionQueue.containsKey(shipID);
	}
	
	/**
	 * Function will check if the current action at
	 * the top of the queue is still valid
	 * 
	 * @param space	a reference to space
	 * @param shipID	the UUID of the ship to check
	 */
	public void checkCurrentAction(Toroidal2DPhysics space, UUID shipID) {
		HighLevelAction action = shipToActionQueue.get(shipID).peek();
		
		if(action.actionType == ActionEnum.GET_ASTEROID) { // Check for the existance of an asteroid in the simulator
			UUID asteroidID = action.goalObject;
			
			Asteroid asteroid = (Asteroid) space.getObjectById(asteroidID);
			
			if(asteroid == null || !(asteroid.isAlive())) { // If asteroid is dead
				// System.out.println("Asteroid is dead");
				state.unassignAsteroidToShip(asteroidID); // Make sure to unassign
				shipToActionQueue.get(shipID).remove(); // Remove the no longer valid action from queue
			}
		}
		else if(action.actionType == ActionEnum.RETURN_TO_BASE) { // Use heuristic for ship returned to base
			Base base = (Base) space.getObjectById(action.goalObject);
			Ship ship = (Ship) space.getObjectById(shipID);
			
			if((ship == null || !(ship.isAlive())) || ship.getResources().getTotal() == 0 && 
					space.findShortestDistance(ship.getPosition(), base.getPosition()) < StateRepresentation.HIT_BASE_DISTANCE 
					&& !(ship.isCarryingFlag())) {
				// System.out.println("Hit base");
				state.unassignBaseToShip(base.getId());
				shipToActionQueue.get(shipID).remove();
			}
			
		}
	}
	
	/**
	 * Function will clear all assignments...
	 * This basically undos all the actions taken...
	 */
	public void clear() {
		state.clear();
	}
	
	/**
	 * 
	 * @param space
	 * @param shipID
	 */
	public void clearShipActions(Toroidal2DPhysics space, UUID shipID) {
		for(HighLevelAction action : shipToActionQueue.get(shipID)) {
			if(action.goalObject != null) {
				if(action.actionType == ActionEnum.GET_ASTEROID) {
					Asteroid asteroid = (Asteroid) space.getObjectById(action.goalObject);
					state.unassignAsteroidToShip(asteroid.getId());
				}
				else if(action.actionType == ActionEnum.RETURN_TO_BASE) {
					Base base = (Base) space.getObjectById(action.goalObject);
					state.unassignBaseToShip(base.getId());
				}
			}
		}
		emptyShipActionQueue(shipID); // Give new queue that is empty
	}
	
	/**
	 * Specifies a high level action that contains data 
	 * members that relay enough to reconstruct action
	 *
	 */
	public class HighLevelAction {
		
		/**
		 * Specifies the action 
		 */
		public final ActionEnum actionType;
		
		/**
		 * Specifies the goal object 
		 * NOTE: This can be <code>null</code>
		 */
		public final UUID goalObject;
		
		/**
		 * Specifies the goal position
		 * NOTE: This can be <code>null</code>
		 */
		public final Position goalPosition;
		
		/**
		 * Basic contructor
		 * 
		 * @param action
		 * @param goal
		 */
		public HighLevelAction(ActionEnum action, UUID goal) {
			actionType = action;
			goalObject = goal;
			goalPosition = null;
		}
		
		/**
		 * Basic constructor
		 * 
		 * @param action
		 * @param goal
		 */
		public HighLevelAction(ActionEnum action, Position goal) {
			actionType = action;
			goalObject = null;
			goalPosition = goal;
		}
	}
}
