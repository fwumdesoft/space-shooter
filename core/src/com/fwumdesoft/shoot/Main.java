package com.fwumdesoft.shoot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.fwumdesoft.shoot.net.ServerInterface;

public class Main extends Game {
	
	public static Game game;
	public static Skin uiskin;
	public static AssetManager assets;
	
	@Override
	public void create() {
		game = this;
		uiskin = new Skin(Gdx.files.internal("ui/uiskin.json"));
		assets = new AssetManager();
		assets.load("sprites/player.png", Texture.class);
		assets.load("sprites/bullet.png", Texture.class);
		assets.finishLoading();
		setScreen(new MainScreen());
	}
	
	@Override
	public void dispose() {
		super.dispose();
		uiskin.dispose();
		assets.dispose();
		if(ServerInterface.isConnected())
			ServerInterface.disconnect();
	}
}
