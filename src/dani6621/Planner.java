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
		GET_FLAG, GET_ASTEROID, RETURN_TO_BASE, 
		LOITER_AT_LOCATION, GET_ENERGY;
	}
	
	/**
	 * 
	 */
	private boolean isFlagDead;

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
		PlanSearchNode root = null; // Hold reference to root
		PlanSearchNode currentNode = null; // Hold reference to current node
		
		state.assignShipToResourceCount(ship.getId(), ship.getResources().getTotal()); // Initialize intial resource count to ship current cargo state
		emptyShipActionQueue(shipID);
		
		if(ASTEROID_GATHERING_PHASE) { // Gather asteroids to get items that optimize flag gathering
			root = new PlanSearchNode(state); // Create a 'root' of the tree with state as initial state... Notice it will carry mutations of previous searches...
			currentNode = asteroidGathering(space, shipID, root); // Hold reference to current node in search
		}
		else { // Flag gathering phase
			// Where we assign flag gathering and base building to optimize flag count
			if(state.getFlagCarrierOneID() == null) { // Need to give state flag carrier ID
				Ship flagCarrier = WorldKnowledge.getFlagCarrier(space, state);
				if(ship != null) { // Check if a ship was found, otherwise just skip until next timestep and check again
					state.assignFlagCarrierOneID(flagCarrier.getId());
				}
			}
						
			// Need to assign second flag carrier
			if(state.getFlagCarrierTwoID() == null) {
				Ship flagCarrier = WorldKnowledge.getFlagCarrier(space, state);
				if(ship != null) {
					state.assignFlagCarrierTwo(flagCarrier.getId());
				}
			}
			
			root = new PlanSearchNode(state); // Create a 'root' of the tree with state as initial state... Notice it will carry mutations of previous searches...
			currentNode = root; // Hold reference to current node in search
			
			if(shipID.equals(state.getFlagCarrierOneID()) || shipID.equals(state.getFlagCarrierTwoID())) { // Flag carriers
				Flag otherTeamFlag = WorldKnowledge.getOtherTeamFlag(space);
				while(!(currentNode.state.isGoalState(space))) {
					
					if(currentNode.parent != null && (currentNode.parent.edge.edgeValue.actionType == ActionEnum.GET_ENERGY ||
							currentNode.parent.edge.edgeValue.actionType == ActionEnum.LOITER_AT_LOCATION)) {
						break;
					}
					
					for(ActionEnum action : ActionEnum.values()) {
						if(action == ActionEnum.GET_FLAG) { // Get flag action
							if(isClosestToFlag(space, shipID) && state.getCurrentFlagCarrier() == null) { // If it is closer and the flag isn't being carried
								state.setCurrentFlagCarrier(shipID); // Now have flag
								currentNode.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.GET_FLAG, otherTeamFlag.getId()), 
										new PlanSearchNode(currentNode, state));
								currentNode = currentNode.edge.endNode;
								break;
							}
						}
						else if(action == ActionEnum.RETURN_TO_BASE) { // Return to base action
							Base closestBase;
							if(shipID.equals(state.getCurrentFlagCarrier())) { // If it is carrying the flag
								state.incrementTotalFlag(); // Increment the amount of flags
								state.setCurrentFlagCarrier(null); // No one is carrying the flag now
								closestBase = WorldKnowledge.getClosestFriendlyBase(space, ship.getPosition());
								currentNode.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.RETURN_TO_BASE, closestBase.getId()), 
										new PlanSearchNode(currentNode, state));
								currentNode = currentNode.edge.endNode;
								break;
							}
						}
						else if(action == ActionEnum.LOITER_AT_LOCATION) {
							if(!(isClosestToFlag(space, shipID)) && !(shipID.equals(state.getCurrentFlagCarrier()))) {
								if(shipID.equals(state.getFlagCarrierOneID())) {
									currentNode.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.LOITER_AT_LOCATION, state.getConvientBaseBuildingLocations()[0]), 
											new PlanSearchNode(currentNode, state));
									currentNode = currentNode.edge.endNode;
									break;
								}
								else {
									currentNode.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.LOITER_AT_LOCATION, state.getConvientBaseBuildingLocations()[1]), 
											new PlanSearchNode(currentNode, state));
									currentNode = currentNode.edge.endNode;
									break;
								}
							}
						}
						else {
							continue;
						}
					}
				}
			}
			else { // Non-flag carrier
				root = new PlanSearchNode(state); // Create a 'root' of the tree with state as initial state... Notice it will carry mutations of previous searches...
				currentNode = asteroidGathering(space, shipID, root); // Hold reference to current node in search
			}
			
		}
		
		while(currentNode != null && currentNode.parent != null) {
				((LinkedList<HighLevelAction>) shipToActionQueue.get(shipID)).addFirst(currentNode.parent.edge.edgeValue);
			currentNode = currentNode.parent;
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
		Position goalPosition;
		AbstractAction action = new DoNothingAction();
		
		HighLevelAction highLevelAction = shipToActionQueue.get(shipID).peek();
		
		// One of the events that will trigger a contingency plan... Low Energy state...
		if((highLevelAction == null && ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) || 
				(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD && !(highLevelAction.actionType == ActionEnum.GET_ENERGY))) {
			// System.out.println("Contingency plan... Need energy... Ship: " + shipID);
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
		}
		
		// Check for 'null'
		if(highLevelAction != null) {
			if(highLevelAction.actionType == ActionEnum.GET_ASTEROID) { // Need to get asteroid
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0 && goalObject != null) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstacles(space, ship));
				}
				action = state.getTeamMemberAction(space, ship, false);
			}
			else if(highLevelAction.actionType == ActionEnum.RETURN_TO_BASE) { // Return to base
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship));
				}
				action = state.getTeamMemberAction(space, ship, false);
			}
			else if(highLevelAction.actionType == ActionEnum.GET_ENERGY) { // Got to get energy
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstacles(space, ship));
				}
				action = state.getTeamMemberAction(space, ship, false);
			}
			else if(highLevelAction.actionType == ActionEnum.GET_FLAG) { // Getting a flag
				goalObject = space.getObjectById(highLevelAction.goalObject);
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0) {
					state.generateTeamMemberPath(space, ship, goalObject.getPosition(), WorldKnowledge.getAllObstacles(space, ship));
				}
				action = state.getTeamMemberAction(space, ship, false);
			}
			else if(highLevelAction.actionType == ActionEnum.LOITER_AT_LOCATION) { // Just waiting around
				goalPosition = highLevelAction.goalPosition;
				if(space.getCurrentTimestep() % MultiShipAgent.NAVIGATION_REPLAN_TIMESTEP == 0) {
					state.generateTeamMemberPath(space, ship, goalPosition, WorldKnowledge.getAllObstacles(space, ship));
				}
				action = state.getTeamMemberAction(space, ship, true);
			}
		}
		else {
			formulatePlan(space, shipID); // Need to create a new plan since we ran out of actions
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
					state.unassignBaseToShip(base.getId());
					shipToActionQueue.get(shipID).remove();
					
					if(!(WorldKnowledge.getOtherTeamFlag(space).isAlive())) { // Means we need to replan flag carriers
						isFlagDead = true;
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
	 * 
	 * @param space
	 */
	public void checkReplanFlagCarriers(Toroidal2DPhysics space) {
		if(isFlagDead && !(ASTEROID_GATHERING_PHASE)) {
			if(WorldKnowledge.getOtherTeamFlag(space).isAlive()) { // So flag has repawned... Now we can replan
				if(state.getFlagCarrierOneID() != null) {
					formulatePlan(space, state.getFlagCarrierOneID());
				}
				
				if(state.getFlagCarrierTwoID() != null) {
					formulatePlan(space, state.getFlagCarrierTwoID());
				}
				isFlagDead = false;
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
	 * 
	 * @return
	 */
	public boolean getAsteroidGatheringPhase() {
		return ASTEROID_GATHERING_PHASE;
	}
	
	/**
	 * Helper function that will assign ship to a asteroid gathering plan
	 * 
	 * @param space	a reference to space
	 * @param shipID	ID of ship that will be assigned plan
	 */
	private PlanSearchNode asteroidGathering(Toroidal2DPhysics space, UUID shipID, PlanSearchNode root) {
		Ship ship = (Ship) space.getObjectById(shipID);
		while(!(root.state.isGoalState(space))) {
			
			if(root.parent != null && root.parent.edge.edgeValue.actionType == ActionEnum.GET_ENERGY) {
				break;
			}
			
			// So consider each action in order of 'enum', which has it in priority... The search is DFS...
			for(ActionEnum action : ActionEnum.values()) {
				// Start by applying preconditions and checking state
				if(action == ActionEnum.GET_FLAG) {
					continue; // No flag gathering during 'Asteroid Gathering Phase'... A precondition
				}
				else if(action == ActionEnum.GET_ASTEROID && 
						((!(state.getNumberOfAssignedAsteroids() >= WorldKnowledge.getMineableAsteroids(space).size()) &&
						ship.getEnergy() >= WorldKnowledge.ENERGY_THRESHOLD) || !(ship.isCarryingFlag()))) { // Get asteroid
					Asteroid closestAsteroid;
					// Ship may get asteroid if it is NOT at full capacity and number of assigned asteroids is not exceeded
					if(state.getResourceCount(shipID) < WorldKnowledge.RESOURCE_THRESHOLD) {
						closestAsteroid = WorldKnowledge.getClosestAsteroid(space, ship, state); // Also applies precondition of unassigned asteroid...
						if(closestAsteroid != null) { // Found an asteroid that was unassigned!
							state.assignAsteroidToShip(space, shipID, closestAsteroid.getId()); // Mutate state by applying effect...
							root.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.GET_ASTEROID, closestAsteroid.getId()), new PlanSearchNode(root, state));
							root = root.edge.endNode;
							break;
						}
					}
				}
				else if(action == ActionEnum.RETURN_TO_BASE) { // Return to base with resources
					Base closestBase;
					if(((state.getResourceCount(shipID) >= WorldKnowledge.RESOURCE_THRESHOLD || 
							state.getNumberOfAssignedAsteroids() >= WorldKnowledge.getMineableAsteroids(space).size()) &&
							ship.getEnergy() >= WorldKnowledge.ENERGY_THRESHOLD) || ship.isCarryingFlag()) { // Got ship with full cargo
						closestBase = WorldKnowledge.getClosestFriendlyBase(space, ship.getPosition());
						state.assignBaseToShip(shipID, closestBase.getId()); // Mutate state by applying effect...
						root.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.RETURN_TO_BASE, closestBase.getId()), new PlanSearchNode(root, state));
						root = root.edge.endNode;
						break;
					}
				}
				else if(action == ActionEnum.GET_ENERGY) {
					if(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) {
						AbstractObject energySource = WorldKnowledge.getClosestEnergySource(space, ship, state);
						if(energySource != null) {
							if(energySource instanceof Base) { // Base Type
								state.assignBaseToShip(shipID, energySource.getId());
								root.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()), 
										new PlanSearchNode(root, state));
								root = root.edge.endNode;
							}
							else { // Beacon Type
								state.assignBeaconToShip(shipID, energySource.getId());
								root.edge = new PlanSearchEdge(new HighLevelAction(ActionEnum.GET_ENERGY, energySource.getId()), 
										new PlanSearchNode(root, state));
								root = root.edge.endNode;
							}
						}
						break;
					}
				}
				else {
					continue; // Other actions will not be considered as guided by the precondition of being in asteroid gathering phase
				}
			}
		}	
		return root;
	}
	
	/**
	 * Function returns location that is closest to loiter location for ship
	 * 
	 * @param space	a reference to space
	 * @param shipID 	passes a ship ID to compare
	 * @return	an <code>int</code> representing the indice choice in convient base location, which
	 * 				is a loiter point for ships
	 */
	private boolean isClosestToFlag(Toroidal2DPhysics space, UUID shipID) {
		Position[] convientBaseLocation = state.getConvientBaseBuildingLocations();
		Flag flag = WorldKnowledge.getOtherTeamFlag(space);
		boolean result = false;
		
		if(space.findShortestDistance(flag.getPosition(), convientBaseLocation[0]) 
				< space.findShortestDistance(flag.getPosition(), convientBaseLocation[1])) { // Flag carrier one
			if(shipID.equals(state.getFlagCarrierOneID())) {
				result = true;
			}
		}
		else { // Flag carrier two
			if(shipID.equals(state.getFlagCarrierTwoID())) {
				result = true;
			}
		}
		
		return result;
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
	
	/**
	 * Since the planner employs a search... We will need 
	 * a node implmentation
	 *
	 */
	public class PlanSearchNode {
		
		/**
		 * Reference to parent
		 */
		public PlanSearchNode parent;
		
		/**
		 * The state contained in the node... 
		 * It will allow algoirthm to check for goal state...
		 */
		public StateRepresentation state;
		
		/**
		 * Connects action to next node
		 */
		public PlanSearchEdge edge;
		
		/**
		 * Basic constructor
		 * 
		 * @param p	the parent
		 * @param s	the state
		 * @param e	the edge
		 */
		public PlanSearchNode(PlanSearchNode p, StateRepresentation s, PlanSearchEdge e) {
			parent = p;
			state = s;
			edge = e;
		}
		
		/**
		 * Basic constructor
		 * 
		 * @param p	the parent
		 * @param s	the state
		 */
		public PlanSearchNode(PlanSearchNode p, StateRepresentation s) {
			parent = p;
			state = s;
			edge = null;
		}
		
		/**
		 * Basic constructor
		 * 
		 * @param s	the state
		 * @param e	the edge
		 */
		public PlanSearchNode(StateRepresentation s, PlanSearchEdge e) {
			parent = null;
			state = s;
			edge = e;
		}
		
		/**
		 * Basic constructor 
		 * 
		 * @param s	the state
		 */
		public PlanSearchNode(StateRepresentation s) {
			parent = null;
			state = s;
			edge = null;
		}
	}
	
	/**
	 * Represents edge that will connect the nodes...
	 * Essentially the action...
	 *
	 */
	public class PlanSearchEdge {
		
		/**
		 * The high level action that connects nodes
		 */
		public HighLevelAction edgeValue;
		
		/**
		 * The node that is connected by the edge
		 */
		public PlanSearchNode endNode;
		
		/**
		 * Basic constructor
		 * 
		 * @param v	the edge value
		 * @param n	the node connected at end of node
		 */
		public PlanSearchEdge(HighLevelAction v, PlanSearchNode n) {
			edgeValue = v;
			endNode = n;
		}
	}
}
