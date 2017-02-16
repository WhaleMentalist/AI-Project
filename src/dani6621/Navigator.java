package dani6621;

import java.util.*;

import dani6621.GraphSearch.GraphSearchNode;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * The class is designed to guide the agent along a path that
 * was solved by a graph search. It will store the path in a 
 * stack corresponding to a node. As it travels the path it
 * will 'pop' the stack and when within a certain distance to
 * objective, it will simply go to it like a reflex agent.
 */
public class Navigator {
	
	/**
	 * Abstraction of the navigation problem as 
	 * a graph data structure
	 */
	private NavigationMap map;
	
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
	 * Initializes the object 
	 */
	public Navigator() {
		currentTargetNode = null;
	}
	
	/**
	 * Function will return a <code>AbstractAction</code> that allows the agent 
	 * to follow the path. It will continue to 'pop' the stack until empty.
	 * 
	 * @param space the reference to game space used for utility functions
	 * @param knowledge the reference to knowledge representation used for utility functions
	 * @param ship the ship that is transversing the path
	 * @return an action the ship will take to follow the path stored
	 */
	public AbstractAction retrieveNavigationAction(Toroidal2DPhysics space, WorldState knowledge, Ship ship) {
		
		// If ship is close enough to goal, then simply go straight to goal
		if(space.findShortestDistance(goalObject.getPosition(), ship.getPosition()) < NavigationMap.SPACING * 2.0) {
			return new MoveAction(space, ship.getPosition(), goalObject.getPosition(),
					knowledge.calculateInterceptVelocity(goalObject));
		}
		
		// Check if the current path is empty
		if(!(path.isEmpty())) {
			
			if(currentTargetNode == null) { // Assign a new node
				currentTargetNode = path.pop();
				return new MoveAction(space, ship.getPosition(), currentTargetNode.node.position,
						knowledge.calculateVelocity(currentTargetNode.node.position));
			}
			else if(space.findShortestDistance(ship.getPosition(), currentTargetNode.node.position) < (NavigationMap.SPACING / 2)) {
				currentTargetNode = path.pop();
				return new MoveAction(space, ship.getPosition(), currentTargetNode.node.position,
						knowledge.calculateVelocity(currentTargetNode.node.position));
			}
			else {
				return new MoveAction(space, ship.getPosition(), currentTargetNode.node.position,
						knowledge.calculateVelocity(currentTargetNode.node.position));
			}
		}
		else {
			return new DoNothingAction(); // Can't do anything if search fails
		}
	}
	
	/**
	 * Function will generate a path to the objective using a 
	 * <code>GraphSearch</code> object that can employ graph search
	 * algorithms.
	 * 
	 * @param space the reference to game space used for utility functions
	 * @param knowledge the reference to knowledge representation used for utility functions
	 * @param ship the ship that is transversing the path
	 * @param goal the goal the ship needs to reach
	 */
	public void generatePath(Toroidal2DPhysics space, WorldState knowledge, AbstractObject ship, AbstractObject goal) {
		currentTargetNode = null;
		map = new NavigationMap(space, knowledge); // Generate graph for problem
		goalObject = goal;
		GraphSearch graphSearch = new GraphSearch(map, ship, goal); // Give search parameters
		
		try {
			path = graphSearch.aStarSearch(); // Generate a path
		}
		catch(GraphSearch.AStarSearchFailureException e) {
			System.out.println(e.getMessage());
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
		return (List<GraphSearchNode>) path.clone();
	}
	
}
