package com.fwumdesoft.shoot.desktop;

import java.io.PrintWriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.fwumdesoft.shoot.Main;
import com.fwumdesoft.shoot.net.server.Server;

public class DesktopLauncher {
	public static void main(String[] arg) {
		if(arg.length > 0 && arg[0].equals("server")) {
			try {
				HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
				config.renderInterval = 1f; //this will keep the main loop alive
				new HeadlessApplication(new Server(), config);
			} catch(Throwable e) {
				if(Gdx.files != null) {
					FileHandle logFile = Gdx.files.local("log");
					e.printStackTrace(new PrintWriter(logFile.writer(true), true));
				}
			}
		} else {
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "Shooter";
			new LwjglApplication(new Main(), config);
		}
	}
}
