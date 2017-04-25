package dani6621;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
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
	 * Used to help gather team ships
	 */
	public static final String TEAM_NAME = "Padawan_Daniel_and_Flood";
	
	/**
	 * Used as threshold for ship considering base as energy source
	 */
	public static final double SUFFICIENT_ENERGY = 1000;
	
	/**
	 * Threshold for resource capacity
	 */
	public static final int RESOURCE_THRESHOLD = 1000;

	/**
	 * Threshold for refuel
	 */
	public static final double ENERGY_THRESHOLD = Ship.SHIP_MAX_ENERGY / 2.0;
	
	/**
	 * Threshold that marks a healthy ship
	 */
	public static final double HEALTHY_ENERGY = Ship.SHIP_MAX_ENERGY * 0.75;
	
    /**
     * If a maximum velocity is imposed the agent has better
     * control
     */
	public static final double MAX_VELOCITY_MAGNITUDE = 60.0;

    /**
     * Helps to avoid agent from chasing asteroid at a slow speed
     */
	public static final double MIN_VELOCITY_MAGNITUDE = 35.0;
	
	public static final Position flagOneSpawn;
	
	public static final Position flagTwoSpawn;
    
    /**
     * Contains information on team members' actions
     */
    private TeamKnowledge teamKnowledge;
    
    /**
     * Basic constructor
     * 
     * @param teamInfo	the information about the team
     */
    public WorldKnowledge(TeamKnowledge teamInfo) {
    	teamKnowledge = teamInfo;
    }
	
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
	 * Function retrieves other team flag
	 * 
	 * @param space	a reference to space
	 * @return	a <code>Flag</code> object belonging to other team
	 * 			NOTE: It can return <code>null</code>
	 */
	public static Flag getOtherTeamFlag(Toroidal2DPhysics space, Ship ship) {
		Flag otherTeamFlag = null;
		for(Flag flag : space.getFlags()) {
			if(!(flag.getTeamName().equals(ship.getTeamName()))) {
				otherTeamFlag = flag;
			}
		}
		return otherTeamFlag;
	}
	
	/**
	 * Function retrieves all flags in the space currently.
	 * Function is kind of redundant, but for now we will leave 
	 * it.
	 * 
	 * @param space	a reference to space
	 * @return	a <code>Set</code> containing flags
	 */
	public static Set<Flag> getFlags(Toroidal2DPhysics space) {
		return space.getFlags();
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
	 * Function will retrieve all ships on team
	 * 
	 * @param space	a reference to space
	 * @return	a <code>Set</code> of all team ships in game
	 */
	public static Set<Ship> getTeamShips(Toroidal2DPhysics space) {
		Set<Ship> ships = new HashSet<Ship>();
		for(Ship ship : space.getShips()) {
			if(ship.getTeamName().equals(TEAM_NAME)) {
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
		obstacles.addAll(getUnmineableAsteroids(space));
		obstacles.addAll(getBases(space));
		obstacles.addAll(getShips(space, ship));
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
		obstacles.addAll(getUnmineableAsteroids(space));
		obstacles.addAll(getNonTeamBases(space, ship));
		obstacles.addAll(getShips(space, ship));
		return obstacles;
	}
	
	/**
	 * Function will select flag carrier based on state of ship and
	 * its distance to flag
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that wants to find other team flag
	 * @return	a <code>Ship</code> that is the best candidate for flag carrying 
	 * 				NOTE: This can return <code>null</code>
	 */
	public static Ship getFlagCarrier(Toroidal2DPhysics space, Ship ship) {
		double shortestDist = Double.MAX_VALUE;
		double dist = 0.0;
		Ship candidate = null;
		Flag otherTeamFlag = WorldKnowledge.getOtherTeamFlag(space, ship);
		
		if(otherTeamFlag == null) // No flag so we can't do anything
			return null;
		
		// Check each ship on team
		for(Ship shipElement : WorldKnowledge.getTeamShips(space)) {
			if(shipElement.getEnergy() > HEALTHY_ENERGY) { // Find a healthy ship
				dist = space.findShortestDistance(shipElement.getPosition(), otherTeamFlag.getPosition());
				// Found potenial candidate
				if(dist < shortestDist) {
					shortestDist = dist;
					candidate = shipElement;
				}
			}
		}
		return candidate;
	}
	
	/**
	 * 
	 * @param space
	 * @param ship
	 * @return
	 */
	public static Ship getShipBuilder(Toroidal2DPhysics space, Ship ship) {
		Ship candidate = null;
		return null;
	}
	
	/**
	 * Function returns closest asteroid to passed ship
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that will be used as reference
	 * @oaran failedAsteroids	track the asteroids that had a failed search attempt
	 * @return	a <code>Asteroid</code> closest to ship
	 */
	public Asteroid getClosestAsteroid(Toroidal2DPhysics space, Ship ship, Set<UUID> failedAsteroids) {
		double shortestDist = Double.MAX_VALUE;
		double dist = 0.0; // Store temporary distance
		Position shipPos = ship.getPosition();
		Asteroid candidate = null;
		
		for(Asteroid asteroid : getMineableAsteroids(space)) {
			dist = space.findShortestDistance(asteroid.getPosition(), shipPos);
			if(dist < shortestDist && !(teamKnowledge.isAsteroidAssigned(asteroid)) && !(failedAsteroids.contains(asteroid.getId()))) {
				shortestDist = dist;
				candidate = asteroid;
				teamKnowledge.assignAsteroidToShip(ship, asteroid);
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
    public Base getClosestFriendlyBase(Toroidal2DPhysics space, Ship ship) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        Base candidate = null;
        Position shipPos = ship.getPosition();
        for (Base base : getTeamBases(space, ship)) { // Go through team bases
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
     * @param failedSources	the objects that could not have path formed
     * @return <code>AbstractObject</code> object that is closest to ship
     */
    public AbstractObject getClosestEnergySource(Toroidal2DPhysics space, Ship ship, Set<UUID> failedSources) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        AbstractObject candidate = null; // The variable will hold beacon that was found
        Position shipPos = ship.getPosition();
        for (AbstractObject energySource : getEnergySources(space, ship)) {
        	
        	if(failedSources.contains(energySource.getId())) // Skip objects that are unapproachable
        		continue;

            if(energySource instanceof Base) { // Check if it is base
                if(((Base) energySource).getEnergy() < SUFFICIENT_ENERGY) { // Check if base has sufficient energy
                    continue; // Skip if base is too low on energy
                }
            }
            
            dist = space.findShortestDistance(shipPos, energySource.getPosition());

            // Otherwise see if it is closer
            if (dist < shortestDist) {
                // Now check what the energy source is...
                if(energySource instanceof Base)
                {
                	// TODO: Add 'isAssign' check here as well
                	shortestDist = dist; // Reassign shortest distance
                    candidate = energySource;
                	teamKnowledge.assignBaseToShip(ship, (Base) energySource); 
                }
                else {
                	if(!(teamKnowledge.isEnergyAssigned(ship, (Beacon) energySource))) {
                		shortestDist = dist; // Reassign shortest distance
                        candidate = energySource;
                    	teamKnowledge.assignEnergyToShip(ship, (Beacon) energySource);
                	}
                }
            }
        }
        return candidate;
    }
    
    // TODO: Change way ship navigates using functions below!!! Need to experiment with controls... Definately look into overdamp vs underdamp
    
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
    private Set<AbstractObject> getEnergySources(Toroidal2DPhysics space, Ship ship) {
        Set<AbstractObject> energySources = new HashSet<AbstractObject>();
        energySources.addAll(getTeamBases(space, ship)); // All team bases are decent energy source
        energySources.addAll(space.getBeacons()); // Beacons are also energy sources
        return energySources;
    }
}
