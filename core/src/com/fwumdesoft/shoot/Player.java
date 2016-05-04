package com.fwumdesoft.shoot;

import java.util.UUID;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.fwumdesoft.shoot.net.ServerInterface;

/**
 * Represents a player in the game that can move around the world.
 * Players are controlled by each client and the client primarily has
 * authority over the position of the player.
 */
public class Player extends NetActor {
	public static final float SPEED = 5;
	public static final float ROTATE_SPEED = 3f;
	
	private TextureRegion texture;
	private boolean isLocalPlayer;
	
	public Player(final UUID id) {
		super(id);
		texture = new TextureRegion(Main.assets.get("textures/player.png", Texture.class));
		setWidth(texture.getRegionWidth());
		setHeight(texture.getRegionHeight());
		setOrigin(Align.center);
		setScale(0.5f);
	}
	
	public Player(final UUID id, boolean isLocalPlayer) {
		this(id);
		this.isLocalPlayer = isLocalPlayer;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}
	
	@Override
	public void positionChanged() {
		if(isLocalPlayer()) {
			if(ServerInterface.isConnected()) {
				ServerInterface.updateLocalPlayer(this);
			}
			
			getStage().getCamera().position.set(getX() + getOriginX(), getY() + getOriginY(), 1);
		}
	}
	
	@Override
	public void rotationChanged() {
		if(isLocalPlayer()) {
			if(ServerInterface.isConnected()) {
				ServerInterface.updateLocalPlayer(this);
			}
		}
	}
	
	public float getSpeedCompX() {
		return Player.SPEED*MathUtils.cosDeg(getRotation());
	}
	
	public float getSpeedCompY() {
		return Player.SPEED*MathUtils.sinDeg(getRotation());
	}
	
	public boolean isLocalPlayer() {
		return isLocalPlayer;
	}
}
