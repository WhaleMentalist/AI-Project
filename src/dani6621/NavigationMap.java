package dani6621;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import spacesettlers.graphics.LineGraphics;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.graphics.StarGraphics;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

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
	public class NavigationVertexKey {
		
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
	public static class NavigationVertex {
		
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
	public static final int SPACING = 40;
	
	/**
	 * The amount of time the projection of movement will be viewed
	 */
	public static final int LOOK_AHEAD = (int) Math.ceil(SPACING / 15.0);
	
	/**
	 * Determines if vertex is close to obstacle
	 */
	public static final int CLOSE_DISTANCE = (int) (Ship.SHIP_RADIUS * 2.5);
	
	/**
	 * Set value for graphcis debugging
	 */
	private final boolean DEBUG_MODE;
	
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
	 * Reference to the set of obstacles that will try to be
	 * avoided
	 */
	private Set<AbstractObject> obstacles;
	
	/**
	 * Number of nodes in each row
	 */
	public final int rowNodeNumber;
	
	/**
	 * Number of nodes in each column
	 */
	public final int columnNodeNumber;
	
	public List<SpacewarGraphics> graphDrawing;
	
	/**
	 * Constructor will initialize and create graph
	 * with proper edges and connections
	 * 
	 * @param space the space being abstracted by the 
	 * 			graph
	 * 
	 *@param debug	the flag for graphics display
	 */
	public NavigationMap(Toroidal2DPhysics space, boolean debug) {
		DEBUG_MODE = debug;
		graphDrawing = new ArrayList<SpacewarGraphics>();
		
		spaceRef = space;
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
		
		// Enumerate through rows after columns
		for(int i = 0; i < rowNodeNumber; ++i) {
			// Go down along column first
			for(int j = 0; j < columnNodeNumber; ++j) { // Populate graph with vertices
				nodePosition = new Position((double) i * SPACING, (double) j * SPACING);
				vertex = new NavigationVertex(nodePosition);
				key = new NavigationVertexKey(i, j);
				dataPoints.addVertex(key, vertex);
				if(DEBUG_MODE)
					graphDrawing.add(new StarGraphics(Color.RED, nodePosition));
			}
		}
		
		obstacles = new HashSet<AbstractObject>(); // Instantiate
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
	 * Method will retrieve the neighbors of the vertex
	 * 
	 * @param v	the vertex that is being querried
	 * @return	a <code>List</code> of <code>Edge</code> objects connected to the vertex in question
	 */
	public List<Graph<NavigationVertexKey, NavigationVertex>.Edge> getNeighbors(NavigationVertex v) {
		NavigationVertexKey key = getNavigationVertexKey(v);
		return dataPoints.getEdges(key);
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
				
				// If there are no obstacles along edge or no obstacles are too close to end node then it can be added
				if(spaceRef.isPathClearOfObstructions(startPos, endPos, obstacles, CLOSE_DISTANCE) && !(isCloseToObstacle(endVertex))) {
					dataPoints.addEdge(startKey, endKey, (int) Math.ceil(spaceRef.findShortestDistance(startPos, endPos)));
					if(DEBUG_MODE)
						graphDrawing.add(new LineGraphics(startPos, endPos, spaceRef.findShortestDistanceVector(startPos, endPos)));
				}
			}
		}
	}	
	
	/**
	 * This function will create connections to the vertex
	 * 
	 * @param key the key of the vertex to form connections
	 * @param vertex the actual vertex object to add connections to
	 */
	public void formConnections(NavigationVertexKey key) {
		int vertexRow = key.vertexRowNumber;
		int vertexColumn = key.vertexColumnNumber;
		
		// Goto neighbor next to node by offset
		for(int i = vertexRow - 1; i < vertexRow + 2; ++i) {
			for(int j = vertexColumn - 1; j < vertexColumn + 2; ++j) {
				if(i == vertexRow && j == vertexColumn) { // Skip 'vertex' itself, don't want to form connection to self
					continue;
				}
				else {
					// Use modulus to perform wrap around, as this is a torus space
					addConnection(vertexRow, vertexColumn, ((i + rowNodeNumber) % rowNodeNumber), 
							((j + columnNodeNumber) % (columnNodeNumber))); // Create a connection
				}
			}
		}
		
		addConnection(vertexRow, vertexColumn, ((vertexRow + 1) % rowNodeNumber), 
				((vertexColumn - 1) % columnNodeNumber));
				
	}
	
	/** 
	 * Finds the coordinate vertex that is closest to the passed object
	 * 
	 * @param myObj - Passes an abstract object
	 * @return Vertex object that is closest to the argument
	 */
	public NavigationVertex findNearestVertex(AbstractObject myObj) {
		Position objPos = myObj.getPosition();
		Double xCoord = objPos.getX();
		Double yCoord = objPos.getY();
		
		//Convert to grid space
		int rowNumber = ((int)Math.round(xCoord / SPACING) % rowNodeNumber);
		int columnNumber = ((int)Math.round(yCoord / SPACING) % columnNodeNumber);
		
		NavigationVertexKey key = new NavigationVertexKey(rowNumber, columnNumber);
		NavigationVertex candidate = dataPoints.getVertex(key).data;
		
		return candidate;
	}
	
	/**
	 * Function will retrieve the key for the given <code>NavigationVertex</code>
	 * using the <code>SPACING</code> constant
	 * 
	 * @param vertex the vertex whose key will be retrieved
	 * @return a <code>NavigationVertexKey</code> of the <code>NavigationVertex</code>
	 * 			passed
	 */
	public NavigationVertexKey getNavigationVertexKey(NavigationVertex vertex) {
		Position vertexPos = vertex.position;
		Double xCoord = vertexPos.getX();
		Double yCoord = vertexPos.getY();
		
		//Convert to grid space
		int rowNumber = ((int)Math.round(xCoord / SPACING) % rowNodeNumber);
		int columnNumber = ((int)Math.round(yCoord / SPACING) % columnNodeNumber);
		
		return new NavigationVertexKey(rowNumber, columnNumber);
	}
	
	/**
	 * Function returns a <code>boolean</code> if object is too
	 * close to particular vertex
	 * 
	 * @param v the vertex to test
	 * @return the result
	 */
	public boolean isCloseToObstacle(NavigationVertex v) {
		int candidate;
		Position projPosition;
		
		for(AbstractObject obj : obstacles) {
			
			// Account for movement by projecting few timesteps ahead (i.e 52 timesteps to be exact)
			if(obj.isMoveable()) {
				projPosition = new Position(obj.getPosition().getX() + obj.getPosition().getxVelocity(), 
											obj.getPosition().getY() + obj.getPosition().getyVelocity());
				candidate =  (int) spaceRef.findShortestDistance(v.position, projPosition) - obj.getRadius();
			}
			else {
				// Calculate straight-line distance and accoutn for object radius
				candidate = (int) spaceRef.findShortestDistance(v.position, obj.getPosition()) - obj.getRadius();
			}
			
			if(candidate < CLOSE_DISTANCE) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Function will calculate a heuristic value given a random node and the 
	 * goal node. The time complexity is constant, thereby fulfilling requirement
	 * of the project. The heuristic is the straight line distance (relatively simple,
	 * yet powerful heuristic). Used in 'A*' algorithm.
	 * 
	 * @param node	the node that will have its heuristic value calculated
	 * @param goalNode	the location of the goal that will be used to calculate the 
	 * 					heuristic
	 * @return the value of the heuristic of the <code>node</code> parameter as an integer
	 * 			quantity
	 */
	public int calculateHeuristic(NavigationVertex node, NavigationVertex goalNode) {
		return (int) Math.ceil(spaceRef.findShortestDistance(node.position, goalNode.position));
	}
	
	/**
	 * Function will retrieve the weight between the two nodes (if one exists). If 
	 * an <code>Edge</code> does not exist between the two nodes, then a value of -1 
	 * is returned. Used in 'A*' algorithm.
	 * 
	 * @param start the start location 
	 * @param end the end location
	 * @return a weight value corresponding to distance cost between nodes
	 */
	public int calculateCost(NavigationVertex start, NavigationVertex end) {
		NavigationVertexKey startKey = getNavigationVertexKey(start);
		NavigationVertexKey endKey = getNavigationVertexKey(end);
		
		return dataPoints.getWeight(startKey, endKey);
	}
	
	/**
	 * Method will set the obstacles to track on the navigation map
	 * 
	 * @param obst	the obstacles that will be tracked
	 */
	public void setObstacles(Set<AbstractObject> obst) {
		obstacles = obst;
	}
 }
