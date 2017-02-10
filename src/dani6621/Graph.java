package dani6621;

import java.util.*;

/**
 * Implementation of graph uses adjacency list
 * to track a vertex's connections in the graph.
 * The inner class <code>Edge</code> also contains 
 * a corresponding weight, which is useful for 
 * path cost calculations that the A* algorithm 
 * utilizes. For now, the graph is implemented 
 * as an undirected, but it DOES have weights. The 
 * weights will NOT be negative
 */
public class Graph<Z> {
	
	/**
	 * A parameterized <code>Node</code> implementation that
	 * will allow flexibility of how the graph can represent 
	 * data
	 *
	 * @param <T> the data type of the graph
	 */
	private class Vertex {
		
		/**
		 * Data contained in the vertex (i.e name, position,
		 * or even number)
		 */
		public Z data;
	}
	
	/**
	 * Simple edge class to track connections 
	 * between vertices and the corresponding
	 * weight (i.e cost)
	 *
	 */
	private class Edge {
		
		/**
		 * The vertex the edge is connected 
		 */
		public Vertex vertex;
		
		/**
		 * The weight of the given edge
		 */
		public int weight;
		
		/**
		 * Constructor to initialize edge
		 * 
		 * @param v the vertex connected to edge
		 * @param w the weight of the edge
		 */
		public Edge(Vertex v, int w) {
			vertex = v;
			weight = w;
		}
	}
	
	/**
	 * This will allow <code>addEdge</code> function to add
	 * at the head of the linked list. This will decrease 
	 * time complexity
	 */
	public static final int HEAD = 0;
	
	/**
	 * A list containing entries of linked lists that contain
	 * <code>Edge</code> objects
	 * 
	 * Example:
	 * [0] => [1, 2, 5]
	 * [1] => [0, 2]
	 * [2] => [0, 1]
	 * [3] => []
	 * [4] => []
	 * [5] => [0]
	 */
	private Map<Vertex, LinkedList<Edge>> graph;
	
	/**
	 * Constructor initializes graph
	 * @param verticeCount the number vertices in the graph
	 */
	public Graph(int verticeCount) {
		graph = new HashMap<Vertex, 
				LinkedList<Edge>>(verticeCount); // Initialize to fit the number of required vertices
	}
	
	/**
	 * Function will add an edge between the start vertex and 
	 * go to the end vertex with the corresponding weight. Since
	 * the graph is undirected, the function will add an edge to 
	 * both of the vertice's adjacency list
	 * 
	 * @param startVertex the start location of the edge
	 * @param endVertex the end location of the edge
	 * @param weight the weight of the edge
	 */
	public void addEdge(Vertex startVertex, Vertex endVertex, int weight) {
		graph.get(startVertex).add(Graph.HEAD, new Edge(endVertex, weight)); // Add at head of linked list
		graph.get(endVertex).add(Graph.HEAD, new Edge(startVertex, weight)); // Add other way as well
	}
	
	/**
	 * Function will return <code>boolean</code> result of checking if
	 * two vertices are connected to an edge
	 * 
	 * @param vertex1 the first vertex
	 * @param vertex2 the second vertex
	 * @return a <code>boolean</code> result of whether two vertices
	 * 			are connected by an <code>Edge</code>
	 */
	public boolean isConnected(Vertex vertex1, Vertex vertex2) {
		LinkedList<Edge> neighbors = graph.get(vertex1); // Get the neighbors of vertex
		
		// Loop through each vertex and check if 'vertex2' is in the list
		for(Edge edge : neighbors) {
			if(edge.vertex == vertex2) { // Return true if 'vertex2' is found. Comparison by reference!
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Function will retrieve weight, if applicable, from the two
	 * vertices
	 * 
	 * @param vertex1 the first vertex
	 * @param vertex2 the second vertex
	 * @return the weight between the two vertices, if the returned value is negative
	 * 			then there is no connection!
	 */
	public int getWeight(Vertex vertex1, Vertex vertex2) {
		int weight = -1;
		LinkedList<Edge> neighbors = graph.get(vertex1);
		
		for(Edge edge : neighbors) {
			if(edge.vertex == vertex2) { // Found the connection
				weight = edge.weight;
				break;
			}
		}
		return weight;
	}
}
