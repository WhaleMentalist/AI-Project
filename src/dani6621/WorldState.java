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
public class WorldState {

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
    public static final double MIN_VELOCITY_MAGNITUDE = 40.0;

    /**
     * Delimits if bases is viable to get energy from
     */
    public static final double SUFFICIENT_BASE_ENERGY = 600;
    
    /**
     * Any algorithms that perform look-ahead will use this
     * as max amount (i.e two seconds later in simulation)
     */
    public static final double MAX_LOOK_AHEAD = 80;

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
    public WorldState(Toroidal2DPhysics space, Ship ship) {
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
    
    /*
    public Set<AbstractObject> getObstaclesFuture() {
    	Set<AbstractObject> obstacles = new HashSet<AbstractObject>();
    	Position futurePosition;
    	Position currentPosition;
    	
    	for(AbstractObject asteroid : getUnmineableAsteroids()) {
    		
    		if(asteroid.isMoveable()) {
    			currentPosition = asteroid.getPosition();
    			// futurePosition = currentPosition.setX(x);
    			// obstacles.add();
    		}
    	}
    }
	*/
    
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
     * Function will return the most efficient asteroid in teh simulation (i.e the amount
     * of resource per unit of distance)
     *
     * @return an <code>Asteroid</code> object that is most efficient to ship
     */
    public Asteroid getMostEfficientMinableAsteroid() {
        double costEffectiveness = Double.NEGATIVE_INFINITY; // Most effective asteroid (higher is better)
        double dist; // Store distance to object
        double currentCostEffectiveness; // Store the cost effectiveness of current asteroid
        Asteroid candidate = null; // The variable will hold asteroid that was found
        Position shipPos = _referenceShip.getPosition(); // Retrieve current position of ship

        for (Asteroid asteroid : getMineableAsteroids()) {
            dist = _space.findShortestDistance(shipPos, asteroid.getPosition());
            currentCostEffectiveness = asteroid.getResources().getTotal() / dist; // Cost effectiveness calculation
            if (currentCostEffectiveness > costEffectiveness
            		/* && _space.isPathClearOfObstructions(shipPos, asteroid.getPosition(), getObstacles(), 
            				_referenceShip.getRadius()) */) { // Check if asteroid closer to ship and clear of obstructions
                costEffectiveness = currentCostEffectiveness; // Reassign shortest distance
                candidate = asteroid;
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
            if (dist < shortestDist &&
            		_space.isPathClearOfObstructions(shipPos, base.getPosition(), getObstaclesExceptTeamBase(), 
            				_referenceShip.getRadius())) { // Check if the best candidate is beaten
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
            if (dist < shortestDist && 
            	_space.isPathClearOfObstructions(shipPos, energySource.getPosition(), getObstaclesExceptTeamBase(), 
            			_referenceShip.getRadius())) {
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

        return finalVelocity;
    }
    
    /**
     * Function will return a vector that will tug ship away from obstacle. It will calculate
     * the future position of ship and generate a line for collision checking. The vector will be
     * in the direction of the closest point subtracted from the collision object position. The vector
     * will be scaled to help actually apply a significant force.
     * 
     * @return
     */
    public Vector2D collisionAvoidance() {
    	Position initialShipPos = _referenceShip.getPosition();
    	Position predictedShipPos = 
    		new Position(initialShipPos.getX() + 
    				initialShipPos.getTranslationalVelocityX() * _space.getTimestep() * MAX_LOOK_AHEAD,
    		initialShipPos.getY() + 
    		initialShipPos.getTranslationalVelocityY() * _space.getTimestep() * MAX_LOOK_AHEAD);
    	double shipVelocityMagnitude = _referenceShip.getPosition().getTotalTranslationalVelocity();
    	Vector2D avoidanceVector = new Vector2D(0.0, 0.0);
    	Vector2D closest;
    	
    	AbstractObject threat = findFutureCollision(initialShipPos, predictedShipPos);
    	
    	if(threat != null) {
    		double distance = _space.findShortestDistance(initialShipPos, threat.getPosition());
    		closest = new Vector2D(calculateClosest(initialShipPos, predictedShipPos, threat.getPosition()));
    		avoidanceVector = (closest.subtract(new Vector2D(threat.getPosition()))).getUnitVector();
    		System.out.println(avoidanceVector.toString());
    		// Respond to collision more strongly as distance shortens and speed increases
    		avoidanceVector = avoidanceVector.
    				multiply(15.0 * (shipVelocityMagnitude / 
    				(distance * distance) * _referenceShip.getMass()));
    	}
    	else {
    		avoidanceVector.multiply(0.0);
    	}
    	return avoidanceVector;
    	
    }
    
    /**
     * Function will find future collision by iterating through each object. The closest
     * object is chosen as it is the MOST imminent threat.
     * 
     * @param initialPosition the initial position of ship
     * @param finalPosition the final position of ship after timestep (assuming constant velocity)
     * @return an object that will collide with the ship (this can be null)
     */
    public AbstractObject findFutureCollision(Position initialPosition, Position finalPosition) {
    	AbstractObject collisionObject = null;
    	Position collisionObjectPosition;
    	Position objectPosition;
    	boolean isCollision = false;
    	
    	for(AbstractObject object : getObstaclesExceptTeamBase()) {
    		
    		objectPosition = object.getPosition();
    		
    		if(object.isMoveable()) {
    			objectPosition.setX(objectPosition.getX() + objectPosition.getTranslationalVelocityX());
    			objectPosition.setY(objectPosition.getY() + objectPosition.getTranslationalVelocityY());
    		}
    		
    		isCollision = lineInterectCircle(_referenceShip.getPosition(), finalPosition, objectPosition,
    						object.getRadius());
    		
    		if(isCollision && (collisionObject == null || 
    				(_space.findShortestDistance(initialPosition, objectPosition) < 
    				_space.findShortestDistance(initialPosition, collisionObject.getPosition())))) {
    			collisionObject = object;
    			collisionObjectPosition = collisionObject.getPosition();
    			
        		if(collisionObject != null) {
        			if(collisionObject.isMoveable()) {
        				collisionObjectPosition.setX(collisionObjectPosition.getX() + 
        						collisionObjectPosition.getTranslationalVelocityX());
        				collisionObjectPosition.setY(collisionObjectPosition.getY() + 
        						collisionObjectPosition.getTranslationalVelocityY());
            		}
        		}
    		}
    	}
    	
    	return collisionObject;
    }
    
    /**
     * Check if line intersects a circle
     * 
     * @param initialShipPos initial location of ship
     * @param predictedShipPos future location of ship
     * @param objectPos the position of the object we are checking
     * @param objectRadius the radius of the object we are checking
     * @return a boolean of the result of the check (true for intersect and false for no intersect)
     */
    private boolean lineInterectCircle(Position initialShipPos, Position predictedShipPos, 
    									Position objectPos, double objectRadius) {
    	
    	Position closest = calculateClosest(initialShipPos, predictedShipPos, objectPos);
    	Vector2D distance = _space.findShortestDistanceVector(closest, objectPos);
    	
    	return (distance.getMagnitude() < 1.5 * (objectRadius + _referenceShip.getRadius()));
    }
    
    /**
     * Calculates the closest point on line to the object
     * 
     * @param initialShipPos the initial location of ship
     * @param predictedShipPos the future location of ship
     * @param objectPos the location of the object
     * @return a <code>Position</code> object holding the location of the closest point to object 
     */
    private Position calculateClosest(Position initialShipPos, Position predictedShipPos,
    									Position objectPos) {
    	Vector2D aheadSegment = _space.findShortestDistanceVector(initialShipPos, predictedShipPos);
    	Vector2D shipToObjectSegment = _space.findShortestDistanceVector(initialShipPos, objectPos);
    	Vector2D projection = shipToObjectSegment.vectorProject(aheadSegment);
    	Position closest;
    	
    	// Need to check couple of cases
    	if(projection.getMagnitude() < 0.0) { // Set closest to the beginning of segment
    		closest = initialShipPos;
    	}
    	else if(projection.getMagnitude() > aheadSegment.getMagnitude()){ // Set closest to the end of segment
    		closest = new Position(aheadSegment); 
    	}
    	else {
    		// Otherwise closest is the initial spot summed with the projection vector
        	closest = new Position(new Vector2D(initialShipPos).add(projection));
    	}
    	
    	return closest;
    }
    
    /**
     * Function will generate a free location that is free of obstructions
     * 
     * @return	a position that is obstruction free
     */
    public Position getRandomObstructionFreeLocation() {
    	Position randomSpot;
    	
    	randomSpot = _space.getRandomFreeLocation(new Random(), _referenceShip.getRadius());
    	
    	while(!(_space.isPathClearOfObstructions(_referenceShip.getPosition(), randomSpot, getObstacles(), 
    			_referenceShip.getRadius()))) {
    		randomSpot = _space.getRandomFreeLocation(new Random(), _referenceShip.getRadius());
    	}
    	return randomSpot;
    }
}
