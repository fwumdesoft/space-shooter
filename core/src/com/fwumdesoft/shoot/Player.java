package com.fwumdesoft.shoot;

import java.util.UUID;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents a player in the game that can move around the world.
 * Players are controlled by each client and the client primarily has
 * authority over the position of the player.
 */
public class Player extends NetActor {
	private TextureRegion texture;
	
	public Player(final UUID id) {
		super(id);
		texture = new TextureRegion(Main.assets.get("textures/player.png", Texture.class));
		setWidth(texture.getRegionWidth());
		setHeight(texture.getRegionHeight());
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}
}
