package dani6621;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import dani6621.NavigationMap.NavigationVertex;
import dani6621.NavigationMap.NavigationVertexKey;
import spacesettlers.objects.AbstractObject;

/**
 * Class will contain data members such as initial state (node), 
 * goal state (node), and the cost value accumulation. It will
 * implement an inner class called 'AStarNode' that will keep track 
 * of the parent and the cost to get to the node. This will allow 
 * the implementation of a <code>Comparator</code> for the 
 * <code>PriorityQueue</code> used to search the space.
 * 
 * NOTE: Don't even know if it is logical to create another 
 *       class, but it seems difficult to try to compare 
 *       <code>NavigationNode</code> itself. It also doesn't 
 *       make a lot of sense to bloat <code>NavigationNode</code>
 *       class.
 *
 */
public class AStar {
	
	/**
	 * Custom exception class that will be used in the instance 
	 * the search fails (however rarely)
	 *
	 */
	public class AStarSearchFailureException extends RuntimeException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6242761421647354639L;

		/**
		 * Blank constructor that will use default message
		 */
		public AStarSearchFailureException() {
			super();
		}
		
		/**
		 * Constructor creates exception to hold passed message
		 * 
		 * @param msg the message of the exception
		 */
		public AStarSearchFailureException(String msg) {
			super(msg);
		}
		
	}
	
	/**
	 * Inner class that will allow easy comparisons between 
	 * nodes. It will maintain a reference to the node it 
	 * is referring too and it will also contain the cost
	 * for the particular node.
	 *
	 */
	public class AStarNode {

		/**
		 * The parent of the node to help with 
		 * backtracking
		 */
		public AStarNode parent;
		
		/**
		 * Reference to <code>NavigationVertex</code>
		 */
		public NavigationVertex node;
		
		/**
		 * Total cost of node
		 */
		public int fCost;
		
		/**
		 * The cost to node
		 */
		public int gCost;
		
		/**
		 * The heuristic cost for node
		 */
		public int hCost;
		
		/**
		 * 
		 * @param node
		 */
		public AStarNode(NavigationVertex vertex) {
			parent = null;
			node = vertex;
		}
		
		/**
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			return result;
		}
		
		/**
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AStarNode other = (AStarNode) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (node == null) {
				if (other.node != null)
					return false;
			} else if (!node.equals(other.node))
				return false;
			return true;
		}

		private AStar getOuterType() {
			return AStar.this;
		}
	}
	
	/**
	 * The initial start of the search
	 */
	private AStarNode initialNode;
	
	/**
	 * The goal of the search
	 */
	private AStarNode goalNode;
	
	/**
	 * The graph that will be searched
	 */
	private NavigationMap map;
	
	/**
	 * Initialize the <code>AStar</code> with initial node and goal node. It also
	 * gives reference to the graph it will search.
	 * 
	 * @param gameMap the graph that the algorithm will search
	 * @param ship the ship object to get location
	 * @param object the object to get the location
	 */
	public AStar(NavigationMap gameMap, AbstractObject ship, AbstractObject object) {
		map = gameMap; // Give reference to current game map
		
		// Get vertices closest to objects to help initialize search
		NavigationVertex shipVertex = map.findNearestVertex(ship);
		NavigationVertex objectVertex = map.findNearestVertex(object);
		
		goalNode = new AStarNode(objectVertex);
		goalNode.hCost = 0;	// Goal node heuristic is zero
		
		initialNode = new AStarNode(shipVertex);
		initialNode.hCost = map.calculateHeuristic(initialNode.node, goalNode.node);
		initialNode.gCost = 0; // Initial node has 0 step cost
		initialNode.fCost = initialNode.gCost + initialNode.hCost;
	}
	
	/**
	 * Function will search for a solution (i.e a path) given the data members
	 * 
	 * @return an <code>AStarNode</code> who can be recursively iterated to 
	 * 			generate a path
	 * @throws AStarSearchFailureException any instance where the search fails due to
	 * 										a multitude of reasons
	 */
	public AStarNode search() throws AStarSearchFailureException {
		Set<AStarNode> closed = new HashSet<AStarNode>();
		PriorityQueue<AStarNode> open = new PriorityQueue<AStarNode>(new Comparator<AStarNode>() {

			@Override
			public int compare(AStarNode arg0, AStarNode arg1) {
				return Integer.compare(arg0.fCost, arg1.fCost);
			}
		});
		
		List<Graph<NavigationVertexKey, NavigationVertex>.Edge> neighbors = map.getNeighbors(initialNode.node);
		AStarNode newNode;
		AStarNode nextNode;
		
		// Add children of initial node to frontier
		for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
			newNode = new AStarNode(edge.endVertex.data);
			newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node); // Calculate heuristic
			newNode.parent = initialNode; // Set parent to initial node
			newNode.gCost = edge.weight + newNode.parent.gCost; // Add path cost
			newNode.fCost = newNode.gCost + newNode.hCost; // Calculate total cost
			open.add(newNode); // Add to queue  
		}
		
		while(true) { // Will need to change for 'Spacewars' 
			if(open.isEmpty()) {
				throw new AStarSearchFailureException("A* Search Failed! The frontier was empty!");
			}
			
			nextNode = open.poll(); // Get head of priority queue
			
			if(nextNode.equals(goalNode)) { // If it is a goal node then return solution
				return nextNode;
			}
			
			if(!(closed.contains(nextNode))) { // If closed does not contain the explored node
				closed.add(nextNode); // Add explored node to the closed set
				neighbors = map.getNeighbors(nextNode.node);
				
				for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
					newNode = new AStarNode(edge.endVertex.data);
					
					if(!(closed.contains(newNode)) && 
							!(open.contains(newNode))) {
						newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node);
						newNode.parent = nextNode;
						newNode.gCost = edge.weight + newNode.parent.gCost;
						newNode.fCost = newNode.gCost + newNode.hCost;
						open.add(newNode);
					}
				}
			}
		}
		
	}
}