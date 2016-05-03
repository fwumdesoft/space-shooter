package com.fwumdesoft.shoot;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.fwumdesoft.shoot.net.ServerInterface;

public class Main extends Game {
	
	public static Game game;
	public static Skin uiskin;
	public static AssetManager assets;
	
	@Override
	public void create() {
		Gdx.app.setLogLevel(Application.LOG_INFO);
		game = this;
		uiskin = new Skin(Gdx.files.internal("ui/uiskin.json"));
		assets = new AssetManager();
		assets.load("textures/player.png", Texture.class);
		assets.load("textures/bullet.png", Texture.class);
		assets.load("sounds/fire_bolt.mp3", Sound.class);
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
