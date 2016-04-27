package com.fwumdesoft.shoot.desktop;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.fwumdesoft.shoot.Main;
import com.fwumdesoft.shoot.server.Server;

public class DesktopLauncher {
	public static void main(String[] arg) {
		if(arg.length > 0 && arg[0].equals("server")) {
			HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
			config.renderInterval = -1f;
			new HeadlessApplication(new Server(), config);
		} else {
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "Shooter";
			new LwjglApplication(new Main(), config);
		}
	}
}
