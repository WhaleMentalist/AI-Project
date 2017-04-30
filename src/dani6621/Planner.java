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
import spacesettlers.objects.Flag;
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
	 * Dictates level needed to switch to flag gathering phase
	 */
	public static final int WATER_RESOURCE_LEVEL = 1250;
	
	/**
	 * Dictates level needed to switch to flag gathering phase
	 */
	public static final int FUEL_RESOURCE_LEVEL = 1900;
	
	/**
	 * Dictates level needed to switch to flag gathering phase
	 */
	public static final int METAL_RESOURCE_LEVEL = 1850;
	
	/**
	 * Denotes when planner should soley assign ships to 
	 * gathering resources
	 */
	private boolean ASTEROID_GATHERING_PHASE = true;
	
	/**
	 * Specifies high level actions that planner can issue...
	 * This will be stored into a stack and subsequent function
	 * will be called
	 *
	 */
	public enum ActionEnum {
		GET_FLAG, RETURN_TO_BASE, GET_ASTEROID,
		GET_ENERGY, GO_TO_LOCATION;
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
		
		if(ASTEROID_GATHERING_PHASE) { // Get enough asteroids in order to optimize flag gathering
			asteroidGathering(space, shipID);
		}
		else {
			// Where we assign flag gathering and base building to optimize flag count
			if(state.getFlagCarrierOneID() == null) { // Need to give state flag carrier ID
				Ship ship = WorldKnowledge.getFlagCarrier(space, state);
				
				if(ship != null) { // Check if a ship was found, otherwise just skip until next timestep and check again
					state.assignFlagCarrierOneID(ship.getId());
				}
			}
			
			/* It may not be neccessary the flag carrier in close proximity to convient base
			if(state.getBaseBuilderID() == null) { // Need to give state base builder ID
				state.assignBaseBuilder(WorldKnowledge.getBaseBuilder(space, state).getId());
			}
			*/
			
			Ship ship = (Ship) space.getObjectById(shipID);
			state.assignShipToResourceCount(ship.getId(), ship.getResources().getTotal()); // Initialize intial resource count to ship current cargo state
			emptyShipActionQueue(shipID);
			
			// Low energy state...
			if(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) { // Need to attain energy as priority in plan
				AbstractObject energySource = WorldKnowledge.getClosestEnergySource(space, ship, state);
				
				if(energySource != null) {
					if(energySource instanceof Base) { // Base Type
						state.assignBaseToShip(shipID, energySource.getId());
						shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()));
					}
					else { // Beacon Type
						state.assignBeaconToShip(shipID, energySource.getId());
						shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()));
					}
				}
			}
			
			// Need to assign a location to sit while waiting for flag spawn
			
			if(!(ship.isCarryingFlag()) && shipID.equals(state.getFlagCarrierOneID())) { // So if we have flag carrier not carrying flag
				Flag flag = WorldKnowledge.getOtherTeamFlag(space);
				shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.GET_FLAG, flag.getId()));
				
				Base closestBase = WorldKnowledge.getClosestFriendlyBase(space, flag.getPosition()); // Get base to return to...
				shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.RETURN_TO_BASE, closestBase.getId()));
				state.assignBaseToShip(shipID, closestBase.getId());
			}
			else { // Just do asteroid gathering as usual...
				asteroidGathering(space, shipID);
			}
		}
		
		System.out.println("Plan for: " + shipID);
		
		for(HighLevelAction a : shipToActionQueue.get(shipID)) {
			System.out.println(shipID + " : " + a.actionType + "," + a.goalObject);
		}
	}
	
	/**
	 * Function will get the action ship needs to perform in plan...
	 * It will allow for contingency plans under certain circumstances such 
	 * as low energy
	 * 
	 * @param shipID	the <code>UUID</code> of the ship
	 * @return
	 */
	public AbstractAction getShipAction(Toroidal2DPhysics space, UUID shipID) {
		Ship ship = (Ship) space.getObjectById(shipID);
		AbstractObject goalObject;
		AbstractAction action = new DoNothingAction();
		
		HighLevelAction highLevelAction = shipToActionQueue.get(shipID).peek();
		
		// One of the events that will trigger a contingency plan... Low Energy state...
		if(highLevelAction == null || ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD 
				&& !(highLevelAction.actionType == ActionEnum.GET_ENERGY)) {
			System.out.println("Contingency plan... Need energy... Ship: " + shipID);
			AbstractObject energySource = WorldKnowledge.getClosestEnergySource(space, ship, state);
			
			if(energySource != null) {
				if(energySource instanceof Base) { // Base Type
					state.assignBaseToShip(shipID, energySource.getId());
					((LinkedList<HighLevelAction>) shipToActionQueue.get(shipID)).
							addFirst(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()));
				}
				else { // Beacon Type
					state.assignBeaconToShip(shipID, energySource.getId());
					state.assignBaseToShip(shipID, energySource.getId());
					((LinkedList<HighLevelAction>) shipToActionQueue.get(shipID)).
							addFirst(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()));
				}
				
				highLevelAction = shipToActionQueue.get(shipID).peek(); // Recheck the top of queue
			}
			
			System.out.println("New Plan for: " + shipID);
			
			for(HighLevelAction a : shipToActionQueue.get(shipID)) {
				System.out.println(shipID + " : " + a.actionType + "," + a.goalObject);
			}
		}
		
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
			else if(highLevelAction.actionType == ActionEnum.GET_ENERGY) { // Got to get energy
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstacles(space, ship));
				}
				action = state.getTeamMemberAction(space, ship);
			}
			else if(highLevelAction.actionType == ActionEnum.GET_FLAG) { // Getting a flag
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstacles(space, ship));
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
		
		if(action != null) {
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
					
					if(shipID.equals(state.getFlagCarrierOneID())) {
						state.unassignFlagCarrierOne();
					}
					
					formulatePlan(space, shipID);
				}
			}
			else if(action.actionType == ActionEnum.GET_ENERGY) { // Check if energy source is still alive
				AbstractObject energySource = space.getObjectById(action.goalObject);
				Ship ship = (Ship) space.getObjectById(shipID);
				
				if(energySource instanceof Base) { // Base Type
					if((ship == null || !(ship.isAlive())) || ship.getResources().getTotal() == 0 && 
							space.findShortestDistance(ship.getPosition(), energySource.getPosition()) < StateRepresentation.HIT_BASE_DISTANCE 
							&& !(ship.isCarryingFlag())) {
						state.unassignBaseToShip(energySource.getId());
						shipToActionQueue.get(shipID).remove();
					}
				}
				else { // Beacon Type
					if((ship == null || !(ship.isAlive())) || energySource == null || !(energySource.isAlive())) {
						state.unassignBeaconToShip(energySource.getId());
						shipToActionQueue.get(shipID).remove();
					}
				}
			}
			else if(action.actionType == ActionEnum.GET_FLAG) {
				Ship ship = (Ship) space.getObjectById(shipID);
				
				if(ship.isCarryingFlag()) {
					shipToActionQueue.get(shipID).remove();
				}
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
	 * Function will undo the affects of ship's action...
	 * 
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
				else if(action.actionType == ActionEnum.GET_ENERGY) {
					AbstractObject energySource = space.getObjectById(action.goalObject);
					
					if(energySource instanceof Base) {
						state.unassignBaseToShip(energySource.getId());
					}
					else {
						state.unassignBeaconToShip(energySource.getId());
					}
				}
				else if(action.actionType == ActionEnum.GET_FLAG) {
					state.unassignFlagCarrierOne();
				}
			}
		}
		emptyShipActionQueue(shipID); // Give new queue that is empty
	}
	
	/**
	 * Function will set the asteroid gathering phase, which dictates'
	 * the strategy the planner uses
	 * 
	 * @param value	the new value for flag
	 */
	public void setAsteroidGatheringPhase(boolean value) {
		ASTEROID_GATHERING_PHASE = value;
	}
	
	/**
	 * Helper function that will assign ship to a asteroid gathering plan
	 * 
	 * @param space	a reference to space
	 * @param shipID	ID of ship that will be assigned plan
	 */
	private void asteroidGathering(Toroidal2DPhysics space, UUID shipID) {
		Ship ship = (Ship) space.getObjectById(shipID);
		state.assignShipToResourceCount(ship.getId(), ship.getResources().getTotal()); // Initialize intial resource count to ship current cargo state
		emptyShipActionQueue(shipID);
		
		if(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) { // Need to attain energy as priority in plan
			AbstractObject energySource = WorldKnowledge.getClosestEnergySource(space, ship, state);
			
			if(energySource != null) {
				if(energySource instanceof Base) { // Base Type
					state.assignBaseToShip(shipID, energySource.getId());
					shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()));
				}
				else { // Beacon Type
					state.assignBeaconToShip(shipID, energySource.getId());
					shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()));
				}
			}
		}
		
		Asteroid closestAsteroid;
		// Check if ship can get full cargo and also check if there are more asteroids to even assign...
		while((ship.getResources().getTotal() < WorldKnowledge.RESOURCE_THRESHOLD 
			&& state.getResourceCount(shipID) < WorldKnowledge.RESOURCE_THRESHOLD) 
			&& state.getNumberOfAssignedAsteroids() < WorldKnowledge.getMineableAsteroids(space).size()) {
			closestAsteroid = WorldKnowledge.getClosestAsteroid(space, ship, state); // Applies preconditions...
			if(closestAsteroid != null) {
				state.assignAsteroidToShip(space, shipID, closestAsteroid.getId()); // Applies affect... Mutate state
				shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.GET_ASTEROID, closestAsteroid.getId())); // Puts in action... 
			}
			else { // Could be a lot of ships, but very little amount of asteroids...
				break; // Means we have run out of asteroids
			}
		}
		// TODO: Adjust for the fact that ship won't be at initial location...
		
		// Return to base...
		Base closestBase = WorldKnowledge.getClosestFriendlyBase(space, ship.getPosition()); // Get base to return to...
		shipToActionQueue.get(shipID).offer(new HighLevelAction(ActionEnum.RETURN_TO_BASE, closestBase.getId()));
		state.assignBaseToShip(shipID, closestBase.getId());
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
