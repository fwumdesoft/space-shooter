package com.fwumdesoft.shoot;

import com.badlogic.gdx.Game;

public class Driver extends Game {
	@Override
	public void create() {
		setScreen(new MainScreen());
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
}
