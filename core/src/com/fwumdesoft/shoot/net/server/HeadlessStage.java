package com.fwumdesoft.shoot.net.server;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * A version of the Stage class that can be used in Headless mode.
 */
class HeadlessStage extends Stage {
	public HeadlessStage() {
		setActionsRequestRendering(false);
	}
	
	@Override
	public void draw() {
		if(Gdx.app.getType() == ApplicationType.HeadlessDesktop) throw new IllegalStateException("Headless mode");
		super.draw();
	}
	
	@Override
	public void calculateScissors(Rectangle localRect, Rectangle scissorRect) {
		if(Gdx.app.getType() == ApplicationType.HeadlessDesktop) throw new IllegalStateException("Headless mode");
		super.calculateScissors(localRect, scissorRect);
	}
}
