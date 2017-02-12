package flood1166;

import spacesettlers.objects.*;

/*
 *	This class will store previous actions taken by the agent.  Since a significant 
 *	amount of time is wasted in turning the ship, this internal state will ensure that
 *	once a ship is pursuing a target, it won't suddenly change targets.
 */
public class InternalState {
	
	
	/*
	 * Private members
	 */	
	
	
	//Boolean variables that shows whether the ship is currently pursuing an asteroid,
	//a charging point (beacon or base), or an asteroid between the ship and a charging point
	private boolean boolAsteroid;
	private boolean boolCharge;
	private boolean boolBetween;

	//Stores the object for the cases described above.
	private Asteroid targetAsteroid;
	private AbstractObject targetCharge;
	private Asteroid between;
	
	//Constructor
	public InternalState(){
		boolAsteroid = false;
		boolCharge = false;
		boolBetween = false;

		targetAsteroid = null;
		targetCharge = null;
		between = null;
	}

	

	/*
	 * Getters/Setters for each of the private members.  Pretty self explanatory.
	 */
	
	public boolean isBoolBetween(){
		return boolBetween;
	}
	
	
	public void setBoolBetween(boolean boolBetween){
		this.boolBetween = boolBetween;
	}
	
	
	public boolean isBoolAsteroid() {
		return boolAsteroid;
	}


	public void setBoolAsteroid(boolean boolAsteroid) {
		this.boolAsteroid = boolAsteroid;
	}


	public boolean isBoolCharge() {
		return boolCharge;
	}


	public void setBoolCharge(boolean boolCharge) {
		this.boolCharge = boolCharge;
	}
	
	
	public Asteroid getBetween(){
		return between;
	}
	
	
	public void setBetweenAsteroid(Asteroid between){
		this.between = between;
	}


	public Asteroid getTargetAsteroid() {
		return targetAsteroid;
	}


	public void setTargetAsteroid(Asteroid targetAsteroid) {
		this.targetAsteroid = targetAsteroid;
	}


	public AbstractObject getTargetCharge() {
		return targetCharge;
	}


	public void setTargetCharge(AbstractObject targetCharge) {
		this.targetCharge = targetCharge;
	}


}