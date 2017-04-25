package dani6621;

import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import dani6621.GraphSearch.GraphSearchNode;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * The class is designed to guide the agent along a path that
 * was solved by a graph search. It will store the path in a 
 * stack corresponding to a node. As it travels the path it
 * will 'pop' the stack and when within a certain distance to
 * objective, it will simply go to it like a reflex agent.
 */
public class Navigator {
	
	/**
	 * Set value for graphcis debugging
	 */
	private final boolean DEBUG_MODE;
	
	/**
	 * The minimal path needed to pursue object
	 */
	private static final int MINIMAL_PATH = 3;
	
	/**
	 * Abstraction of the navigation problem as 
	 * a graph data structure
	 */
	public NavigationMap map;
	
	/**
	 * The path to the objective (i.e goal)
	 */
	private Stack<GraphSearchNode> path;
	
	/**
	 * Current node the ship is pursuing
	 */
	private GraphSearchNode currentTargetNode;
	
	/**
	 * The goal stores as an <code>AbstractObject</code>. This
	 * is useful when object is moving and ship needs to
	 * intercept it.
	 */
	private AbstractObject goalObject;
	
	/**
	 * Exception designed when navigation fails (i.e search fails)
	 */
	public class NavigationFailureException extends RuntimeException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3826713121968922686L;

		/**
		 * 
		 */
		public NavigationFailureException() {
			super();
		}
		
		/**
		 * 
		 * @param msg
		 */
		public NavigationFailureException(String msg) {
			super(msg);
		}
	}
	
	/**
	 * Initializes the object 
	 * 
	 * @param debug	allow graphcis display for debug
	 */
	public Navigator(boolean debug) {
		currentTargetNode = null;
		DEBUG_MODE = debug;
	}
	
	/**
	 * Function will return a <code>AbstractAction</code> that allows the agent 
	 * to follow the path. It will continue to 'pop' the stack until empty.
	 * 
	 * @param space the reference to game space used for utility functions
	 * @param ship the ship that is transversing the path
	 * @return an action the ship will take to follow the path stored
	 */
	public AbstractAction retrieveNavigationAction(Toroidal2DPhysics space, Ship ship) {
		
		// If ship is close enough to goal, then simply go straight to goal or if the object has n0 obstacles impeding it
		if((goalObject != null && space.findShortestDistance(goalObject.getPosition(), 
				ship.getPosition()) < NavigationMap.SPACING * 2.0 &&
				path.size() < MINIMAL_PATH) || (goalObject != null && space.
				isPathClearOfObstructions(ship.getPosition(), goalObject.getPosition(), 
				WorldKnowledge.getAllObstacles(space, ship), NavigationMap.CLOSE_DISTANCE))) {
			return new MoveAction(space, ship.getPosition(), goalObject.getPosition(),
					WorldKnowledge.calculateInterceptVelocity(space, ship, goalObject));
		}
		
		// Check if the current path is empty or no path formed at all
		if(path != null && !(path.isEmpty())) {
			
			if(currentTargetNode == null) { // Assign a new node
				currentTargetNode = path.pop();
				return new MoveAction(space, ship.getPosition(), currentTargetNode.node.position,
						WorldKnowledge.calculateVelocity(space, ship, currentTargetNode.node.position));
			}
			else if(space.findShortestDistance(ship.getPosition(), currentTargetNode.node.position) < (NavigationMap.SPACING / 2)) {
				currentTargetNode = path.pop();
				return new MoveAction(space, ship.getPosition(), currentTargetNode.node.position,
						WorldKnowledge.calculateVelocity(space, ship, currentTargetNode.node.position));
			}
			else {
				return new MoveAction(space, ship.getPosition(), currentTargetNode.node.position,
						WorldKnowledge.calculateVelocity(space, ship, currentTargetNode.node.position));
			}
		}
		else {
			return new DoNothingAction(); // Can't do anything if search fails
		}
	}
	
	/**
	 * Function will generate a path to the objective using a 
	 * <code>GraphSearch</code> function that implements 'A*' search
	 * algorithms.
	 * 
	 * @param space the reference to game space used for utility functions
	 * @param ship the ship that is transversing the path
	 * @param goal the goal the ship needs to reach
	 * @param obstacles the obstacles to avoid
	 */
	public void generateAStarPath(Toroidal2DPhysics space, AbstractObject ship, 
			AbstractObject goal, Set<AbstractObject> obstacles) {
		
		currentTargetNode = null;
		map = new NavigationMap(space, DEBUG_MODE); // Generate graph for problem
		goalObject = goal;
		
		if(goalObject == null) {
			throw new NavigationFailureException("Navigation failed! No object was specified!");
		}
		
		GraphSearch graphSearch = new GraphSearch(map, ship, goal.getPosition()); // Give search parameters
		
		try {
			path = graphSearch.aStarSearch(obstacles); // Generate a path
		}
		catch(GraphSearch.SearchFailureException e) {
			path = new Stack<GraphSearchNode>();
		}
	}
	
	/**
	 * Function will generate a path to the objective using a 
	 * <code>GraphSearch</code> function that implements 'A*' search
	 * algorithms.
	 * 
	 * @param space the reference to game space used for utility functions
	 * @param ship the ship that is transversing the path
	 * @param goal the goal the ship needs to reach
	 * @param obstacles the obstacles to avoid
	 */
	public void generateAStarPath(Toroidal2DPhysics space, AbstractObject ship, 
			Position goal, Set<AbstractObject> obstacles) {
		
		currentTargetNode = null;
		map = new NavigationMap(space, DEBUG_MODE); // Generate graph for problem
		goalObject = null;
		
		GraphSearch graphSearch = new GraphSearch(map, ship, goal); // Give search parameters
		
		try {
			path = graphSearch.aStarSearch(obstacles); // Generate a path
		}
		catch(GraphSearch.SearchFailureException e) {
			path = new Stack<GraphSearchNode>();
		}
	}
	
	/**
	 * Function will generate a path to the objective using a 
	 * <code>GraphSearch</code> object that can employ graph search
	 * algorithms.  This particular search is greedy best first search
	 * 
	 * @param space the reference to game space used for utility functions
	 * @param ship the ship that is transversing the path
	 * @param goal the goal the ship needs to reach
	 */
	public void generateGreedyBFPath(Toroidal2DPhysics space, AbstractObject ship, AbstractObject goal) {
		currentTargetNode = null;
		map = new NavigationMap(space, DEBUG_MODE); // Generate graph for problem
		goalObject = goal;
		GraphSearch graphSearch = new GraphSearch(map, ship, goal.getPosition()); // Give search parameters
		
		try {
			path = graphSearch.greedyBFSearch(); // Generate a path
		}
		catch(GraphSearch.SearchFailureException e) {
			path = new Stack<GraphSearchNode>(); // Generate an empty stack
		}
	}
	
	/**
	 * Function will return a copy of the path contained in the object.
	 * Used mainly for debugging.
	 * 
	 * @return a <code>List</code> of <code>GraphSearchNode</code> objects
	 */
	public List<GraphSearchNode> getCopyPath() {
		if(path == null) 
			return null;
		return (List<GraphSearchNode>) path.clone();
	}
	
	/**
	 * Function retrieves the UUID of goal object
	 * 
	 * @return	the <code>UUID</code> of goal object
	 * 			NOTE: This can return <code>null</code>
	 */
	public UUID getGoalObjectUUID() {
		UUID id = null;
		if(goalObject != null)
			id = goalObject.getId();
		return id;
	}
	
}
