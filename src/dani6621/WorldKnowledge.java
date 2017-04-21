package dani6621;

import java.util.HashSet;
import java.util.Set;

import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;

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
}
