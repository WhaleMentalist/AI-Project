package dani6621;

import java.util.*;


import dani6621.NavigationMap.NavigationVertex;
import dani6621.NavigationMap.NavigationVertexKey;
import spacesettlers.objects.AbstractObject;

/**
 * Class will contain data members such as initial state (node), 
 * goal state (node), and the cost value accumulation. It will
 * implement an inner class called 'GraphSearchNode' that will keep track 
 * of the parent and the cost to get to the node. This will allow 
 * the implementation of a <code>Comparator</code> for the 
 * <code>PriorityQueue</code> used to search the space. The class
 * will contain graph search methods such as A* to help the AI form
 * paths.
 *
 */
public class GraphSearch {
	
	/**
	 * Custom exception class that will be used in the instance 
	 * the search fails (however rarely)
	 *
	 */
	public class SearchFailureException extends RuntimeException {
		
		/**
		 * Get rid of annoying warninig. Not really useful for any other reasons
		 */
		private static final long serialVersionUID = -6242761421647354639L;

		/**
		 * Blank constructor that will use default message
		 */
		public SearchFailureException() {
			super();
		}
		
		/**
		 * Constructor creates exception to hold passed message
		 * 
		 * @param msg the message of the exception
		 */
		public SearchFailureException(String msg) {
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
	public class GraphSearchNode {

		/**
		 * The parent of the node to help with 
		 * backtracking
		 */
		public GraphSearchNode parent;
		
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
		 * Initialize object with vertex
		 * 
		 * @param vertex the vertex to set in the object
		 */
		public GraphSearchNode(NavigationVertex vertex) {
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
			GraphSearchNode other = (GraphSearchNode) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (node == null) {
				if (other.node != null)
					return false;
			} else if (!node.equals(other.node))
				return false;
			return true;
		}

		private GraphSearch getOuterType() {
			return GraphSearch.this;
		}
	}
	
	/**
	 * Experimental: Trying to find a depth that is NOT too long, 
	 * but doesn't tie down the capabilities of the AI
	 */
	private static final int MAX_DEPTH = 80;
	
	/**
	 * The initial start of the search
	 */
	private GraphSearchNode initialNode;
	
	/**
	 * The goal of the search
	 */
	private GraphSearchNode goalNode;
	
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
	public GraphSearch(NavigationMap gameMap, AbstractObject ship, AbstractObject object) {
		map = gameMap; // Give reference to current game map
		
		// Get vertices closest to objects to help initialize search
		NavigationVertex shipVertex = map.findNearestVertex(ship);
		NavigationVertex objectVertex = map.findNearestVertex(object);
		
		goalNode = new GraphSearchNode(objectVertex);
		goalNode.hCost = 0;	// Goal node heuristic is zero
		
		initialNode = new GraphSearchNode(shipVertex);
		initialNode.hCost = map.calculateHeuristic(initialNode.node, goalNode.node);
		initialNode.gCost = 0; // Initial node has 0 step cost
		initialNode.fCost = initialNode.gCost + initialNode.hCost;
	}
	
	/**
	 * Function will search for a solution (i.e a path) given the data members. Method is
	 * a bit large, but for the most part readable! It uses f(n) = g(n) + h(n)
	 * 
	 * @param obstacles the obstacles the search should avoid
	 * @return an <code>AStarNode</code> who can be recursively iterated to 
	 * 			generate a path
	 * @throws SearchFailureException any instance where the search fails due to
	 * 										a multitude of reasons
	 */
	public Stack<GraphSearchNode> aStarSearch(Set<AbstractObject> obstacles) throws SearchFailureException {
		int depth = 0; // Start at depth zero
		Set<GraphSearchNode> closed = new HashSet<GraphSearchNode>(); // Create list of explored nodes
		
		// Create a priority queue that is sorted by a anonymous class that compares nodes by the 'total cost'
		PriorityQueue<GraphSearchNode> open = new PriorityQueue<GraphSearchNode>(new Comparator<GraphSearchNode>() {
			@Override
			public int compare(GraphSearchNode arg0, GraphSearchNode arg1) {
				return Integer.compare(arg0.fCost, arg1.fCost);
			}
		});
		
		// Retrieve neighbors through an edge list
		List<Graph<NavigationVertexKey, NavigationVertex>.Edge> neighbors = map.getNeighbors(initialNode.node);
		GraphSearchNode newNode;
		GraphSearchNode nextNode;
		
		// Add children of initial node to frontier
		for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
			newNode = new GraphSearchNode(edge.endVertex.data); // Generate a search node to track costs
			newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node); // Calculate heuristic
			newNode.parent = initialNode; // Set parent to initial node
			newNode.gCost = edge.weight + newNode.parent.gCost; // Add path cost
			newNode.fCost = newNode.gCost + newNode.hCost; // Calculate total cost
			open.add(newNode); // Add to queue  
		}
		
		while(depth < MAX_DEPTH) { // Implementation specific to 'Spacewars', we don't want to sit and search too long
			
			// The explorable is empty! Something weird happened
			if(open.isEmpty()) {
				throw new SearchFailureException("A* Search Failed! The frontier was empty!");
			}
			
			nextNode = open.poll(); // Get head of priority queue
			
			if(nextNode.equals(goalNode)) { // If it is a goal node then return solution
				return generatePath(nextNode);
			}
			
			if(!(closed.contains(nextNode)) && !(map.isCloseToObstacle(nextNode.node))) { // If closed does not contain the explored node
				closed.add(nextNode); // Add explored node to the closed set
				neighbors = map.getNeighbors(nextNode.node);
				
				// Iterate through each neighbor
				for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
					newNode = new GraphSearchNode(edge.endVertex.data);
					
					if(!(closed.contains(newNode)) && 
							!(open.contains(newNode))) {
						// Again... Like up above we need to calculate costs for the algorithm to use
						newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node);
						newNode.parent = nextNode;
						newNode.gCost = edge.weight + newNode.parent.gCost;
						newNode.fCost = newNode.gCost + newNode.hCost;
						open.add(newNode);
					}
				}
			}
			++depth; // Explored a layer, so increase the current depth
		}
		throw new SearchFailureException("A* Search Failed! Maximum Depth Reached"); // Means search reached max depth
	}
	
	
	/**
	 * Function will search for a solution (i.e a path) given the data members. Method is
	 * a bit large, but for the most part readable!
	 * 
	 * @return a Stack of GraphSearchNode which can generate a path
	 * @throws SearchFailureException any instance where the search fails due to
	 * 										a multitude of reasons
	 */
	public Stack<GraphSearchNode> greedyBFSearch() throws SearchFailureException {
		int depth = 0; // Start at depth zero
		Set<GraphSearchNode> closed = new HashSet<GraphSearchNode>(); // Create list of explored nodes
		
		// Create a priority queue that is sorted by a anonymous class that compares nodes by the 'total cost'
		PriorityQueue<GraphSearchNode> open = new PriorityQueue<GraphSearchNode>(new Comparator<GraphSearchNode>() {

			@Override
			public int compare(GraphSearchNode arg0, GraphSearchNode arg1) {
				return Integer.compare(arg0.fCost, arg1.fCost);
			}
		});
		
		// Retrieve neighbors through an edge list
		List<Graph<NavigationVertexKey, NavigationVertex>.Edge> neighbors = map.getNeighbors(initialNode.node);
		GraphSearchNode newNode;
		GraphSearchNode nextNode;
		
		// Add children of initial node to frontier
		for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
			newNode = new GraphSearchNode(edge.endVertex.data); // Generate a search node to track costs
			newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node); // Calculate heuristic
			newNode.parent = initialNode; // Set parent to initial node
			newNode.gCost = edge.weight + newNode.parent.gCost; // Add path cost
			newNode.fCost = newNode.gCost + newNode.hCost; // Calculate total cost
			open.add(newNode); // Add to queue  
		}
		
		while(depth < MAX_DEPTH) { // Implementation specific to 'Spacewars', we don't want to sit and search too long
			
			// The explorable is empty! Something weird happened
			if(open.isEmpty()) {
				throw new SearchFailureException("A* Search Failed! The frontier was empty!");
			}
			
			nextNode = open.poll(); // Get head of priority queue
			
			if(nextNode.equals(goalNode)) { // If it is a goal node then return solution
				return generatePath(nextNode);
			}
			
			if(!(closed.contains(nextNode))) { // If closed does not contain the explored node
				closed.add(nextNode); // Add explored node to the closed set
				neighbors = map.getNeighbors(nextNode.node);
				
				// Iterate through each neighbor
				for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
					newNode = new GraphSearchNode(edge.endVertex.data);
					
					if(!(closed.contains(newNode)) && 
							!(open.contains(newNode))) {
						// Again... Like up above we need to calculate costs for the algorithm to use
						newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node);
						newNode.parent = nextNode;
						newNode.gCost = edge.weight + newNode.parent.gCost;
						newNode.fCost = newNode.hCost;  //Greedy Best First uses h(x) for f(x)
						open.add(newNode);
					}
				}
			}
			++depth; // Explored a layer, so increase the current depth
		}
		throw new SearchFailureException("Greedy Best First Search Failed! Maximum Depth Reached"); // Means search reached max depth
	}
	
	/**
	 * Function acts as a helper when search function completes search. It
	 * will take a given <code>GraphSearchNode</code> (i.e goal node) and retrace
	 * the path to the initial node
	 * 
	 * @param node the starting location (i.e goal)
	 * @return a path of nodes starting with initial node at top of stack
	 */
	private Stack<GraphSearchNode> generatePath(GraphSearchNode node) {
		Stack<GraphSearchNode> path = new Stack<GraphSearchNode>();
		GraphSearchNode currentNode = node;
		
		while(currentNode.parent != null) {
			path.push(currentNode);
			currentNode = currentNode.parent;
		}
		return path;
	}
}
