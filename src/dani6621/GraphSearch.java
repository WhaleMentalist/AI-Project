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
	 * Experimental: Trying to find a limit that is NOT too long, 
	 * but doesn't tie down the capabilities of the AI
	 */
	private static final int MAX_SEARCH_LIMIT = 300;
	
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
	 * a bit large, but for the most part readable! It uses f(n) = g(n) + h(n) as cost.
	 * 
	 * @param obstacles the obstacles the search should avoid
	 * @return an <code>AStarNode</code> who can be recursively iterated to 
	 * 			generate a path
	 * @throws SearchFailureException any instance where the search fails due to
	 * 										a multitude of reasons
	 */
	public Stack<GraphSearchNode> aStarSearch(Set<AbstractObject> obstacles) throws SearchFailureException {
		Set<GraphSearchNode> closed = new HashSet<GraphSearchNode>(); // Set of explored nodes
		Map<GraphSearchNode, Integer> costMap = new HashMap<GraphSearchNode, Integer>(); // Access cost in O(1)
		Queue<GraphSearchNode> open = new PriorityQueue<GraphSearchNode>(new Comparator<GraphSearchNode>() {
			@Override
			public int compare(GraphSearchNode nodeOne, GraphSearchNode nodeTwo) {
				return Integer.compare(nodeOne.fCost, nodeTwo.fCost);
			}
		});
		
		map.setObstacles(obstacles);
		
		GraphSearchNode currentNode; // Reference to node that was just popped from queue
		GraphSearchNode newNode; // Reference to potenial neighbor nodes
		List<Graph<NavigationVertexKey, NavigationVertex>.Edge> neighbors; // List containing neighbors of node
		int tentativeGScore = 0; // Hold the potenial cost of node, in case better path is found
		int searchLimit = 0;
		
		open.add(initialNode); // Insert start node into open queue
		costMap.put(initialNode, initialNode.gCost); // Put cost so far
		while(!(open.isEmpty()) && searchLimit < MAX_SEARCH_LIMIT) { // Continue until open queue is empty
			currentNode = open.poll(); // Remove lowest 'fCost' node from queue
			
			if(currentNode.equals(goalNode)) 
				return generatePath(currentNode); // Recursivly generate path and return it
			
			closed.add(currentNode); // Add node to the explored set
			map.formConnections(map.getNavigationVertexKey(currentNode.node)); // Generate connections as needed
			neighbors = map.getNeighbors(currentNode.node); // Get neighbors of current node
			
			// Iterate through each neighbor of the current node
			for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
				newNode = new GraphSearchNode(edge.endVertex.data); // Create new graph search node
				if(closed.contains(newNode)) // Skip nodes that have been explored
					continue;
				tentativeGScore = currentNode.gCost + 
								map.calculateCost(currentNode.node, newNode.node); // Calculate path cost
				if(!(open.contains(newNode))) {
					newNode.parent = currentNode;
					newNode.gCost = tentativeGScore;
					newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node);
					newNode.fCost = newNode.hCost + newNode.gCost;
					costMap.put(newNode, newNode.gCost);
					open.add(newNode);
				}
				else if(tentativeGScore >= costMap.get(newNode)) // Path cost is NOT better
					continue;
			}
			++searchLimit;
		}
		throw new SearchFailureException("Error, A* search failed! The open set became empty!");
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
		Set<GraphSearchNode> closed = new HashSet<GraphSearchNode>(); // Set of explored nodes
		Queue<GraphSearchNode> open = new PriorityQueue<GraphSearchNode>(new Comparator<GraphSearchNode>() {
			@Override
			public int compare(GraphSearchNode nodeOne, GraphSearchNode nodeTwo) {
				return Integer.compare(nodeOne.fCost, nodeTwo.fCost);
			}
		});
		
		GraphSearchNode currentNode; // Reference to node that was just popped from queue
		GraphSearchNode newNode; // Reference to potenial neighbor nodes
		List<Graph<NavigationVertexKey, NavigationVertex>.Edge> neighbors; // List containing neighbors of node
		int searchLimit = 0;
		
		open.add(initialNode); // Insert start node into open queue
		while(!(open.isEmpty()) && searchLimit < MAX_SEARCH_LIMIT) { // Continue until open queue is empty
			currentNode = open.poll(); // Remove lowest 'fCost' node from queue
			
			if(currentNode.equals(goalNode)) 
				return generatePath(currentNode); // Recursivly generate path and return it
			
			closed.add(currentNode); // Add node to the explored set
			map.formConnections(map.getNavigationVertexKey(currentNode.node)); // Generate connections as needed
			neighbors = map.getNeighbors(currentNode.node); // Get neighbors of current node
			
			// Iterate through each neighbor of the current node
			for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : neighbors) {
				newNode = new GraphSearchNode(edge.endVertex.data); // Create new graph search node
				if(closed.contains(newNode)) // Skip nodes that have been explored
					continue;
				if(!(open.contains(newNode))) {
					newNode.parent = currentNode;
					newNode.gCost = 0;
					newNode.hCost = map.calculateHeuristic(newNode.node, goalNode.node);
					newNode.fCost = newNode.hCost;
					open.add(newNode);
				}
			}
			++searchLimit;
		}
		throw new SearchFailureException("Error, Greedy Best First search failed! The open set became empty!");
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
		
		// Then iterate as usual
		while(currentNode.parent != null) {
			path.push(currentNode);
			currentNode = currentNode.parent;
		}
		return path;
	}
}
