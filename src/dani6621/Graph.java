package dani6621;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class Graph<K, V> {
	
	/**
	 * The value that will be mapped in the <code>graph</code>
	 * data member. It will contain the <code>Vertex</code> 
	 * parameterized data type and a list of edges that connect
	 * vertex
	 */
	public class Vertex {

		/**
		 * The data associated with the vertex
		 */
		public final V data;
		
		/**
		 * The set of edges that vertex has with
		 * other vertices
		 */
		public final List<Edge> edges;
		
		/**
		 * Initialize the the vertex with no edges, 
		 * but with vertex data
		 * 
		 * @param v the vertex data as a parameterized 
		 * 				type
		 */
		public Vertex(V v) {
			data = v;
			edges = new LinkedList<Edge>();
		}
		
		/**
		 * Auto-generated
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			return result;
		}
		
		/**
		 * Auto-generated
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Vertex other = (Vertex) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			return true;
		}

		private Graph<K, V> getOuterType() {
			return Graph.this;
		}
	}
	
	/**
	 * Simple edge class to track connections 
	 * between vertices and the corresponding
	 * weight (i.e cost)
	 *
	 */
	public class Edge {
		
		/**
		 * The vertex the edge is connected 
		 */
		public final Vertex endVertex;
		
		/**
		 * The weight of the given edge
		 */
		public final int weight;
		
		/**
		 * Constructor to initialize edge
		 * 
		 * @param end the vertex connected to edge
		 * @param w the weight of the edge
		 */
		public Edge(Vertex end, int w) {
			endVertex = end;
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
	private Map<K, Vertex> graph;
	
	/**
	 * Constructor initializes graph
	 * @param verticeCount the number vertices in the graph
	 */
	public Graph(int verticeCount) {
		graph = new HashMap<K, Vertex>(verticeCount); // Initialize to fit the number of required vertices
	}
	
	/**
	 * Function will add the vertex specified by the 
	 * key. Keep in mind both parameters are 
	 * generic 'type' taking on any form.
	 * 
	 * @param key the key used to map the <code>Value</code>
	 * @param data the data used to initialize a <code>Vertex</code>
	 * 				object
	 */
	public void addVertex(K key, V data) {
		Vertex v = new Vertex(data);
		graph.put(key, v);
	}
	
	/**
	 * Function will add an <code>Edge</code> between 
	 * two vertices specified by the keys passed
	 * 
	 * @param startKey the key to the start vertex
	 * @param endKey the key to the end vertex
	 * @param weight the weight of the edge
	 */
	public void addEdge(K startKey, K endKey, int weight) {
		Vertex startVertex = graph.get(startKey);
		Vertex endVertex = graph.get(endKey);
		
		// Add edge on both nodes, since graph is undirected
		graph.get(startKey).edges.add(new Edge(endVertex, weight));
		graph.get(endKey).edges.add(new Edge(startVertex, weight));
	}
	
	/**
	 * Function will retrieve edges from vertex specified 
	 * by the key
	 * 
	 * @param key the key to search for and retrieve the list
	 * 
	 * @return the <code>List</code> of <code>Edge</code> objects
	 */
	public List<Edge> getEdges(K key) {
		return graph.get(key).edges;
	}
	
	/**
	 * Retrieve a vertex specified by key
	 * 
	 * @param k the key used to get the vertex
	 * @return a <code>Vertex</code> object if found, otherwise <code>null</code>
	 */
	public Vertex getVertex(K k) {
		return graph.get(k);
	}
	
	/**
	 * Function will check if a key exists in 
	 * graph
	 * 
	 * @param k the key to look and check for
	 * @return a <code>boolean</code> of the result
	 */
	public boolean containVertex(K k) {
		return graph.containsKey(k);
	}
	
	/**
	 * Function will return <code>boolean</code> result of checking if
	 * two vertices are connected to an edge. Since the branching factor
	 * is at maximum eight, the time complexity of this function isn't too
	 * worrisome.
	 * 
	 * @param vertexOneKey the first vertex key
	 * @param vertexTwoKey the second vertex key
	 * @return a <code>boolean</code> result of whether two vertices
	 * 			are connected by an <code>Edge</code>
	 */
	public boolean isConnected(K vertexOneKey, K vertexTwoKey) {
		List<Edge> edges = graph.get(vertexOneKey).edges;
		Vertex vertexTwo = graph.get(vertexTwoKey);
		
		// Loop through each vertex and check if 'vertexTwo' is in the list
		for(Edge edge : edges) {
			if(edge.endVertex == vertexTwo) { // Check if reference is same
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
	public int getWeight(K vertexOneKey, K vertexTwoKey) {
		int weight = -1; // Initialize to negative as flag
		
		List<Edge> edges = graph.get(vertexOneKey).edges;
		Vertex vertexTwo = graph.get(vertexTwoKey);
		
		// Loop through each vertex and check if 'vertexTwo' is in the list
		for(Edge edge : edges) {
			if(edge.endVertex == vertexTwo) { // Check if reference is same
				weight =  edge.weight;
				break;
			}
		}
		return weight;
	}
}
