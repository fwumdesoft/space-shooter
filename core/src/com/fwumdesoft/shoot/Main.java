package com.fwumdesoft.shoot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.fwumdesoft.shoot.net.ServerInterface;

public class Main extends Game {
	/**
	 * self contained static reference
	 */
	public static Game game;
	public static Skin uiskin;
	
	@Override
	public void create() {
		game = this;
		uiskin = new Skin(Gdx.files.internal("ui/uiskin.json"));
		setScreen(new MainScreen());
	}
	
	@Override
	public void dispose() {
		super.dispose();
		uiskin.dispose();
		ServerInterface.disconnect();
	}
}
