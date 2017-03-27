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
	 * The worst angle the ship would have to turn to perform action
	 */
	public static final double WORST_ANGLE = 180.0;

	/**
	 * Low energy threshold
	 */
	public final double LOW_ENERGY;
	
	/**
	 * Full cargo for ship to go back to base
	 */
	public final int FULL_CARGO;
	
	/**
	 * The amount of weight angle will have when finding asteroids
	 */
	public final double ANGLE_WEIGHT;
	
	/**
	 * Minimal distance required to build base
	 */
	public final double BASE_BUILD_THRESHOLD;
	
	/**
	 * Dictates a threshold of when ship should rendezvous back to 
	 * base when it needs energy and return resources
	 */
	public final int RENDEZVOUS_CARGO_HOLD_CAPACITY;
	
	/**
	 * Distance required to rendezvous back to base when it needs to 
	 * attain energy and return resources
	 */
	public final double MINIMAL_RENDEZVOUS_DISTANCE;
	
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
     * Simply a reference to use some of the functions (i.e shortest distance, obstructions)
     */
    private Toroidal2DPhysics _space;

    /**
     * The ship the function will use when performing calculations such as
     * finding the closest object (i.e the agent)
     */
    private Ship _referenceShip;
    
    /**
     * The individual containing traits that guide agent's actions
     */
    private final Individual individual;

    /**
     * Initialize <code>WorldState</code> and build data for agent
     * to use as world state
     *
     * @param space a reference to simulation containing objects
     * @param ship  the ship belonging to agent
     * @param ind	the individual assigned to the ship
     */
    public WorldState(Toroidal2DPhysics space, Ship ship, Individual ind) {
        _space = space;
        _referenceShip = ship;
        individual = ind;
        
        // Set traits
        LOW_ENERGY = individual.asteroidCollectorChromosome.ENERGY_REFUEL_THRESHOLD;
        FULL_CARGO = individual.asteroidCollectorChromosome.CARGOHOLD_CAPACITY;
        ANGLE_WEIGHT = individual.asteroidCollectorChromosome.ANGLE_WEIGHT;
        BASE_BUILD_THRESHOLD = individual.asteroidCollectorChromosome.BASE_BUILD_THRESHOLD;
        RENDEZVOUS_CARGO_HOLD_CAPACITY = individual.asteroidCollectorChromosome.RENDEZVOUS_CARGO_HOLD_CAPACITY;
        MINIMAL_RENDEZVOUS_DISTANCE = individual.asteroidCollectorChromosome.MINIMAL_RENDEZVOUS_DISTANCE;
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
     * Function will return the most efficient asteroid in teh simulation (i.e the amount
     * of resource per unit of distance)
     * 
     * @param untouchables objects that will NOT be considered
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
        double angleBetween = WORST_ANGLE; // Set to the worst angle 

        for (Asteroid asteroid : getMineableAsteroids()) {
        	
        	if(untouchables.containsKey(asteroid.getId())) { // Skip object
        		continue;
        	}
        	
            dist = _space.findShortestDistance(shipPos, asteroid.getPosition());
            toAsteroid = _space.findShortestDistanceVector(shipPos, asteroid.getPosition()); // Get vector pointing from ship to asteroid
            angleBetween = Math.toDegrees(Math.abs(pathOfShip.angleBetween(toAsteroid))); // Get angle between asteroid and ship velocity
            
            // Angle the ship velocity to target asteroid also adds to cost as angle increases
            currentCostEffectiveness = asteroid.getResources().getTotal() / (dist + angleBetween * ANGLE_WEIGHT);
            
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
        
        // Predict future position of object we are intercepting
        Position estimatedFuturePosition = new Position(objectPos.getX() + objectVel.getXValue(), 
        		objectPos.getY() + objectVel.getYValue());
        
        // Get a vector that goes to projected location from ship position
        Vector2D displacementVector = _space.findShortestDistanceVector(shipPos, estimatedFuturePosition);

        // Using constant acceleration equation to catch object
        Vector2D finalVelocity = ((displacementVector.subtract(shipVel)).
                multiply(2.0));

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
}
