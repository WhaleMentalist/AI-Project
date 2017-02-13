package dani6621;

import java.util.LinkedList;
import java.util.List;

import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.objects.AbstractObject;

import dani6621.Graph.Vertex;

/**
 * Class will take simulation map and create an abstraction
 * using sample data points. Under the hood the class will use
 * a <code>Graph</code> to represent the 'N' data points. Connections 
 * occur when no obstacle is present, otherwise a connection is formed. 
 * The A* algorithm will reside in this class and its parameters will
 * include: ship position and goal object (i.e AbstractObject) as 
 * parameters. The branching factor of any given point will be eight
 * at the maximum and in RARE occurrences zero.
 *
 */
public class NavigationMap {
	
	/**
	 * The key that allows access to specified vertex
	 */
	private class NavigationVertexKey {
		
		/**
		 * Location of node by row
		 */
		public final int vertexRowNumber;
		
		/**
		 * Location of node by column
		 */
		public final int vertexColumnNumber;
		
		/**
		 * Initialize key with data
		 * 
		 * @param row	the location of vertex by row
		 * @param column the location of vertex by column
		 */
		public NavigationVertexKey(int row, int column) {
			vertexRowNumber = row;
			vertexColumnNumber = column;
		}
		
		/**
		 * Hash by column and row number
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + vertexColumnNumber;
			result = prime * result + vertexRowNumber;
			return result;
		}
		
		/**
		 * Compare by column and row number
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NavigationVertexKey other = (NavigationVertexKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (vertexColumnNumber != other.vertexColumnNumber)
				return false;
			if (vertexRowNumber != other.vertexRowNumber)
				return false;
			return true;
		}


		private NavigationMap getOuterType() {
			return NavigationMap.this;
		}
	}
	
	/**
	 * The vertex that will represent the 
	 * navigation map vertices
	 */
	private class NavigationVertex {
		
		/**
		 * The position of the node in the 
		 * map
		 */
		public final Position position;
		
		/**
		 * Initializes node with data to help navigation map
		 * 
		 * @param pos the position of node in map
		 */
		public NavigationVertex(Position pos) {
			position = pos;
		}
	}
	
	/**
	 * The spacing between each node in the graph
	 */
	private static final int SPACING = 100; // Remember: Use 40 when not debugging, 200 for debugging
	
	/**
	 * The offset of the connection algorithm
	 */
	private static final int OFFSET = 2;
	
	/**
	 * Data member contains the sample points, it
	 * will be constructed from <code>Toroidal2DSpace</code> 
	 * object. It will also be reconstructed at a set interval
	 * dictated by the agent's needs.
	 */
	private Graph<NavigationVertexKey, NavigationVertex> dataPoints;
	
	/**
	 * Reference to simulation space for some utility functions
	 */
	private Toroidal2DPhysics spaceRef;
	
	/**
	 * Reference to knowledge representation to help 
	 * graph construct accurate map
	 */
	private WorldState knowledgeRef;
	
	/**
	 * Number of nodes in each row
	 */
	public final int rowNodeNumber;
	
	/**
	 * Number of nodes in each column
	 */
	public final int columnNodeNumber;
	
	/**
	 * Constructor will initialize and create graph
	 * with proper edges and connections
	 * 
	 * @param space the space being abstracted by the 
	 * 			graph
	 * 
	 * @param knowledge the knowledge representation
	 */
	public NavigationMap(Toroidal2DPhysics space, WorldState knowledge) {
		
		spaceRef = space;
		knowledgeRef = knowledge;
		// Get dimensions of environment
		int height = spaceRef.getHeight();
		int width = spaceRef.getWidth();
		
		// Calculate number of row points and column points
		rowNodeNumber = width / SPACING;
		columnNodeNumber = height / SPACING;
		
		// Get number of vertices in the graph
		int numberOfVertices = rowNodeNumber * columnNodeNumber;
		
		// Initialize graph to hold correct number of vertices
		dataPoints = new Graph<NavigationVertexKey, NavigationVertex>(numberOfVertices);
		
		Position nodePosition;
		NavigationVertexKey key;
		NavigationVertex vertex;
		
		//row/columns are backwards
		// Enumerate through rows after columns
		for(int i = 0; i < rowNodeNumber; ++i) {
			// Go down along column first
			for(int j = 0; j < columnNodeNumber; ++j) { // Populate graph with vertices
				nodePosition = new Position((double) i * SPACING, (double) j * SPACING);
				vertex = new NavigationVertex(nodePosition);
				key = new NavigationVertexKey(i, j);
				dataPoints.addVertex(key, vertex);
			}
		}
		
		// Form connections for each node
		for(int i = 0; i < rowNodeNumber; ++i) {
			for(int j = 0; j < columnNodeNumber; ++j) {
				key = new NavigationVertexKey(i, j);
				vertex = dataPoints.getVertex(key).data;
				formConnections(key, vertex);
			}
		}
	}
	
	/**
	 * Function will retrieve the position of a vertex given
	 * a row and column
	 * 
	 * @param row the row to find vertex
	 * @param column the column to find vertex
	 * @return a <code>Position</code> object of the vertex location
	 */
	public Position getPositionOfVertex(int row, int column) {
		return dataPoints.getVertex(new NavigationVertexKey(row, column)).data.position;
	}
	
	/**
	 * Function will return the positions of vertices connected 
	 * to the vertex passed as row and column
	 * 
	 * @param row the row that is queried for to check for positions
	 * @param column the column that is queried for to check for positions
	 * @return a <code>List</code> of <code>Position</code> objects of the 
	 * 			vertices connected to the central vertex queried
	 */
	public List<Position> getNeighborPosition(int row, int column) {
		List<Graph<NavigationVertexKey, NavigationVertex>.Edge> edges = 
				dataPoints.getEdges(new NavigationVertexKey(row, column));
		List<Position> positions = new LinkedList<Position>();
		
		for(Graph<NavigationVertexKey, NavigationVertex>.Edge edge : edges) {
			positions.add(Graph.HEAD, edge.endVertex.data.position);
		}
		return positions;
	}
	
	/**
	 * Function will create an edge between two vertices at 
	 * specified row and column locations. The function will
	 * find the vertices based on the row and columns passed (i.e
	 * they are used as keys)
	 * 
	 * @param startRow the row position of the start vertex
	 * @param startColumn the column position of the start vertex
	 * @param endRow the row position of the end vertex
	 * @param endColumn the column position of the end vertex
	 */
	private void addConnection(int startRow, int startColumn, int endRow, int endColumn) {
		
		NavigationVertexKey startKey = new NavigationVertexKey(startRow, startColumn);
		NavigationVertexKey endKey = new NavigationVertexKey(endRow, endColumn);
		
		if(dataPoints.containVertex(startKey) && dataPoints.containVertex(endKey)) {
			NavigationVertex startVertex = dataPoints.getVertex(startKey).data;
			NavigationVertex endVertex = dataPoints.getVertex(endKey).data;
			
			if(startVertex != null && endVertex != null) {
				Position startPos = startVertex.position;
				Position endPos = endVertex.position;
				
				if(spaceRef.isPathClearOfObstructions(startPos, endPos, knowledgeRef.getObstacles(), Ship.SHIP_RADIUS)) {
					dataPoints.addEdge(startKey, endKey, (int) Math.ceil(spaceRef.findShortestDistance(startPos, endPos)));
				}
			}
		}
	}	
	
	/**
	 * 
	 * @param key
	 * @param vertex
	 */
	private void formConnections(NavigationVertexKey key, NavigationVertex vertex) {
		int vertexRow = key.vertexRowNumber;
		int vertexColumn = key.vertexColumnNumber;
		
		// Goto neighbor next to node by offset
		for(int i = vertexRow; i < vertexRow + OFFSET; ++i) {
			for(int j = vertexColumn; j < vertexColumn + OFFSET; ++j) {
				if(i == vertexRow && j == vertexColumn) { // Skip 'vertex' itself, don't want to form connection to self
					continue;
				}
				else {
					// Use modulus to perform wrap around, as this is a torus space
					addConnection(vertexRow, vertexColumn, (i % (rowNodeNumber)), (j % (columnNodeNumber))); // Create a connection
				}
			}
		}
		
		// Edge case: When the specified column is zero, we need to connect both diagonal up and diagonal down (Torus space)
		if(vertexColumn == 0) {
			addConnection(vertexRow, vertexColumn, ((vertexRow + 1) % rowNodeNumber), (columnNodeNumber - 1));
		}
		
		addConnection(vertexRow, vertexColumn, ((vertexRow + 1) % rowNodeNumber), ((vertexColumn - 1) % columnNodeNumber));
	}
	
	/**Finds the coordinate vertex that is closest to the passed object
	 * 
	 * @param myObj - Passes an abstract object
	 * @return Vertex object that is closest to the argument
	 */
	public Vertex findNearestVertex(AbstractObject myObj){
		
		Position objPos = myObj.getPosition();
		Double xCoord = objPos.getX();
		Double yCoord = objPos.getY();
		
		//Convert to grid space
		int gridxCoord = (int)Math.round(xCoord / SPACING);
		int gridyCoord = (int)Math.round(yCoord / SPACING);
		
		//Create navigation vertex key map
		NavigationVertexKey nearestKey = new NavigationVertexKey(gridxCoord, gridyCoord);
		Vertex nearestVert = dataPoints.getVertex(nearestKey);
		
		return nearestVert;
	}
	
	
 }
