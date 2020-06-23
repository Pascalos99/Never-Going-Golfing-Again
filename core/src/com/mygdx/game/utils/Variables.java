package com.mygdx.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.screens.BackgroundColor;
import com.mygdx.game.CrazyPutting;
import com.mygdx.game.physics.PhysicsSetting;
import com.mygdx.game.bots.*;
import com.mygdx.game.courses.GameInfo;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.parser.AtomFunction2d;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.screens.SettingsScreen;

import static com.mygdx.game.utils.ColorProof.*;
import static com.mygdx.game.physics.PhysicsSetting.*;

public final class Variables {

	// DEFAULT CONSTRUCTOR OF PUTTINGCOURSE SETTINGS
	public static final double 	DEFAULT_FRICTION = 0.131;
	public static final double	DEFAULT_HOLE_TOLERANCE = 0.3;
	public static final double 	DEFAULT_MAXIMUM_VELOCITY = 6;
	public static final double	DEFAULT_GRAVITY = 9.812;
	public static final double 	DEFAULT_SAND_FRICTION = 0.578;
	public static final double	DEFAULT_MASS = 50;

	// MAP GENERATION SETTINGS
	// World-border settings
	public static final Function2d OUT_OF_BOUNDS_FRICTION = Function2d.getConstant(100000);
	public static final	Function2d 	OUT_OF_BOUNDS_HEIGHT = new AtomFunction2d("-sin(y - x)/8 - cos(x + y)/8 - 2");
	public static final float BOUNDED_WORLD_SIZE = 20.0f;

	// Ball color
	public static final SettingsScreen.ColorSelection[] BALL_COLORS = {
			new SettingsScreen.ColorSelection("White", "White", WHITE),
			new SettingsScreen.ColorSelection("Pink", "Light Gray", PINK),
			new SettingsScreen.ColorSelection("Red", "Gray", RED),
			new SettingsScreen.ColorSelection("Purple", "Dark Blue", PURPLE),
			new SettingsScreen.ColorSelection("Blue", "Blue", BLUE),
			new SettingsScreen.ColorSelection("Cyan", "Light Blue", CYAN),
			new SettingsScreen.ColorSelection("Yellow", "Yellow", YELLOW),
			new SettingsScreen.ColorSelection("Black", "Black", BLACK)
	};

	// Default player names
	public static final String[] PLAYER_NAMES = {
			"Lightning", "Blitz", "DQBO", "Some Player", "Putting Crazy", "Lord Putter", "Harry Potter", "Pheonix"
		};

	//CRAZY PUTTING
	public static PerspectiveCamera CAMERA;
	public static GameInfo GAME_ASPECTS;
	public static final float WORLD_SCALING = (float)(1d/(2d*Math.PI/ 50d)); // ~7,9577471545947668
	public static final float BALL_RADIUS = 0.05f / WORLD_SCALING;
	public static PuttingCourse WORLD;
	public static final float FLAGPOLE_HEIGHT = 7;
	public static float GRAPHICS_SCALING = (float)(20/(2*Math.PI));
	public static Vector2d WORLD_SHIFT;

	//skins
	public static Skin MENU_SKIN = new Skin(Gdx.files.internal("orange/uiskin.json"));
	public static final Skin ORANGE = new Skin(Gdx.files.internal("orange/uiskin.json"));
	public static final Skin GLASSY = new Skin(Gdx.files.internal("glassy/glassy-ui.json"));
	public static final Skin QUANTUM_SKIN =new Skin(Gdx.files.internal("quantum/quantum-horizon-ui.json"));
	public static final Skin DEFAULT_SKIN = ORANGE;
	public static final Skin COLOR_BLIND_SKIN = QUANTUM_SKIN;

	public static final BackgroundColor TABLE_BKG=new BackgroundColor("highlight.png");
	public static final BackgroundColor BLANK_BKG= new BackgroundColor("highlight.png");
	public static final BackgroundColor EXTRA_BKG= new BackgroundColor("highlight.png");
	public static final BackgroundColor MENU_BKG= new BackgroundColor("MenuBKG.png");

	//PHYSICS
	public static final double GRADIENT_CUTTOFF = 1d; // no clue what unit this is
	public static double MAX_SHOT_VELOCITY = 6d;
	public static double SHOT_VELOCITY = MAX_SHOT_VELOCITY/2d;
	public static double SHOT_VELOCITY_INCREASE() { return 0.01 * MAX_SHOT_VELOCITY; }

	public static double PITCH = Math.PI*2/9d;
	public static double YAW = Math.PI*0.75;
	public static double VIEW_ZOOM = 7;

	public static void reset() {
		MAX_SHOT_VELOCITY = 6d;
		SHOT_VELOCITY = MAX_SHOT_VELOCITY/2d;
		PITCH = Math.PI * 2/9d;
		YAW = Math.PI * 0.75;
		VIEW_ZOOM = 7;
		CURRENT_PHYSICS_SETTING = Verlet;
		ALLOW_FLIGHT = false;
		CAST_SHADOWS = false;
		GAME = null;
		CAMERA = null;
		GAME_ASPECTS = null;
		WORLD = null;
		WORLD_SHIFT = null;
	}

	public static PhysicsSetting CURRENT_PHYSICS_SETTING = Verlet;

	// AI
	/** @return the current shot angle in radians on the range [-pi, pi] where an angle of 0 rad is when the ball is being shot in the
	 * 		x-axis only. Rotation goes up with clockwise rotation on the map (this is equal to counter-clockwise in a cartesian coordinate system)
	 * 		and switches abruptly from pi to -pi at the negative x-axis direction. */
	public static double getShotAngle() {
		Vector2d dir = new Vector2d(CAMERA.direction.x, CAMERA.direction.z).normalize();
		return dir.angle();
	}
	public static final double AI_SHOT_ANGLE_BOUND = 0.03;
	public static final AI_controller[] AVAILABLE_BOTS = {
			new AI_TopHat(), new AI_Fedora(), new AI_Gaussian(), new AI_Sherlock(), new AI_Basic()
	};

	public static final int TURN_STATE_START = 0;
	public static final int TURN_STATE_WAIT = 1;
	public static final int TURN_STATE_END = 2;

	public static CrazyPutting GAME = null;
	public static boolean ALLOW_FLIGHT = true;
	public static boolean CAST_SHADOWS = false;

	public static final double DELTA = 0.01d;

	public static final double FRICTION_SCALE = 5d;

	public static final double WALL_HEIGHT = TopDownPhysicsObject.toWorldScale(5d/WORLD_SCALING);//GRAPHICS SCALE
	public static final double WALL_BASE = TopDownPhysicsObject.toWorldScale(-50d/WORLD_SCALING);//GRAPHICS SCALE

	// The rest is all Deprecated
	// can't remove yet, because some of the code using these may be useful later
	@Deprecated
	public static final int 	NUM_FLAG_POS_TRIES = 2000;@Deprecated
	public static final double 	START_PLATFORM_RADIUS = 3;@Deprecated
	public static final double 	FLAG_PLATFORM_RADIUS = 3;@Deprecated
	public static final double 	PATH_RADIUS = 1.5;@Deprecated
	public static final double 	MIN_FLAG_DISTANCE = 0.5;@Deprecated // percentage of diagonal length of map
	public static final double	PATH_BUMPINESS = 0.2;@Deprecated // a value of 0 is completely smooth, anything above 1 is incredibly noisy (affects height)
	public static final double	PATH_ROUGHNESS = 0.4; @Deprecated
	public static final double 	MINIMUM_HEIGHT = -1;@Deprecated
	public static final double 	MAXIMUM_HEIGHT = 3;@Deprecated
	public static final double 	MINIMUM_FRICTION = 0.065;@Deprecated
	public static final double 	MAXIMUM_FRICTION = 0.196;@Deprecated
	public static final double	MINIMUM_START_HEIGHT = 0.5;@Deprecated // inclusive
	public static final double	MAXIMUM_START_HEIGHT = 1.5;@Deprecated // exclusive
	public static final double	MINIMUM_FLAG_HEIGHT = 0.5;@Deprecated // inclusive
	public static final double	MAXIMUM_FLAG_HEIGHT = 1.5;@Deprecated // exclusive
	public static final double	MINIMUM_START_FRICTION = 0.0933;@Deprecated // inclusive
	public static final double	MAXIMUM_START_FRICTION = 0.169;@Deprecated // exclusive
	public static final double	MINIMUM_FLAG_FRICTION = 0.0933;@Deprecated // inclusive
	public static final double	MAXIMUM_FLAG_FRICTION = 0.169; // exclusive
}
