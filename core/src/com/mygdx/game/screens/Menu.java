package com.mygdx.game.screens;


import com.badlogic.gdx.Game;
import com.mygdx.game.courses.CourseBuilder;
import com.mygdx.game.courses.GameInfo;


public class Menu extends Game {
	/*
		Default Game: just needs world generation and game loop
		Random Game : needs random world generation and game loop
		Custom Game: needs world generation based on parameters and game loop
		Skin: can be changed from Variables
		Other :Limit inputs in settings and limit number of players

		Classes
		Menu(this):manages changes of screens
			loadingScreen: intermediate screen to start the game
			MainMenuScreen: contains the information for the main menu screen
			PlayerScreen:Allows the addition of players into a game
				Player: stores all the players and their attributes(MUST be adjusted to accommodate
						different cameras and balls)
			GameSelectScreen: Allows the choice between game generation types
			SettingsScreen: contains the information for the settings screen as well as all the getters for
							game variables
				Gameinfo: stores all the information about a game( can be omited once better structure is agreed upon)


	 */
	//this class will manage switching between screens
	private MainMenuScreen mainMenu;
	private SettingsScreen settings;
	protected PlayerScreen players;
	private GameSelectScreen gameSelect;
	private GameScreen gameScreen;
	private ObstacleSelect obstacleScreen;
	private GameInfo gameInfo;
	private FractalSettings fractalSettings;
	private WarningScreen warningScreen;

	public static final int MAIN_MENU=0;
	public static final int DEFAULT_GAME=1;
	public static final int CUSTOM_GAME=2;
	public static final int RANDOM_GAME=3;
	public static final int PLAYER_SELECT=4;
	public static final int GAME_SELECT=5;
	public static final int PLAY =6;
	public static final int CUSTOMIZE_OBSTACLES=7;
	public static final int FRACTAL_SELECT=8;
	public static final int WARNING_SCREEN=9;




	public void changeScreen(int screen){
		System.out.print(">");
		switch (screen){
			case MAIN_MENU:
				System.out.println("Main Menu");
				//will have all the buttons for game Choices and exit
				mainMenu = new MainMenuScreen(this);
				this.setScreen(mainMenu);
				break;
			case DEFAULT_GAME:
				System.out.println("Default GAME");
				settings = new SettingsScreen(this);
				GameInfo g = settings.getGameInfo();
				SettingsScreen.cb=new CourseBuilder(g);
				changeScreen(PLAY);
				break;
			case CUSTOM_GAME:
				System.out.println("Custom GAME");
				settings = new SettingsScreen(this);
				this.setScreen(settings);
				break;
			case RANDOM_GAME:
				System.out.println("Random GAME");
				settings = SettingsScreen.randomGame(this);
				changeScreen(PLAY);
				break;
			case PLAYER_SELECT:
				System.out.println("Player Select");
				players =new PlayerScreen(this);
				this.setScreen(players);
				break;
			case GAME_SELECT:
				System.out.println("Game Select");
				gameSelect= new GameSelectScreen(this);
				this.setScreen(gameSelect);
				break;
			case PLAY:
				System.out.println("PLAY");
				gameInfo = settings.getGameInfo();
				System.out.println(gameInfo);
				gameScreen = new GameScreen(this, gameInfo);
				this.setScreen(gameScreen);
				break;
			case CUSTOMIZE_OBSTACLES:
				System.out.println("Customize obstacles");
				obstacleScreen= new ObstacleSelect(this);
				this.setScreen(obstacleScreen);
				break;
			case FRACTAL_SELECT:
				fractalSettings= new FractalSettings(this);
				this.setScreen(fractalSettings);
				break;
			case WARNING_SCREEN:
				warningScreen=new WarningScreen(this);
				this.setScreen(warningScreen);
				break;
		}
	}





	@Override
	public void create () {
		//start with the intermediate loading screen
		mainMenu = new MainMenuScreen(this);

		this.setScreen(mainMenu);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {

	}
}
