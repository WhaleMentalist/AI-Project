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
	 * Dictates if base is at the location
	 */
	public static final double BASE_AT_LOCATION_THRESHOLD = 100.0;
	
	/**
	 * Used as threshold for ship considering base as energy source
	 */
	public static final double SUFFICIENT_ENERGY = 1600;
	
	/**
	 * Threshold for resource capacity
	 */
	public static final int RESOURCE_THRESHOLD = 800;
	
	/**
	 * Signifies if ship is close enough to location to build a base
	 */
	public static final double BASE_BUILD_THRESHOLD = 30.0;

	/**
	 * Threshold for refuel
	 */
	public static final double ENERGY_THRESHOLD = Ship.SHIP_MAX_ENERGY * 0.4;
	
	/**
	 * Threshold that marks a healthy ship
	 */
	public static final double HEALTHY_ENERGY = Ship.SHIP_MAX_ENERGY * 0.65;
	
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
     * Contains the reference to agent
     */
    private static String teamName;
	
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
	public static Flag getOtherTeamFlag(Toroidal2DPhysics space) {
		Flag otherTeamFlag = null;
		for(Flag flag : space.getFlags()) {
			if(!(flag.getTeamName().equalsIgnoreCase(teamName))) {
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
	 * @param teamName	the team name that will be used to compare team
	 * @return	a <code>Set</code> of bases on same team as ship
	 */
	public static Set<Base> getTeamBases(Toroidal2DPhysics space) {
		Set<Base> bases = new HashSet<Base>();
		for(Base base : space.getBases()) {
			if(base.getTeam().getTeamName().equalsIgnoreCase(teamName)) {
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
			if(!(base.getTeam().getTeamName().equalsIgnoreCase(ship.getTeamName()))) {
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
			if(ship.getTeamName().equalsIgnoreCase(teamName)) {
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
	 * @param state	the team information and domain knowledge
	 * @return	a <code>Ship</code> that is the best candidate for flag carrying 
	 * 				NOTE: This can return <code>null</code>
	 */
	public static Ship getFlagCarrier(Toroidal2DPhysics space, StateRepresentation state) {
		double shortestDist = Double.MAX_VALUE;
		double dist = 0.0;
		Ship candidate = null;
		Flag otherTeamFlag = WorldKnowledge.getOtherTeamFlag(space);
		
		if(otherTeamFlag == null) {// No flag so we can't do anything
			return null;
		}
		
		// Check each ship on team
		for(Ship shipElement : WorldKnowledge.getTeamShips(space)) {
			if(shipElement.isCarryingFlag()) { // If anyone happens to be carrying the flag then they will be carrier
				candidate = shipElement;
				break; // Skip the formalities we found obvious case
			}
			
			// Make sure flag carrier isn't being assigned twice
			if(!(shipElement.getId().equals(state.getFlagCarrierOneID()))) {
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
	 * Function will retrieve the ship that will act as the base builder
	 * 
	 * @param space	a space reference
	 * @param state	the team information and general domain knowledge
	 * 
	 * @return	the <code>Ship</code> selected for base building...
	 * 				NOTE: This can return <code>null</code>
	 */
	public static Ship getBaseBuilder(Toroidal2DPhysics space, StateRepresentation state) {
		Ship candidate = null;
		double shortestDist = Double.MAX_VALUE;
		double dist = 0.0;
		Position[] baseLocations = state.getConvientBaseBuildingLocations();
		
		// Bases already built! We don't need to assign a base builder
		if(isBaseBuiltAtLocation(space, baseLocations[0]) && isBaseBuiltAtLocation(space, baseLocations[1])) {
			return null;
		}
		
		for(Ship shipElement : getTeamShips(space)) {
			
			if(shipElement.getEnergy() > HEALTHY_ENERGY && shipElement.getResources().getTotal() < WorldKnowledge.RESOURCE_THRESHOLD) {
				dist = Math.min(space.findShortestDistance(shipElement.getPosition(), baseLocations[0]), 
						space.findShortestDistance(shipElement.getPosition(), baseLocations[1]));
				if(dist < shortestDist) {
					candidate = shipElement;
					shortestDist = dist;
				}
			}
		}
		return candidate;
	}
	
	/**
	 * Function returns closest asteroid to passed ship. It 
	 * will ensure that the closest asteroid was never assigned.
	 * 
	 * @param space	a reference to space
	 * @param ship	the ship that will be used as reference
	 * @param state	team state
	 * @return	an unassinged <code>Asteroid</code> closest to ship
	 */
	public static Asteroid getClosestAsteroid(Toroidal2DPhysics space, Ship ship, StateRepresentation state) {
		double shortestDist = Double.MAX_VALUE;
		double dist = 0.0; // Store temporary distance
		Position shipPos = ship.getPosition();
		Asteroid candidate = null;
		
		for(Asteroid asteroid : getMineableAsteroids(space)) {
			dist = space.findShortestDistance(asteroid.getPosition(), shipPos);
			if(dist < shortestDist && !(state.isAsteroidAssigned(asteroid.getId()))) {
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
     * @param shipPos	the position of the ship
     * @return  <code>Base</code> object closest to ship
     */
    public static Base getClosestFriendlyBase(Toroidal2DPhysics space, Position shipPos) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        Base candidate = null;
        for (Base base : getTeamBases(space)) { // Go through team bases
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
     * @param state	the team information stored as state
     * @return <code>AbstractObject</code> object that is closest to ship
     */
    public static AbstractObject getClosestEnergySource(Toroidal2DPhysics space, Ship ship, StateRepresentation state) {
        double shortestDist = Double.POSITIVE_INFINITY;
        double dist;
        AbstractObject candidate = null; // The variable will hold beacon that was found
        Position shipPos = ship.getPosition();
        for (AbstractObject energySource : getEnergySources(space)) {
        	
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
                	if(!(state.isBaseAssigned(energySource.getId()))) {
                       	shortestDist = dist; // Reassign shortest distance
                        candidate = energySource;
                    	state.assignBaseToShip(energySource.getId(), ship.getId()); 
                	}
                }
                else {
                	if(!(state.isBeaconAssigned(energySource.getId()))) {
                		shortestDist = dist; // Reassign shortest distance
                        candidate = energySource;
                    	state.assignBeaconToShip(energySource.getId(), ship.getId());
                	}
                }
            }
        }
        return candidate;
    }
    
/*    *//**
     * Function will check if team base is built at the specified location
     * 
     * @param space	a reference to space
     * @param ship	the ship that dicates the team for bases
     * @param position	the position we wish to check
     * @return	the result as boolen
     
    public boolean isBaseBuiltAtLocation(Toroidal2DPhysics space, Ship ship, Position position) {
    	boolean result = false;
    	for(Base base : WorldKnowledge.getTeamBases(space, ship)) {
    		if(space.findShortestDistance(base.getPosition(), position) < BASE_AT_LOCATION_THRESHOLD) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * Function will check if team base is built at the specified location
     * 
     * @param space	a reference to space
     * @param position	the position we wish to check
     * @return	the result as boolen
     */
    public static boolean isBaseBuiltAtLocation(Toroidal2DPhysics space, Position position) {
    	boolean result = false;
    	for(Base base : WorldKnowledge.getTeamBases(space)) {
    		if(space.findShortestDistance(base.getPosition(), position) < WorldKnowledge.BASE_AT_LOCATION_THRESHOLD) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * Function will return the closest base building site
     * 
     * @param space	a reference to space
     * @param ship	the ship that wishes to find base building location
     * @param state	the team information and general domain knowledge
     * @return	a <code>Position</code> representing base building site
     */
    public static Position getClosestBaseBuildingSite(Toroidal2DPhysics space, Ship ship, StateRepresentation state) {
    	double shortestDist = Double.MAX_VALUE;
    	double dist = 0.0;
    	Position candidate = null;
    	
    	Position[] baseSites = state.getConvientBaseBuildingLocations(); // Retrieve the good base location
    	
    	for(Position pos : baseSites) { // Iterate through each convient base
    		dist = space.findShortestDistance(pos, ship.getPosition());
    		// Find closest one
    		if(dist < shortestDist && !(isBaseBuiltAtLocation(space, pos))) {
    			shortestDist = dist;
    			candidate = pos;
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
     * Function will set the name the knowledge will
     * use when gathering objects such as ships or bases
     * 
     * @param name	the name of the team
     */
    public static void setTeamName(String name) {
    	teamName = name;
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
    private static Set<AbstractObject> getEnergySources(Toroidal2DPhysics space) {
        Set<AbstractObject> energySources = new HashSet<AbstractObject>();
        energySources.addAll(getTeamBases(space)); // All team bases are decent energy source
        energySources.addAll(space.getBeacons()); // Beacons are also energy sources
        return energySources;
    }
}
