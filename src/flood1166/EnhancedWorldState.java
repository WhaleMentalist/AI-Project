package flood1166;

import spacesettlers.objects.*;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;

import java.util.*;

/**
 *
 */
public class EnhancedWorldState {

    /**
     * Constants helps identify a low energy threshold
     */
	public static final double VERY_LOW_ENERGY = Ship.SHIP_MAX_ENERGY/5.0;
    public static final double LOW_ENERGY = Ship.SHIP_MAX_ENERGY/3.0;

    /**
     * Constant helps to delimit return to base
     */
    public static final int FULL_CARGO = 4000;

    /**
     * Effort to control velocity of ship
     */
    public static final double MAX_VELOCITY_MAGNITUDE = 50.0;

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
    public EnhancedWorldState(Toroidal2DPhysics space, Ship ship) {
        _space = space;
        _referenceShip = ship;
    }
    
    /**
     * Retrieve the ship that is contained in knowledge 
     * representation
     * 
     * @return the ship that is associated with knowledge
     */
    public Ship getReferenceShip(){
    	return _referenceShip;
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
     * 
     * @return A <code>Set</code> of bases in simulation except for bases belonging to ship
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
    
    /**Function retrieves all current obstacles in simulation except for
     * the bases belonging to _ship
     * 
     * @return the <code>Set</code> containing all obstacles in simulation
     * except team bases belonging to _ship
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
            if (currentCostEffectiveness > costEffectiveness &&
                    _space.isPathClearOfObstructions(shipPos, asteroid.getPosition(),
                            getObstacles(), _referenceShip.getRadius())) { // Check if asteroid closer to ship and clear of obstructions
                costEffectiveness = currentCostEffectiveness; // Reassign shortest distance
                candidate = asteroid;
            }
        }
        return candidate;
    }

    /**		Function retrieves base object that is closest to our ship
     * 
     * @return Base object
     */
    public Base getClosestFriendlyBase() {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        Base candidate = null;
        Position shipPos = _referenceShip.getPosition();
        for (Base base : getTeamBases()) {
            dist = _space.findShortestDistance(shipPos, base.getPosition());
            if (dist < shortestDist &&
                    _space.isPathClearOfObstructions(shipPos, base.getPosition(),
                            getObstaclesExceptTeamBase(), _referenceShip.getRadius())) {
                shortestDist = dist;
                candidate = base;
            }
        }
        return candidate;
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
     * Function will return closest beacon to ship using a straight path (i.e distance formula)
     *
     * @return <code>Beacon</code> object that is closest to ship
     */
    public Beacon getClosestBeacon() {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        Beacon candidate = null; // The variable will hold beacon that was found
        Position shipPos = _referenceShip.getPosition();
        for (Beacon beacon : _space.getBeacons()) {
            dist = _space.findShortestDistance(_referenceShip.getPosition(), beacon.getPosition());
            if (dist < shortestDist &&
                    _space.isPathClearOfObstructions(shipPos, beacon.getPosition(),
                            getObstacles(), _referenceShip.getRadius())) {
                shortestDist = dist; // Reassign shortest distance
                candidate = beacon;
            }
        }
        return candidate;
    }
    
    
    /**  Returns the nearest re-charge point.  Doesn't discriminate or prioritize between
     * bases and beacons.
     * 
     * @return AbstractObject of closest base or beacon
     */
    public AbstractObject getClosestRecharge(){
    	
    	Beacon candidate = getClosestBeacon();
    	Base candidate2 = getClosestFriendlyBase();
    	Position shipPos = _referenceShip.getPosition();
    	double dist = Double.MAX_VALUE;
    	double dist2 = Double.MAX_VALUE;
    	
    	//Some if statements to fix null pointer
    	if(candidate != null){
    		dist = _space.findShortestDistance(_referenceShip.getPosition(), candidate.getPosition());}
    	if(candidate2 != null){
    		dist2 = dist = _space.findShortestDistance(shipPos, candidate2.getPosition());
    	}
    	if(candidate == null & candidate2 == null)
    	{
    		return null;
    	}
    	if (dist < dist2){
    		return (AbstractObject)candidate;
    	}
    	else{
    		return (AbstractObject)candidate2;
    	}
    	
    }

    /**
     * 
     * @param pass in the object (beacon or base) the ship is headed towards
     * @return Asteroid that is between the target and thisX with an angle of
     * less that 45 degrees.
     */
    public Asteroid findAsteroidsBetween(AbstractObject target){
    	
    	Asteroid best = null;
    	double bestAngle = 0;  
    	double angleBetween = 0;
    	double asteroidDistance;
    	Position shipPosition = _referenceShip.getPosition();
    	Position targetPosition = target.getPosition();
    	double distanceLimit = _space.findShortestDistance(shipPosition, targetPosition);

    	Vector2D pathToAsteroid;
    	Vector2D pathToTarget;
    	
    	for(Asteroid ast : getMineableAsteroids()){
    		pathToAsteroid = _space.findShortestDistanceVector(shipPosition,ast.getPosition());
    		pathToTarget = _space.findShortestDistanceVector(shipPosition, target.getPosition());
    		asteroidDistance = _space.findShortestDistance(shipPosition, ast.getPosition());
    		angleBetween = Math.abs(pathToTarget.angleBetween(pathToAsteroid));
    		
    		//If asteroid falls within 45 degree scope of the vector between ship and 
    		//destination, and the distance between the ship and asteroid is less that
    		//that of the ship and the base, this is the best asteroid.
    		if(angleBetween < Math.PI/8 && angleBetween < bestAngle && 
    				asteroidDistance<distanceLimit){
    			bestAngle = angleBetween;
    			best = ast;
    		}
    	}
    	return best;
    }
    

    /**
     * Function helps ship to intercept object in space for faster retrieval
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
		
        return finalVelocity;
    }
}
