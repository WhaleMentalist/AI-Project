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
 * Essentially the knowledge representation of the agent. It will 
 * store some useful constants to delimit key states and it also has 
 * functions that retrieve other useful information such as closest asteroids 
 * and potential obstacles.
 */
public class SAWorldState {

    /**
     * Constant helps identify a low energy threshold
     */
    public static final double LOW_ENERGY = Ship.SHIP_MAX_ENERGY / 2.0;

    /**
     * Constant helps to delimit return to base
     */
    public static final int FULL_CARGO = 4000;

    /**
     * If a maximum velocity is imposed the agent has better
     * control
     */
    public static final double MAX_VELOCITY_MAGNITUDE = 60.0;

    /**
     * Helps to avoid agent from chasing asteroid at a slow speed
     */
    public static final double MIN_VELOCITY_MAGNITUDE = 35.0;

    /**
     * Delimits if bases is viable to get energy from
     */
    public static final double SUFFICIENT_BASE_ENERGY = 1000;
    
    /**
     * Any algorithms that perform look-ahead will use this
     * as max amount (i.e two seconds later in simulation)
     */
    public static final double MAX_LOOK_AHEAD = 80;
    
    /**
     * Speed multiplier that will be used in the calculation of intercept velocity
     */
    public double SPEED_MULTIPLIER = 1;
    
    /**
     * Constant that will be used it the calculation of intercept velocity
     * that prioritizes asteroids directly in front of the ship
     */
    public double ANGLE_WEIGHT = 1;

    /**
     * Simply a reference to use some of the functions (i.e shortest distance, obstructions)
     */
    private Toroidal2DPhysics _space;

    /**
     * The ship the function will use when performing calculations such as
     * finding the closest object (i.e the agent)
     */
    private Ship _referenceShip;

    /**
     * Initialize <code>WorldState</code> and build data for agent
     * to use as world state
     *
     * @param space a reference to simulation containing objects
     * @param ship  the ship belonging to agent
     */
    public SAWorldState(Toroidal2DPhysics space, Ship ship) {
        _space = space;
        _referenceShip = ship;
    }

    /**
     * Function will populate <code>mineableAsteroids</code>
     * with asteroids that are mineable on simulation
     */
    public Set<Asteroid> getMineableAsteroids() {
        Set<Asteroid> mineableAsteroids = new HashSet<Asteroid>();
        for (Asteroid asteroid : _space.getAsteroids()) {
            if (asteroid.isMineable()) {
                mineableAsteroids.add(asteroid);
            }
        }
        return mineableAsteroids;
    }

    /**
     * Function will populate <code>unmineableAsteroids</code>
     * with asteroids that are unmineable on simulation
     */
    public Set<Asteroid> getUnmineableAsteroids() {
        Set<Asteroid> unmineableAsteroids = new HashSet<Asteroid>();
        for (Asteroid asteroid : _space.getAsteroids()) {
            if (!(asteroid.isMineable())) {
                unmineableAsteroids.add(asteroid);
            }
        }
        return unmineableAsteroids;
    }

    /**
     * Function returns list of all the team's bases of reference
     * ship
     *
     * @return a <code>Set</code> of bases on reference ship's team
     */
    public Set<Base> getTeamBases() {
        Set<Base> bases = new HashSet<Base>();
        for (Base base : _space.getBases()) {
            if (base.getTeam().getTeamName().equals(_referenceShip.getTeamName())) {
                bases.add(base);
            }
        }
        return bases;
    }

    /**
     * Function retrieves all bases
     *
     * @return a <code>Set</code> of all the bases in simulation
     */
    public Set<Base> getBases() {
        Set<Base> bases = new HashSet<Base>();
        for (Base base : _space.getBases()) {
            bases.add(base);
        }
        return bases;
    }

    /**
     * Function retrieves all bases except team bases
     *
     * @return  a <code>Set</code> of bases in simulation not on
     *          reference ship team
     */
    public Set<Base> getBasesExceptTeam() {
        Set<Base> bases = new HashSet<Base>();
        for (Base base : _space.getBases()) {
            if (!(base.getTeam().getTeamName().equals(_referenceShip.getTeamName()))) {
                bases.add(base);
            }
        }
        return bases;
    }

    /**
     * Function retrieves all other ships in simulation with the exception of the
     * reference ship (i.e the agent itself)
     *
     * @return a <code>Set</code> of ships in the simulation
     */
    public Set<Ship> getShips() {
        Set<Ship> ships = new HashSet<Ship>();
        for (Ship ship : _space.getShips()) {
            if (!(ship.getId().equals(_referenceShip.getId()))) { // Add all other ships except reference ship
                ships.add(ship);
            }
        }
        return ships;
    }

    /**
     * Function will retrieve all current obstacles in simulation
     * (i.e asteroids, bases, other ships, and etc)
     *
     * @return the <code>Set</code> containing all obstacles in simulation as
     * <code>AbstractObject</code> type
     */
    public Set<AbstractObject> getObstacles() {
        Set<AbstractObject> obstacles = new HashSet<AbstractObject>();
        obstacles.addAll(getUnmineableAsteroids());
        obstacles.addAll(getBases());
        obstacles.addAll(getShips());
        return obstacles;
    }
    
    /**
     * This will generate a list of obstacles with the
     * exception of team bases
     *
     * @return  a <code>Set</code> containing obstacles
     */
    public Set<AbstractObject> getObstaclesExceptTeamBase() {
        Set<AbstractObject> obstacles = new HashSet<AbstractObject>();
        obstacles.addAll(getUnmineableAsteroids());
        obstacles.addAll(getBasesExceptTeam());
        obstacles.addAll(getShips());
        return obstacles;
    }

    /**
     * Function will return the most efficient asteroid in the simulation (i.e the amount
     * of resource per unit of distance).  This function excludes the additional prioritization of 
     * of objects in front of the ship (angle between calculations).  This is because it is assumed
     * that selecting objects by clusters will ultimately result in more turning/less movement.  
     * 
     * @param untouchables objects that will NOT be considered. Also a target cluster
     * that ensures the ship moves to high mineable asteroid density regions of space
     * @return an <code>Asteroid</code> object that is most efficient to ship
     */
    public Asteroid getMostEfficientMinableAsteroid(Map<UUID, AbstractObject> untouchables) {
        double costEffectiveness = Double.NEGATIVE_INFINITY; // Most effective asteroid (higher is better)
        double dist; // Store distance to object
        double currentCostEffectiveness; // Store the cost effectiveness of current asteroid
        Asteroid candidate = null; // The variable will hold asteroid that was found
        Position shipPos = _referenceShip.getPosition(); // Retrieve current position of ship
        Vector2D pathOfShip = shipPos.getTranslationalVelocity(); // Get velocity of ship
        Vector2D toAsteroid;
        double angleBetween = Math.PI; // Set to maximum angle

        for (Asteroid asteroid : getMineableAsteroids()) {
        	UUID current = asteroid.getId();
        	//Checks if the current asteroid is in our target cluster set.
        	if(untouchables.containsKey(current) ) { // Skip object
        		continue;
        	}
        	
            dist = _space.findShortestDistance(shipPos, asteroid.getPosition());
            toAsteroid = _space.findShortestDistanceVector(shipPos, asteroid.getPosition()); // Get vector pointing from ship to asteroid
            angleBetween = pathOfShip.angleBetween(toAsteroid); // Get angle between asteroid and ship velocity
            currentCostEffectiveness = asteroid.getResources().getTotal() / (dist * (1.0 + Math.pow(angleBetween, ANGLE_WEIGHT))); // Cost effectiveness calculation
            if (currentCostEffectiveness > costEffectiveness) { // Check if asteroid closer to ship and clear of obstructions
                costEffectiveness = currentCostEffectiveness; // Reassign shortest distance
                candidate = asteroid;
            }
        }
        return candidate;
    }

    /**
     * Function will retrieve the closest team base to reference ship
     *
     * @param untouchables objects that will NOT be considered
     * @return  <code>Base</code> object closest to ship
     */
    public Base getClosestFriendlyBase(Map<UUID, AbstractObject> untouchables) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        Base candidate = null;
        Position shipPos = _referenceShip.getPosition();
        for (Base base : getTeamBases()) { // Go through team bases
        	
        	if(untouchables.containsKey(base.getId())) { // Skip object
        		continue;
        	}
        	
            dist = _space.findShortestDistance(shipPos, base.getPosition());
            if (dist < shortestDist) { // Check if the best candidate is beaten
                shortestDist = dist;
                candidate = base;
            }
        }
        return candidate;
    }
    
    /**
     * Function will retrieve the closest team base to reference ship
     *
     * @return  <code>Base</code> object closest to ship
     */
    public Base getClosestFriendlyBase() {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        Base candidate = null;
        Position shipPos = _referenceShip.getPosition();
        for (Base base : getTeamBases()) { // Go through team bases
            dist = _space.findShortestDistance(shipPos, base.getPosition());
            if (dist < shortestDist) { // Check if the best candidate is beaten
                shortestDist = dist;
                candidate = base;
            }
        }
        return candidate;
    }

    /**
     * Function will return all potenial energy sources for
     * reference ship
     *
     * @return a <code>Set</code> containing all energy source
     *          objects
     */
    private Set<AbstractObject> getEnergySources() {
        Set<AbstractObject> energySources = new HashSet<AbstractObject>();

        energySources.addAll(getTeamBases()); // All team bases are decent energy source
        energySources.addAll(_space.getBeacons()); // Beacons are also energy sources

        return energySources;
    }

    /**
     * Function retrieves current energy ship contains
     *
     * @return a real value corresponding to current energy
     */
    public double getCurrentEnergy() {
        return _referenceShip.getEnergy();
    }

    /**
     * Function will return closest energy source to ship using a straight path
     * (i.e distance formula). It will consider friendly bases with sufficient
     * energy
     *
     * @param untouchables objects that will NOT be considered
     * @return <code>AbstractObject</code> object that is closest to ship
     */
    public AbstractObject getClosestEnergySource(Map<UUID, AbstractObject> untouchables) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        AbstractObject candidate = null; // The variable will hold beacon that was found
        Position shipPos = _referenceShip.getPosition();
        for (AbstractObject energySource : getEnergySources()) {
        	
        	if(untouchables.containsKey(energySource.getId())) { // Skip object
        		continue;
        	}
        	
            dist = _space.findShortestDistance(shipPos, energySource.getPosition());

            if(energySource instanceof Base) { // Check if it is base
                if(((Base) energySource).getEnergy() < SUFFICIENT_BASE_ENERGY) { // Check if bases has sufficient energy
                    continue; // Skip if base is too low on energy
                }
            }

            // Otherwise see if it is closer
            if (dist < shortestDist) {
                shortestDist = dist; // Reassign shortest distance
                candidate = energySource;
            }
        }
        return candidate;
    }
    
    /**
     * Function will return closest energy source to ship using a straight path
     * (i.e distance formula). It will consider friendly bases with sufficient
     * energy
     *
     * @return <code>AbstractObject</code> object that is closest to ship
     */
    public AbstractObject getClosestEnergySource() {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        AbstractObject candidate = null; // The variable will hold beacon that was found
        Position shipPos = _referenceShip.getPosition();
        for (AbstractObject energySource : getEnergySources()) {
        	
            dist = _space.findShortestDistance(shipPos, energySource.getPosition());

            if(energySource instanceof Base) { // Check if it is base
                if(((Base) energySource).getEnergy() < SUFFICIENT_BASE_ENERGY) { // Check if bases has sufficient energy
                    continue; // Skip if base is too low on energy
                }
            }

            // Otherwise see if it is closer
            if (dist < shortestDist) {
                shortestDist = dist; // Reassign shortest distance
                candidate = energySource;
            }
        }
        return candidate;
    }


    /**
     * Function helps ship to intercept object in space for faster retrieval.
     * It uses the constant acceleration equation to find new velocity and it
     * imposes restrictions on top speed (keep better control).
     *
     * @param object target object for ship to intercept
     * @return a new velocity for ship to accelerate
     */
    public Vector2D calculateInterceptVelocity(AbstractObject object) {
        Position shipPos = _referenceShip.getPosition();
        Position objectPos = object.getPosition();
        Vector2D shipVel = shipPos.getTranslationalVelocity();
        Vector2D objectVel = shipPos.getTranslationalVelocity();

        double distance = _space.findShortestDistance(shipPos, objectPos);
        double estimatedArrivalTime = distance / shipVel.getMagnitude();
        
        // Predict future position of object we are intercepting
        Position estimatedFuturePosition = new Position(objectPos.getX() + objectVel.getXValue() *
                estimatedArrivalTime, objectPos.getY() + objectVel.getYValue() * estimatedArrivalTime);
        
        // Get a vector that does to projected location from ship position
        Vector2D displacementVector = _space.findShortestDistanceVector(shipPos, estimatedFuturePosition);

        // Using constant acceleration equation to catch object
        Vector2D finalVelocity = ((displacementVector.subtract(shipVel.multiply(estimatedArrivalTime))).
                multiply(2.0)).divide(estimatedArrivalTime * estimatedArrivalTime);

        // Don't let the ship GO TO FAST! It will lose control because its yaw is slow
        if(finalVelocity.getMagnitude() > MAX_VELOCITY_MAGNITUDE) {
            finalVelocity = finalVelocity.getUnitVector().multiply(MAX_VELOCITY_MAGNITUDE);
        }
        else if(finalVelocity.getMagnitude() < MIN_VELOCITY_MAGNITUDE) {
            finalVelocity = finalVelocity.getUnitVector().multiply(MIN_VELOCITY_MAGNITUDE);
        }

        return finalVelocity;
    }
    
    /**
     * Function helps ship to go to location in space much more quickly
     *
     * @param location the new location the ship needs to go to
     * @return a new velocity for ship to accelerate
     */
    public Vector2D calculateVelocity(Position location) {
        Position shipPos = _referenceShip.getPosition();
        Position objectPos = location;
        Vector2D shipVel = shipPos.getTranslationalVelocity();
        Vector2D objectVel = shipPos.getTranslationalVelocity();

        double distance = _space.findShortestDistance(shipPos, objectPos);
        double estimatedArrivalTime = distance / shipVel.getMagnitude();

        Position estimatedFuturePosition = new Position(objectPos.getX() + objectVel.getXValue() *
                estimatedArrivalTime, objectPos.getY() + objectVel.getYValue() * estimatedArrivalTime);

        Vector2D displacementVector = _space.findShortestDistanceVector(shipPos, estimatedFuturePosition);

        // Using constant acceleration equation to catch object
        Vector2D finalVelocity = ((displacementVector.subtract(shipVel.multiply(estimatedArrivalTime))).
                multiply(2.0)).divide(estimatedArrivalTime * estimatedArrivalTime);

        // Don't let the ship GO TO FAST! It will lose control because its yaw is slow
        if(finalVelocity.getMagnitude() > MAX_VELOCITY_MAGNITUDE) {
            finalVelocity = finalVelocity.getUnitVector().multiply(MAX_VELOCITY_MAGNITUDE);
        }
        else if(finalVelocity.getMagnitude() < MIN_VELOCITY_MAGNITUDE) {
            finalVelocity = finalVelocity.getUnitVector().multiply(MIN_VELOCITY_MAGNITUDE);
        }

        return finalVelocity.multiply(SPEED_MULTIPLIER);
    }
    

    /**
     * Function that performs k-means clustering on mineable asteroids. It is assumed
     * that moving to regions with a higher density of mineable asteroids (in terms of
     * average dispersion) will result in higher scores.
     * 
     * There is a hard cap of 30 iterations on the number
     * of times the centroid location can change before the function simply returns a cluster.  Testing
     * has shown that it rarely ever takes more than 7 steps to converge.
     * 
     * This function will initialize random centroids for the specified number of k cluster.  Only
     * one random initialization will occur from a call to this function.  K-means clustering with
     * restarts can be implemented by nesting function within a for loop and tracking the best cluster.
     * This can be observed within the AStarAgent java file.
     * 
     * @param k number of clusters to be used in the calculation
     * @return a cluster with the lowest dispersion (as measured by average distance of objects from the
     * centroid).
     */
    public Cluster kmeansClustering(int kClusters){	 	
    	Random random = new Random();
    	Position currentPosition = _referenceShip.getPosition();
    	boolean outerFlag = false;
    	Cluster[] clusters = new Cluster[kClusters];
    	
    	//Initialize Cluster centers
    	for(int i=0;i<kClusters;i++){
    		Position initialLocation = _space.getRandomFreeLocationInRegion(random, 0, (int) currentPosition.getX(), 
				(int) currentPosition.getY(), 500);
    		clusters[i] = new Cluster((i),initialLocation);
    	}
    	
    	//worst case iterations
    	int j = 0;
    	while(j < 30){
    		
	    	//Assign asteroids to clusters.
	    	for (Asteroid asteroid : getMineableAsteroids()){
	    		double bestDist = Double.MAX_VALUE;
	    		Position asteroidPosition = asteroid.getPosition();
	    		int bestCluster = -1;
	    		
	    		//Find nearest cluster
	    		for(int i=0;i<clusters.length;i++){
	    			double distanceBetween = _space.findShortestDistance(asteroidPosition, clusters[i].getCentroid());
	    			if(distanceBetween < bestDist){
	    				bestDist = distanceBetween;
	    				bestCluster = i;
	    			}
	    		}  		
	    		//Assign to best cluster
	    		clusters[bestCluster].pushAsteroid(asteroid);	
	    	}
	    	
	    	//Update Cluster Centroids and check if we have converged.
	    	//if the centroid of the cluster before and after updating
	    	//is the same, then the test has converged.
	    	boolean test = true;
	    	for(Cluster c : clusters){
	    		Position last = c.getCentroid();
	    		c.updateCentroid(_space);
	    		if(!last.equalsLocationOnly(c.getCentroid())){
	    			test = false;
	    		}
	    	}
	    		
	    	//Exit loop if test has converged.  Find the best cluster as defined
	    	//by minimum dispersion and return cluster with lowest dispersion
	    	if(test){
	    		int bestCluster=-1;
	    		double dispersion = Double.MAX_VALUE;
	    		for(int k=0;k<clusters.length;k++){
	    			double tempDisp = clusters[k].calculateDispersion(_space);
	    			if(tempDisp<dispersion){
	    				bestCluster=k;
	    				dispersion = tempDisp;
	    			}
	    		}  
	    		return clusters[bestCluster];
	    	}
	    	j++;
	    	
    	}
    	//If for some reason we don't converge in the set number of iterations.
    	//then calculate the best cluster here.
    	if(outerFlag){
			int bestCluster=-1;
			double dispersion = Double.MAX_VALUE;
			for(int k=0;k<clusters.length;k++){
				if(dispersion>clusters[k].calculateDispersion(_space)){
					bestCluster=k;
					dispersion = clusters[k].calculateDispersion(_space);
				}
			}  
			return clusters[bestCluster];
	    }
    	else{
    		return null;
    	}  
    }
}
