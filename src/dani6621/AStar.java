package dani6621;

import dani6621.NavigationMap.NavigationVertex;

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
	 * Inner class that will allow easy comparisons between 
	 * nodes. It will maintain a reference to the node it 
	 * is referring too and it will also contain the cost
	 * for the particular node.
	 *
	 */
	private class AStarNode {
		
		/**
		 * The parent of the node to help with 
		 * backtracking
		 */
		public NavigationVertex parent;
		
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
	}
}
