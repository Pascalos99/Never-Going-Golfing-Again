package com.mygdx.game.courses;

import java.awt.*;

import static com.mygdx.game.courses.MaterialType.*;

@Deprecated
public enum Material {			// index
	GRASS			(SURFACE,		0,		new Color(0x47bf67)),
	HILL			(SURFACE,		1,		new Color(0x2e8c47)),
	MOUNTAIN		(SURFACE,		2,		new Color(0x637066)),
	SAND			(SURFACE,		3,		new Color(0xebe188)),
	WATER			(DEATH, 		4,		new Color(0x3085c2)),
	ICE				(SURFACE, 		5,		new Color(0xccf2ff)),
	TREE			(OBSTACLE, 		6,		new Color(0x1b422a)),
	STARTING_POINT	(START, 		7,		new Color(0xfc9d62)),
	FLAG			(GOAL, 			8,		new Color(0x404040)),
	TNT				(EXPLOSIVE, 	9,		new Color(0xf74343));
	
	public final MaterialType type;
	/** This index should always be perfectly alligned with the {@link #values()} index of the corresponding Material. */
	public final int index;
	/** Were we to use a mini-map, this would be the color this material would display on the map (for now just used for visualization of map-generation) */
	public final Color map_color;
	// graphical properties
	// other properties
	
	Material(MaterialType type, int index, Color map_color) {
		this.type = type;
		this.index = index;
		this.map_color = map_color;
	}
}
