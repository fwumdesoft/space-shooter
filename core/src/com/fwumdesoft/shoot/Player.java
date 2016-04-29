package com.fwumdesoft.shoot;

import java.util.UUID;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Player extends Actor {
	public final UUID id;
	
	private TextureRegion texture;
	
	public Player(UUID id) {
		this.id = id;
		texture = new TextureRegion(Main.assets.get("sprites/player.png", Texture.class));
		setWidth(texture.getRegionWidth());
		setHeight(texture.getRegionHeight());
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}

	@Override
	public void act(float delta) {
		super.act(delta);
	}
}
