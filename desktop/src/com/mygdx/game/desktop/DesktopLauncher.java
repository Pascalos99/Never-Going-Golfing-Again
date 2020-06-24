package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.screens.Menu;

import java.awt.*;
import java.util.Locale;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width= (int) screenSize.getWidth();
		config.height=(int) screenSize.getHeight();
		config.fullscreen = false;
		config.samples=3;

		Locale.setDefault(Locale.CANADA);

		new LwjglApplication(new Menu(), config);
	}
}
