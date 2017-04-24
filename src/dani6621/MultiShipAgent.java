package dani6621;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dani6621.Navigator.NavigationFailureException;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * Controls multiple ships using knowledge representation that maps ships to
 * assigned duties. 
 * 
 * @author dani6621
 *
 */
public class MultiShipAgent extends TeamClient {
	
	/**
	 * Amount of time that must elapse before agent can replan
	 */
	private static final int REPLAN_TIME_STEP = 10;
	
	/**
	 * Number of new plans that can form if search fails for navigation
	 */
	private static final int MAX_RETRIES = 3;
	
	/**
	 * Track if the program has initialized properly
	 */
	private boolean INITIALIZED = false;
	
	/**
	 * Debug mode for graphics
	 */
	public static final boolean DEBUG_MODE = false;
	
	/**
	 * Member will contain information pertaining to team actions
	 */
	private TeamKnowledge teamKnowledge;

	@Override
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		
		if(!INITIALIZED) { // Initialize beginning ships with individual navigators
			for(Ship ship : WorldKnowledge.getTeamShips(space)) {
				teamKnowledge.assignShipToNavigator(ship, new Navigator(DEBUG_MODE));
			}
			INITIALIZED = true;
		}
		
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

        for (AbstractObject actionable : actionableObjects) { // Find ships and assign each an action to perform
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                actions.put(actionable.getId(), getShipAction(space, ship));
            }
            else { // Bases won't do anything for now
                actions.put(actionable.getId(), new DoNothingAction());
            }
        }
        return actions;
	}

	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		teamKnowledge.updateAssignments(space);
	}

	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		return null;
	}

	@Override
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, ResourcePile resourcesAvailable,
			PurchaseCosts purchaseCosts) {
		return null;
	}

	@Override
	public void initialize(Toroidal2DPhysics space) {
		teamKnowledge = new TeamKnowledge();
	}

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		return null;
	}
	
	/**
     * Function retrieve an action for the ship to perform based on the world 
     * state and the team state. If no action is found then the ship shall perform
     * a <code>DoNothingAction</code> as default
     * 
     * @param space the space containing the objects and the ship
     * @param ship the ship that will be assigned the action
     * @return an action for the ship to perform
     */
    public AbstractAction getShipAction(Toroidal2DPhysics space, Ship ship) {
        AbstractAction newAction = new DoNothingAction();
        WorldKnowledge knowledge = new WorldKnowledge(teamKnowledge);
        
        // Asteroid Gathering Component
        if(ship.getEnergy() < WorldKnowledge.ENERGY_THRESHOLD) { // Low Energy State
        	retrieveEnergy(space, ship, knowledge);
            newAction = teamKnowledge.getTeamMemberAction(space, ship);
        }
        else if(ship.getResources().getTotal() > WorldKnowledge.RESOURCE_THRESHOLD) { // High Cargohold State
        	returnResources(space, ship, knowledge);
        	newAction = teamKnowledge.getTeamMemberAction(space, ship);
        }
        else { // Asteroid Gathering State
        	asteroidMine(space, ship, knowledge);
        	newAction = teamKnowledge.getTeamMemberAction(space, ship);
        	
        	if(newAction instanceof DoNothingAction) { // Algorithm couldn't find a suitable asteroid
        		retrieveEnergy(space, ship, knowledge); // Let's just get some energy to help
        		newAction = teamKnowledge.getTeamMemberAction(space, ship);
        	}
        }
        
        return newAction;
    }
    
    /**
     * Method will compute asteroid for ship to go after
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * @param knowledge	the reference to world state
     */
    private void asteroidMine(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
        Set<UUID> failedAsteroids = new HashSet<UUID>(); // Maintain reference to asteroids that could not be approached
        Asteroid closestAsteroid = knowledge.getClosestAsteroid(space, ship, failedAsteroids); // Find closest asteroid to mine

        if(closestAsteroid != null) {
        	if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) { // Replan path at set time interval
        		for(int i = 0; ; ++i) {
        			try {
        				teamKnowledge.generateTeamMemberPath(space, ship, 
                    			closestAsteroid, WorldKnowledge.getAllObstacles(space, ship)); // Gets the path of ship
        			}
        			catch(NavigationFailureException e) {
        				if(i < MAX_RETRIES) {
        					if(closestAsteroid != null) {
        						failedAsteroids.add(closestAsteroid.getId()); // Add asteroid to list of unapproachable
                				teamKnowledge.unassignAsteroidToShip(closestAsteroid); // Need to unassign asteroid in team knowledge
                				closestAsteroid = knowledge.getClosestAsteroid(space, ship, failedAsteroids); // Find closest asteroid to mine
                				teamKnowledge.generateTeamMemberPath(space, ship, 
                            			closestAsteroid, WorldKnowledge.getAllObstacles(space, ship)); // Gets the path of ship
        					}
        				}
        				else {
        					break;
        				}
        			}
               		break;
        		}
        	}
        }
    }
    
    private void retrieveEnergy(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
    	Set<UUID> failedSources = new HashSet<UUID>(); // Maintain reference to asteroids that could not be approached
        AbstractObject closestEnergySource = knowledge.getClosestEnergySource(space, ship, failedSources); // Find closest asteroid to mine

        if(closestEnergySource != null) {
        	if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) { // Replan path at set time interval
        		for(int i = 0; ; ++i) {
        			try {
        				teamKnowledge.generateTeamMemberPath(space, ship, 
        						closestEnergySource, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
        			}
        			catch(NavigationFailureException e) {
        				if(i < MAX_RETRIES) {
        					if(closestEnergySource != null) {
        						failedSources.add(closestEnergySource.getId()); // Add energy to list of unapproachable
                				teamKnowledge.unassignEnergyToShip(closestEnergySource); // Need to unassign energy in team knowledge
                				closestEnergySource = knowledge.getClosestEnergySource(space, ship, failedSources); // Find closest asteroid to mine
                				teamKnowledge.generateTeamMemberPath(space, ship, 
                						closestEnergySource, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
        					}
        				}
        				else {
        					break;
        				}
        			}
               		break;
        		}
        	}
        }
    }
    
    /**
     * Method will compute base for ship to return resources 
     * 
     * 
     * @param space	the space the ship is in
     * @param ship	the reference to specified ship
     * 
     */
    private void returnResources(Toroidal2DPhysics space, Ship ship, WorldKnowledge knowledge) {
    	Set<UUID> failedBases = new HashSet<UUID>(); 
    	Base closestBase = knowledge.getClosestFriendlyBase(space, ship);
        
        if(closestBase != null) { // Goto base that was found
        	// Replan route
            if(space.getCurrentTimestep() % REPLAN_TIME_STEP == 0) {
            	for(int i = 0; ; ++i) { // Allow contingnecy plan to form when failure occurs
                	try {
                		teamKnowledge.generateTeamMemberPath(space, ship, closestBase, 
                    			WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship));
                	}
                	catch(Navigator.NavigationFailureException e) { // After a number of retries give up
                		if(i < MAX_RETRIES) {
                			if(closestBase != null) {
                				// TODO: Figure out assignment...
                				failedBases.add(closestBase.getId()); // Add energy to list of unapproachable
                				teamKnowledge.generateTeamMemberPath(space, ship, 
                						closestBase, WorldKnowledge.getAllObstaclesExceptTeamBases(space, ship)); // Gets the path of ship
                			}
                			else { // Heh... We ran out of bases. Not much we can do besides performing another action
                				break;
                			}

                		}
                		else {
                			break;
                		}
                	}
                	break;
                }
            }
        }
    }
}
