package dani6621;

import java.util.HashSet;
import java.util.Set;

import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Flag;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;

/**
 * The class responsible for representing the world state i.e the location 
 * of objects, velocity of objects, and utility functions for determining 
 * distances
 * 
 * @author dani6621
 *
 */
public class WorldKnowledge {
	
	/**
	 * Used as threshold for ship considering base as energy source
	 */
	private static final double SUFFICIENT_ENERGY = 1000;
	
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
	 * Function retrieves all mineable asteroids in the space currently
	 * 
	 * @param space	a reference to space
	 * @return	a <code>Set</code> containing mineable asteroids
	 */
	public static Set<Asteroid> getMineableAsteroids(Toroidal2DPhysics space) {
		Set<Asteroid> mineableAsteroids = new HashSet<Asteroid>();
		for(Asteroid asteroid : space.getAsteroids()) {
			if(asteroid.isMineable()) {
				mineableAsteroids.add(asteroid);
			}
		}
		return mineableAsteroids;
	}
	
	/**
	 * Function retrieves all flags in the space currently
	 * 
	 * @param space	a reference to space
	 * @return	a <code>Set</code> containing flags
	 */
	public static Set<Flag> getFlags(Toroidal2DPhysics space) {
		Set<Flag> flags = new HashSet<Flag>();
		for(Flag flag : space.getFlags()) {
			flags.add(flag);
		}
		return flags;
	}
	
	/**
	 * Function retrieves all unmineable asteroids in space currently
	 * 
	 * @param space	a reference to space
	 * @return	a <code>Set</code> containing unmineable asteroids
	 */
	public static Set<Asteroid> getUnmineableAsteroids(Toroidal2DPhysics space) {
		Set<Asteroid> unmineableAsteroids = new HashSet<Asteroid>();
		for(Asteroid asteroid : space.getAsteroids()) {
			if(!(asteroid.isMineable())) {
				unmineableAsteroids.add(asteroid);
			}
		}
		return unmineableAsteroids;
	}
	
	/**
	 * Function retrieves all team bases
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that will be used to compare team
	 * @return	a <code>Set</code> of bases on same team as ship
	 */
	public static Set<Base> getTeamBases(Toroidal2DPhysics space, Ship ship) {
		Set<Base> bases = new HashSet<Base>();
		for(Base base : space.getBases()) {
			if(base.getTeam().getTeamName().equals(ship.getTeamName())) {
				bases.add(base);
			}
		}
		return bases;
	}
	
	/**
	 * Function retrieves all bases in game
	 * 
	 * @return	a <code>Set</code> of bases in game
	 */
	public static Set<Base> getBases(Toroidal2DPhysics space) {
		Set<Base> bases = new HashSet<Base>();
        for (Base base : space.getBases()) {
            bases.add(base);
        }
        return bases;
	}
	
	/**
	 * Function retrieves all non-team bases
	 * 
	 * @param space	a reference to the space
	 * @param ship	the ship that will be used to compare team
	 * @return	a <code>Set</code> containing base not on ship's team
	 */
	public static Set<Base> getNonTeamBases(Toroidal2DPhysics space, Ship ship) {
		Set<Base> bases = new HashSet<Base>();
		for(Base base : space.getBases()) {
			if(!(base.getTeam().getTeamName().equals(ship.getTeamName()))) {
				bases.add(base);
			}
		}
		return bases;
	}
	
	/**
	 * Function will retrieve all ships in the game, except for
	 * ship passed in as parameter
	 * 
	 * @param space	a reference to space
	 * @param refShip	the ship that will be used to compare
	 * @return	a <code>Set</code> of all ships in the game
	 */
	public static Set<Ship> getShips(Toroidal2DPhysics space, Ship refShip) {
		Set<Ship> ships = new HashSet<Ship>();
        for (Ship ship : space.getShips()) {
            if (!(ship.getId().equals(refShip.getId()))) { // Add all other ships except reference ship
                ships.add(ship);
            }
        }
        return ships;
	}
	
	/**
	 * Function retrieves all obstacles in space
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that will be used as reference for data collection	
	 * @return	a <code>Set</code> containing all the obstacles in the space currently
	 */
	public static Set<AbstractObject> getAllObstacles(Toroidal2DPhysics space, Ship ship) {
		Set<AbstractObject> obstacles = new HashSet<AbstractObject>();
		obstacles.addAll(WorldKnowledge.getUnmineableAsteroids(space));
		obstacles.addAll(WorldKnowledge.getBases(space));
		obstacles.addAll(WorldKnowledge.getShips(space, ship));
		return obstacles;
	}
	
	/**
	 * Function retrieves all obstacles with the exception for team bases
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that will be used as reference for data collection
	 * @return	a <code>Set</code> containing all obstacles in the space currently
	 */
	public static Set<AbstractObject> getAllObstaclesExceptTeamBases(Toroidal2DPhysics space, Ship ship) {
		Set<AbstractObject> obstacles = new HashSet<AbstractObject>();
		obstacles.addAll(WorldKnowledge.getUnmineableAsteroids(space));
		obstacles.addAll(WorldKnowledge.getNonTeamBases(space, ship));
		obstacles.addAll(WorldKnowledge.getShips(space, ship));
		return obstacles;
	}
	
	/**
	 * Function returns closest asteroid to passed ship
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that will be used as reference
	 * @return	a <code>Asteroid</code> closest to ship
	 */
	public static Asteroid getClosestAsteroid(Toroidal2DPhysics space, Ship ship) {
		double shortestDist = Double.MAX_VALUE;
		double dist = 0.0; // Store temporary distance
		Position shipPos = ship.getPosition();
		Asteroid candidate = null;
		
		for(Asteroid asteroid : WorldKnowledge.getMineableAsteroids(space)) {
			dist = space.findShortestDistance(asteroid.getPosition(), shipPos);
			if(dist < shortestDist) {
				shortestDist = dist;
				candidate = asteroid;
			}
		}
		return candidate;
	}
	
	/**
     * Function will retrieve the closest team base to ship
     *
     * @param space	a reference to space
     * @param ship	the ship used as reference
     * @return  <code>Base</code> object closest to ship
     */
    public static Base getClosestFriendlyBase(Toroidal2DPhysics space, Ship ship) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        Base candidate = null;
        Position shipPos = ship.getPosition();
        for (Base base : WorldKnowledge.getTeamBases(space, ship)) { // Go through team bases
            dist = space.findShortestDistance(shipPos, base.getPosition());
            if (dist < shortestDist) { // Check if the best candidate is beaten
                shortestDist = dist;
                candidate = base;
            }
        }
        return candidate;
    }
    
    /**
     * Function will return closest energy source to ship using a straight path
     * (i.e distance formula). It will consider friendly bases with sufficient
     * energy
     * 
     * @param space	a reference to space
     * @param ship	the ship used as reference
     * @return <code>AbstractObject</code> object that is closest to ship
     */
    public static AbstractObject getClosestEnergySource(Toroidal2DPhysics space, Ship ship) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        AbstractObject candidate = null; // The variable will hold beacon that was found
        Position shipPos = ship.getPosition();
        for (AbstractObject energySource : getEnergySources(space, ship)) {
        	
            dist = space.findShortestDistance(shipPos, energySource.getPosition());

            if(energySource instanceof Base) { // Check if it is base
                if(((Base) energySource).getEnergy() < SUFFICIENT_ENERGY) { // Check if base has sufficient energy
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
    
    // TODO: Change way ship navigates using functions below!!! Need to experiment with controls...
    
    /**
     * Function helps ship to intercept object in space for faster retrieval.
     * It uses the constant acceleration equation to find new velocity and it
     * imposes restrictions on top speed (keep better control).
     *
     * @param space	a reference to space
     * @param ship	the ship used as reference
     * @param object target object for ship to intercept
     * @return a new velocity for ship to accelerate
     */
    public static Vector2D calculateInterceptVelocity(Toroidal2DPhysics space, Ship ship, AbstractObject object) {
        Position shipPos = ship.getPosition();
        Position objectPos = object.getPosition();
        Vector2D shipVel = shipPos.getTranslationalVelocity();
        Vector2D objectVel = shipPos.getTranslationalVelocity();

        double distance = space.findShortestDistance(shipPos, objectPos);
        double estimatedArrivalTime = distance / shipVel.getMagnitude();
        
        // Predict future position of object we are intercepting
        Position estimatedFuturePosition = new Position(objectPos.getX() + objectVel.getXValue() *
                estimatedArrivalTime, objectPos.getY() + objectVel.getYValue() * estimatedArrivalTime);
        
        // Get a vector that does to projected location from ship position
        Vector2D displacementVector = space.findShortestDistanceVector(shipPos, estimatedFuturePosition);

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
    public static Vector2D calculateVelocity(Toroidal2DPhysics space, Ship ship, Position location) {
        Position shipPos = ship.getPosition();
        Position objectPos = location;
        Vector2D shipVel = shipPos.getTranslationalVelocity();
        Vector2D objectVel = shipPos.getTranslationalVelocity();

        double distance = space.findShortestDistance(shipPos, objectPos);
        double estimatedArrivalTime = distance / shipVel.getMagnitude();

        Position estimatedFuturePosition = new Position(objectPos.getX() + objectVel.getXValue() *
                estimatedArrivalTime, objectPos.getY() + objectVel.getYValue() * estimatedArrivalTime);

        Vector2D displacementVector = space.findShortestDistanceVector(shipPos, estimatedFuturePosition);

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
     * Function will return all potenial energy sources for
     * ship
     *
     * @param space	a reference to space
     * @param ship	the ship used as reference
     * @return a <code>Set</code> containing all energy source
     *          objects
     */
    private static Set<AbstractObject> getEnergySources(Toroidal2DPhysics space, Ship ship) {
        Set<AbstractObject> energySources = new HashSet<AbstractObject>();
        energySources.addAll(getTeamBases(space, ship)); // All team bases are decent energy source
        energySources.addAll(space.getBeacons()); // Beacons are also energy sources
        return energySources;
    }
}
