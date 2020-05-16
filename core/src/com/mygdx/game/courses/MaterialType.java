package com.mygdx.game.courses;

public enum MaterialType {
	
	/** Ball can roll on surface, although its behaviour on that surface can differ from point to point and from material to material. */
	SURFACE,
	/** When the ball touches this material, the shot is declared invalid and the game now decides what should happen. */
	DEATH,
	/** This material causes the ball to behave differently and, generally, cannot be rolled on.<br>Might incur bouncing or reflecting of the ball.*/
	OBSTACLE,
	/** When the ball touches this material, the shot is declared successful and the game now decides what should happen. */
	GOAL,
	/** A spot on the map marked to be the start of any game on the course. There should be exactly one point of START-material in a given course. */
	START,
	/** For laughs, might be implemented later on, will influence terrain around it.<br>Effects not yet fully defined.*/
	EXPLOSIVE;
	
	// physics properties
	
}
