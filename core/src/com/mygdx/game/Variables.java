package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public final class Variables {
	// MATERIAL SETTINGS
	public static final double 	SAND_FRICTION = 0.178;
	public static final double	ICE_FRICTION = 0.0839;
	public static final double	MOUNTAIN_HEIGHT = 2;
	public static final double 	HILL_HEIGHT = 1;
	
	// DEFAULT CONSTRUCTOR OF PUTTINGCOURSE SETTINGS
	public static final double 	DEFAULT_FRICTION = 0.131; // ? TODO decide unit of friction
	public static final int 	DEFAULT_WIDTH = 20 * 100; //cm
	public static final int 	DEFAULT_HEIGHT = 20 * 100; //cm
	public static final double	DEFAULT_HOLE_TOLERANCE = 0.02; //cm
	public static final double 	DEFAULT_MAXIMUM_VELOCITY = 20; // cm/s ? <- physics people have to decide this
	public static final double	DEFAULT_GRAVITY = 9.812; // m/s^2
	public static final double	DEFAULT_MASS = 50;
	
	public static final String	FLAG_TEXTURE = ".\\textures\\Flag.png";
	public static final String	START_TEXTURE = ".\\textures\\Start.png";
	
	// MAP GENERATION SETTINGS
		// World-border settings
	public static final Function2d	OUT_OF_BOUNDS_FRICTION = Function2d.getConstant(100000);
	public static final	Function2d 	OUT_OF_BOUNDS_HEIGHT = new AtomFunction2d("-sin(y/8 - x/8)/8 - cos(x/8 + y/4)/8 - 2");
	
		// Settings for path generation
	public static final int 	NUM_FLAG_POS_TRIES = 2000;
	public static final double 	START_PLATFORM_RADIUS = 3;
	public static final double 	FLAG_PLATFORM_RADIUS = 3;
	public static final double 	PATH_RADIUS = 1.5;
	public static final double 	MIN_FLAG_DISTANCE = 0.5; // percentage of diagonal length of map
	public static final double	PATH_BUMPINESS = 0.2; // a value of 0 is completely smooth, anything above 1 is incredibly noisy (affects height)
	public static final double	PATH_ROUGHNESS = 0.4; // a value of 0 is completely smooth, anything above 1 is incredibly noisy (affects friction)
	
		// Value ranges for fractal generation
	public static final double 	MINIMUM_HEIGHT = -1;
	public static final double 	MAXIMUM_HEIGHT = 3;
	public static final double 	MINIMUM_FRICTION = 0.065;
	public static final double 	MAXIMUM_FRICTION = 0.196;
	
		// Settings for valid flag and start positions
	public static final double	MINIMUM_START_HEIGHT = 0.5; // inclusive
	public static final double	MAXIMUM_START_HEIGHT = 1.5; // exclusive
	public static final double	MINIMUM_FLAG_HEIGHT = 0.5; // inclusive
	public static final double	MAXIMUM_FLAG_HEIGHT = 1.5; // exclusive
	
	public static final double	MINIMUM_START_FRICTION = 0.0933; // inclusive
	public static final double	MAXIMUM_START_FRICTION = 0.169; // exclusive
	public static final double	MINIMUM_FLAG_FRICTION = 0.0933; // inclusive
	public static final double	MAXIMUM_FLAG_FRICTION = 0.169; // exclusive

		// Ball color
	public static Color 		BALL_COLOR = Color.RED;
	public static SettingsScreen.ColorSelection[] BALL_COLORS = {
			new SettingsScreen.ColorSelection("Red", Color.RED),
			new SettingsScreen.ColorSelection("Yellow", Color.YELLOW),
			new SettingsScreen.ColorSelection("Pink", Color.PINK),
			new SettingsScreen.ColorSelection("Blue", Color.BLUE),
			new SettingsScreen.ColorSelection("Purple", Color.PURPLE),
			new SettingsScreen.ColorSelection("White", Color.WHITE)
	};

	//CRAZY PUTTING
	public static PerspectiveCamera CAMERA;
	public static float BALL_RADIUS = 0.05f;
	public static GameInfo GAME_ASPECTS;

	//main menu
	public static Skin MenuSkin = new Skin(Gdx.files.internal("glassy-ui.json"));
}