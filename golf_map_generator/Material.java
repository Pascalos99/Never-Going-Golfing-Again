package golf_map_generator;

import static golf_map_generator.MaterialType.*;

public enum Material {			// index
	GRASS			(SURFACE,		0),
	SAND			(SURFACE,		1),
	WATER			(DEATH, 		2),
	ICE				(SURFACE, 		3),
	TREE			(OBSTACLE, 		4),
	STARTING_POINT	(START, 		5),
	FLAG			(GOAL, 			6),
	TNT				(EXPLOSIVE, 	7);
	
	public final MaterialType type;
	public final int index;
	// graphical properties
	// other properties
	
	Material(MaterialType type, int index) {
		this.type = type;
		this.index = index;
	}
}
