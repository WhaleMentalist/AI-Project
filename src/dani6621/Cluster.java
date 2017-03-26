package dani6621;

import java.util.*;

import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;



/**
 * Class to store a set of UUIDs for asteroids to be used in K-means clustering
 * Also stores the centroid of the cluster, as well as a cluster id
 * @author gregflood918
 *
 */
public class Cluster {
	
	Set<UUID> asteroids;
	Position centroid;
	int id;
	
	/**
	 * Constructor
	 * @param id
	 */
	public Cluster(int id){
		this.id = id;
		asteroids = new HashSet<UUID>();	
	}
	
	/**
	 * Overloaded constructor. Specifies cluster center
	 * @param id
	 * @param center
	 */
	public Cluster(int id, Position center){
		this.id = id;
		asteroids = new HashSet<UUID>();
		centroid = center;
	}
		
	/**
	 * Copy constructor for cluster class
	 * @param c
	 */
	public Cluster(Cluster c){
		Collection<UUID> oldSet = c.getAsteroids();
		Set<UUID> ast = new HashSet<UUID>();
		ast.addAll(oldSet);
		this.asteroids = ast;
		this.centroid = c.getCentroid().deepCopy();
		this.id = c.getId();
	}
	
	/**
	 * Add an asteroid UUID to the set of asteroids in the cluster
	 * @param a
	 */
	public void pushAsteroid(Asteroid a){
		asteroids.add(a.getId());
	}
	
	/**
	 * Clear the contents of the cluster
	 */
	public void clearCluster(){
		asteroids.clear();
	}
	
	/**
	 * Function that updates the location of the centroid based on the contents of the set
	 */
	public void updateCentroid(Toroidal2DPhysics tor){
		
		double x = 0.0;
		double y = 0.0;
		Iterator<UUID> asteroidSet = asteroids.iterator();
		
		while(asteroidSet.hasNext()){
			AbstractObject a = tor.getObjectById(asteroidSet.next());
			x += a.getPosition().getX();
			y += a.getPosition().getY();
		}
		centroid = new Position(x/asteroids.size() , y/asteroids.size());
			
	}
		
	public double calculateDispersion(Toroidal2DPhysics tor){
		
		Iterator<UUID> asteroidSet = asteroids.iterator();
		double dispersion = 0;
		while(asteroidSet.hasNext()){
			AbstractObject a = tor.getObjectById(asteroidSet.next());
			dispersion += tor.findShortestDistance(centroid, a.getPosition());
		}	
		return dispersion/asteroids.size();
	}

	/**
	 * getters and setters below
	 * @return
	 */
	public Set<UUID> getAsteroids() {
		return asteroids;
	}

	public void setAsteroids(Set<UUID> asteroids) {
		this.asteroids = asteroids;
	}

	public Position getCentroid() {
		return centroid;
	}

	public void setCentroid(Position centroid) {
		this.centroid = centroid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
