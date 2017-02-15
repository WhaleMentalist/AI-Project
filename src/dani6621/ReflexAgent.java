package dani6621;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import dani6621.GraphSearch.GraphSearchNode;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.graphics.StarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * The agent that will control the ship. It has a function that
 * will select the action the agent performs based on contents of
 * <code>WorldState</code> reference data member
 */
public class ReflexAgent extends TeamClient {
	
	/**
	 * Constant will delimit whether agent can build a base
	 */
	private static final double MINIMUM_BASE_PURCHASE_DISTANCE = 500.0;
	
	/**
	 * Amount of time to wait before creating a new map
	 */
	private static final int NEW_MAP_TIMESTEP = 20;

    /**
     * Represents how agent will perceive world state. You can
     * think of this as the percept of the agent.
     */
    private WorldState knowledge;
    
    /**
     * Boolean flag to check if the ship is going to a random location for better angle
     * on objects
     */
    private boolean findingRandomLocation;
    
    /**
     * The abstracted map of the space to help the agent navigate
     * in an effective manner (i.e quickest path and avoid obstacles)
     */
    private NavigationMap map;
    
    /**
     * ------------------------------
     */
    private Stack<GraphSearchNode> objective;
    
    /**
     * Graphics to add to help debug the navigation map implementation
     */
    private ArrayList<SpacewarGraphics> graphicsToAdd;

    /**
     * Assigns ships to asteroids and beacons, as described above
     */
    public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
                                                      Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

        for (AbstractObject actionable : actionableObjects) { // Find ships and assign each an action to perform
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                actions.put(actionable.getId(), getReflexAgentAction(space, ship));
            }
            else { // Bases won't do anything for now
                actions.put(actionable.getId(), new DoNothingAction());
            }
        }
        return actions;
    }
    
    /**
     * Function retrieve an action for the agent to perform based on the state of 
     * the <code>knowledge</code> data member
     * 
     * @param space the space containing the objects and the ship
     * @param ship the ship that will be assigned the action
     * @return an action for the ship to perform
     */
    public AbstractAction getReflexAgentAction(Toroidal2DPhysics space, Ship ship) {
        AbstractAction newAction;
        Position randomLocation;
        perceive(space, ship);
        
        // Generate a new map every set time step to deal with dynamic environment
        if(space.getCurrentTimestep() % NEW_MAP_TIMESTEP == 0) {
        	 map = new NavigationMap(space, knowledge);
        }
        
        /*
        Position vertexPosition;
        List<Position> neighborPositions;
        LineGraphics line;
        
        
        // Draw nodes of graph and their respective connections
        for(int i = 0; i < map.rowNodeNumber; ++i) {
        	for(int j = 0; j < map.columnNodeNumber; ++j) {
        		vertexPosition = map.getPositionOfVertex(i, j);
        		graphicsToAdd.add(new StarGraphics(3, Color.RED, vertexPosition));
        		neighborPositions = map.getNeighborPosition(i, j);
        		
        		// Loop through each neighbor position and form a line
        		for(Position position : neighborPositions) {
        			line = new LineGraphics(vertexPosition, position, 
            				space.findShortestDistanceVector(vertexPosition, position));
            		line.setLineColor(Color.RED);
            		graphicsToAdd.add(line);
        		}
        	}
        }
        */
        
        if(knowledge.getCurrentEnergy() < WorldState.LOW_ENERGY) { // Get energy when low
            AbstractObject source = knowledge.getClosestEnergySource();

            if(source == null) { // Didn't find a source
            	if(!(findingRandomLocation)) { // Check if we are going to random location already
            		findingRandomLocation = true;
            		randomLocation = space.getRandomFreeLocation(new Random(), ship.getRadius());
            		newAction = new MoveAction(space, ship.getPosition(), 
            				randomLocation, knowledge.calculateVelocity(randomLocation));
            	}
            	else { // If we are going to random location just continue on unless we find something
            		newAction = ship.getCurrentAction();
            	}
            }
            else { // Otherwise go to the energy source
            	findingRandomLocation = false;
            	newAction = new MoveAction(space, ship.getPosition(), source.getPosition(),
            			knowledge.calculateInterceptVelocity(source));
            }
        }
        else if(ship.getResources().getTotal() > WorldState.FULL_CARGO) { // Detect full cargo
            Base closestBase = knowledge.getClosestFriendlyBase();
            
            if(closestBase != null) { // Goto base that was found
            	findingRandomLocation = false;
                newAction = new MoveAction(space, ship.getPosition(), closestBase.getPosition(),
                                            (knowledge.calculateInterceptVelocity(closestBase)));
            }
            else { // We can't find the base that is clear
            	if(!(findingRandomLocation)) { // Check if we are going to random location already
            		findingRandomLocation = true;
            		randomLocation = space.getRandomFreeLocation(new Random(), ship.getRadius());
            		newAction = new MoveAction(space, ship.getPosition(), 
            				randomLocation, knowledge.calculateVelocity(randomLocation));
            	}
            	else { // If we are going to random location just continue on unless we find something
            		newAction = ship.getCurrentAction();
            	}
            }
        }
        else { // Perform asteroid mining
            // Find closest asteroid to mine
            Asteroid closestAsteroid = knowledge.getMostEfficientMinableAsteroid();
            
            if(closestAsteroid != null) { // If we could find one cancel any move to random locations actions
            	findingRandomLocation = false;
            	
            	 if(space.getCurrentTimestep() % NEW_MAP_TIMESTEP == 0) {
                 	GraphSearch search = new GraphSearch(map, ship, closestAsteroid);
                 	objective = search.aStarSearch();
                 }
                 
                 for(GraphSearchNode node : objective) {
                 	graphicsToAdd.add(new StarGraphics(3,  Color.YELLOW, node.node.position));
                 }
            	               
            	newAction = new MoveAction(space, ship.getPosition(),
                        closestAsteroid.getPosition(),
                            (knowledge.calculateInterceptVelocity(closestAsteroid)));
            }
            else { // We couldn't find an asteroid, so move to a random location that is clear
            	if(!(findingRandomLocation)) { // Check if we are going to random location already
            		findingRandomLocation = true;
            		randomLocation = space.getRandomFreeLocation(new Random(), ship.getRadius());
            		newAction = new MoveAction(space, ship.getPosition(), 
            				randomLocation, knowledge.calculateVelocity(randomLocation));
            	}
            	else { // If we are going to random location just continue on unless we find something
            		newAction = ship.getCurrentAction();
            	}
            }
        }

        return newAction;
    }

    /**
     * Allows agent to populate the <code>WorldState</code>
     * with data about its percepts
     *
     * @param space the object that contains environment agent will perceive
     */
    private void perceive(Toroidal2DPhysics space, Ship ship) {
        knowledge = new WorldState(space, ship);
    }

    @Override
    public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
    	
    }

    @Override
    public void initialize(Toroidal2DPhysics space) {
    	findingRandomLocation = false;
    	graphicsToAdd = new ArrayList<SpacewarGraphics>();
    }

    @Override
    public void shutDown(Toroidal2DPhysics space) {

    }

    @Override
    public Set<SpacewarGraphics> getGraphics() {
    	HashSet<SpacewarGraphics> graphics = new HashSet<SpacewarGraphics>();
		graphics.addAll(graphicsToAdd);
		graphicsToAdd.clear();
		return graphics;
    }

    /**
     * Function will allow the ship to build bases once it is a certain
     * minimum distance away from all distances
     */
    @Override
    public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
                                                     Set<AbstractActionableObject> actionableObjects,
                                                     ResourcePile resourcesAvailable,
                                                     PurchaseCosts purchaseCosts) {

        HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();

        if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable)) {
            for (AbstractActionableObject actionableObject : actionableObjects) {
                if (actionableObject instanceof Ship) {
                    Ship ship = (Ship) actionableObject;
                    Set<Base> bases = space.getBases();

                    // Check distance from each base
                    double minDist = Double.MAX_VALUE;
                    for (Base base : bases) {
                        if (base.getTeamName().equalsIgnoreCase(getTeamName())) {
                            double distance = space.findShortestDistance(ship.getPosition(), base.getPosition());
                            if (distance < minDist) { // Set minimum distance if found
                                minDist = distance;
                            }
                        }
                    }

                    if (minDist > MINIMUM_BASE_PURCHASE_DISTANCE) { // If the minimum distance is larger than constant then purchase a base
                        purchases.put(ship.getId(), PurchaseTypes.BASE);
                        break;
                    }
                }
            }
        }


        return purchases;
    }

    /**
     * The aggressive asteroid collector shoots if there is an enemy nearby!
     *
     * @param space the space containing objects and objects
     * @param actionableObjects the objects that can perform actions
     * @return map of object UUID to powerups
     */
    @Override
    public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
                                                           Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();
        return powerUps;
    }
}
