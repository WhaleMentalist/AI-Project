package flood1166;

import flood1166.EnhancedWorldState;
import spacesettlers.actions.*;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.*;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;

import java.util.*;


/**
 * Model based reflex agent stores internal state of objects it was 
 * pursuing. It works similar to reflex based agent, but with some 
 * internal state helping to combat indecision.
 */
public class ModelBasedReflexAgent extends TeamClient{

    /**
     * Represents how agent will perceive world state. You can
     * think of this as the percept of the agent.
     */
    private EnhancedWorldState knowledge;
    
    /**
     * The internal state the agent will maintain and query to perform
     * tasks in a much more informed fashion (i.e indecisiveness will disappear)
     */
    private InternalState internalState = new InternalState();

    /**
     * Assigns ships to asteroids and beacons, as described above
     */
    public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
                                                      Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

        //Identifies the agent's ship
        for (AbstractObject actionable : actionableObjects) {
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;
                actions.put(actionable.getId(), getReflexAgentAction(space, ship));
            }
            else {
                actions.put(actionable.getId(), new DoNothingAction());
            }
        }
        return actions;
    }

    
    /**Returns an action from the reflex agent.  This function implements all the logic
     * contained within the getReflexAgentAction and returns actions based on rules.
     * 
     * @param space the space with the objects and ship
     * @param ship the ship that will perform the action
     * @return the action the ship will perform
     */
    public AbstractAction getReflexAgentAction(Toroidal2DPhysics space, Ship ship) {

        AbstractAction newAction;
        perceive(space, ship);

        // If ship has low energy go find the nearest beacon - before performing other actions
        if (knowledge.getCurrentEnergy() < EnhancedWorldState.VERY_LOW_ENERGY) {

            AbstractObject charge = knowledge.getClosestRecharge();

            if (charge == null) {
                newAction = new DoNothingAction();
                return newAction;
            } else {
                     	
                newAction = new MoveToObjectAction(space, ship.getPosition(), charge);
                
                //Turn off asteroid pursuit state
                if(internalState.isBoolAsteroid()) {
                    internalState.setTargetAsteroid(null);
                    internalState.setBoolAsteroid(false);
                    } 
                
                //Turn off intermediate asteroid pursuit state
                if(internalState.isBoolBetween()){
                	internalState.setBetweenAsteroid(null);
                	internalState.setBoolBetween(false);             	
                }
                       
                //Turn on energy pursuit state
                if(!internalState.isBoolCharge()){
	                internalState.setBoolCharge(true);
	                internalState.setTargetCharge(charge);
                }
                
	            return newAction;
            }
        } 
        
        
        //If the cargo is full, then proceed to base
        else if (ship.getResources().getTotal() > EnhancedWorldState.FULL_CARGO) {
        
        	if(internalState.isBoolBetween()){
            	newAction = new MoveAction(space, ship.getPosition(),
            			space.getObjectById(internalState.getBetween().getId()).getPosition(),
                        knowledge.calculateInterceptVelocity(space.getObjectById(internalState.getBetween().getId())));
                return newAction;   	
            }
        	
        	
        	//Find closest base
            Base closestBase = knowledge.getClosestFriendlyBase();
            if (closestBase == null) {
                newAction = new DoNothingAction();
                return newAction;
            }
            
            //Turn asteroid state off
            if(internalState.isBoolAsteroid()) {
                internalState.setTargetAsteroid(null);
                internalState.setBoolAsteroid(false);}  
            
            if(!internalState.isBoolCharge()){
                internalState.setBoolCharge(true);
                internalState.setTargetCharge((AbstractObject)closestBase);}
            
            if(!internalState.isBoolBetween()){
            	internalState.setBetweenAsteroid(null);
            	internalState.setBoolBetween(false);
            }
            
            //Check for asteroids between
            Asteroid between = knowledge.findAsteroidsBetween(closestBase);
            
        	//If we find an asteroid between
        	if (between != null){
        		
        		//Turn off previous asteroid pursuit state
                if(internalState.isBoolAsteroid() == true) {
                    internalState.setTargetAsteroid(null);
                    internalState.setBoolAsteroid(false);}
                if(!internalState.isBoolBetween()){
	                internalState.setBetweenAsteroid(between);
	                internalState.setBoolBetween(true);}
        		
        		newAction = new MoveAction(space, ship.getPosition(),
                        space.getObjectById(between.getId()).getPosition(),
                        knowledge.calculateInterceptVelocity(space.getObjectById(between.getId())));
                return newAction;
        	}
        	else{
        		//Accelerate to the base
	            newAction = new MoveAction(space, ship.getPosition(),
	                    space.getObjectById(internalState.getTargetCharge().getId()).getPosition(),
	                    knowledge.calculateInterceptVelocity(space.getObjectById(internalState.getTargetCharge().getId())));
	
	            return newAction;
        	}
            
        }
        
        
       
        //Taking advantage of our internal state.  This will allow our agent to pick up
        //any asteroids that are between the beacon/base and the ship.
        else if (knowledge.getCurrentEnergy() < EnhancedWorldState.LOW_ENERGY) {

        	//If there is an asteroid between the ship and the beacon/base, this code
            //short circuits the rest of the rules and automatically pursues this intermediate
            //asteroid
        	if(internalState.isBoolBetween()){
            	newAction = new MoveAction(space, ship.getPosition(),
                        space.getObjectById(internalState.getBetween().getId()).getPosition(),
                        knowledge.calculateInterceptVelocity(space.getObjectById(internalState.getBetween().getId())));
                return newAction;   	
            }
        	
            AbstractObject charge = knowledge.getClosestRecharge();

            
            if (charge == null) {
                newAction = new DoNothingAction();
                return newAction;
            } else{
            	
            	//Turn on energy pursuit state
                if(!internalState.isBoolCharge()){
	                internalState.setBoolCharge(true);
	                internalState.setTargetCharge(charge);}
                    
                
            	//See if there are any asteroids between recharge point and ship
            	Asteroid between = knowledge.findAsteroidsBetween(charge);
          
            	//If we find an asteroid between
            	if (between != null){
            		
            		//Turn off previous asteroid pursuit state
	                if(internalState.isBoolAsteroid() == true) {
	                    internalState.setTargetAsteroid(null);
	                    internalState.setBoolAsteroid(false);}
	                if(!internalState.isBoolBetween()){
		                internalState.setBetweenAsteroid(between);
		                internalState.setBoolBetween(true);}
            		
            		newAction = new MoveAction(space, ship.getPosition(),
                            space.getObjectById(between.getId()).getPosition(),
                            knowledge.calculateInterceptVelocity(space.getObjectById(between.getId())));
                    return newAction;
            	} else{ //Otherwise, just proceed to base
            	
	                newAction = new MoveToObjectAction(space, ship.getPosition(), charge);
	                
	                //Turn off asteroid pursuit state
	                if(internalState.isBoolAsteroid()) {
	                    internalState.setTargetAsteroid(null);
	                    internalState.setBoolAsteroid(false);}       
	                return newAction;}
            	}
        } 
          

        else { // Perform asteroid mining
        	
            // Find closest asteroid to mine
            if (!(internalState.isBoolAsteroid())) {
            	//Uses our efficieny model to calculate internal state
                Asteroid closestAsteroid = knowledge.getMostEfficientMinableAsteroid();
                internalState.setTargetAsteroid(closestAsteroid);
                internalState.setBoolAsteroid(true);
                newAction = new MoveAction(space, ship.getPosition(),
                        space.getObjectById(internalState.getTargetAsteroid().getId()).getPosition(),
                        knowledge.calculateInterceptVelocity(space.getObjectById(internalState.getTargetAsteroid().getId())));
                return newAction;
            } 
            
            //This is where the internal state becomes relevant
            //This code prevents the ship from veering off towards another asteroid when
            //it has a target.  We found that significant time is being wasted for turns.
            //This minimizes turning
            else {  //Case where ship already has a target asteroid
                if ((internalState.isBoolAsteroid())) {
                    newAction = new MoveAction(space, ship.getPosition(),
                            space.getObjectById(internalState.getTargetAsteroid().getId()).getPosition(),
                            knowledge.calculateInterceptVelocity(space.getObjectById(internalState.getTargetAsteroid().getId())));
                    return newAction;
                }
            }
        }

        return ship.getCurrentAction();
    }

    
	/**
     * Allows agent to populate the <code>WorldState</code>
     * with data about its percepts
     *
     * @param space the object that contains environment agent will perceive
     */
    private void perceive(Toroidal2DPhysics space, Ship ship) {
        knowledge = new EnhancedWorldState(space, ship);
    }

    
    /**
     * Called by simulator after each function.  This is where the internal state will be
     * updated if any actions have been completed
     */
    @Override
    public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
        
    	//Switch internal state to false if the asteroid has been captured
    	if(internalState.isBoolAsteroid()){
    		
        	Asteroid asteroid = (Asteroid) space.getObjectById(internalState.getTargetAsteroid().getId());
        	if(asteroid != null && !(asteroid.isAlive())) {
                internalState.setTargetAsteroid(null);
                internalState.setBoolAsteroid(false);
            }
    	}
    	
    	//Switch internal state to false if the energy beacon or base has been captured
    	if(internalState.isBoolCharge()){
        	
    		if(knowledge.getCurrentEnergy() > EnhancedWorldState.LOW_ENERGY){
	        	internalState.setBoolCharge(false);
	        	internalState.setTargetCharge(null);        
    		}
    	}
    	
    	//Switch internal state to false if the asteroid between the ship and the base 
    	//has been captured
    	
    	if(internalState.isBoolBetween()){
    		
    		Asteroid between = (Asteroid) space.getObjectById(internalState.getBetween().getId());
        	if(!(between.isAlive())) {
                internalState.setBetweenAsteroid(null);
                internalState.setBoolBetween(false);
            }
    	}     
    }

    
    @Override
    public void initialize(Toroidal2DPhysics space) {

    }

    @Override
    public void shutDown(Toroidal2DPhysics space) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<SpacewarGraphics> getGraphics() {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Function will allow ship that is some minimum distance from ships to create a base
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

                    // how far away is this ship to a base of my team?
                    double minDist = Double.MAX_VALUE;
                    for (Base base : bases) {
                        if (base.getTeamName().equalsIgnoreCase(getTeamName())) {
                            double distance = space.findShortestDistance(ship.getPosition(), base.getPosition());
                            if (distance < minDist) {
                                minDist = distance;
                            }
                        }
                    }

                    if (minDist > 500) {
                        purchases.put(ship.getId(), PurchaseTypes.BASE);
                        //System.out.println("Buying a base!!");
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
     * @param space
     * @param actionableObjects
     * @return
     */
    @Override
    public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
                                                           Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();
        return powerUps;
    }
}