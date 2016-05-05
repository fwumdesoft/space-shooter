package com.fwumdesoft.shoot.model;

import java.util.UUID;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.fwumdesoft.shoot.Main;

/**
 * Represents a bolt on the client and on the server. Only gets drawn for the client.
 * This class is designed be be used only in a {@link Pool}.
 */
public class Bolt extends NetActor implements Poolable {
	private static TextureRegion texture;
	
	private float speed;
	private UUID shooterId;
	
	static {
		if(Gdx.app.getType() != ApplicationType.HeadlessDesktop) {
			texture = new TextureRegion(Main.assets.get("textures/bolt.png", Texture.class));
		}
	}
	
	/**
	 * Instantiates a blank Bolt Actor.
	 * <b>Should only be called by a pool when a new instance needs to be created.<b>
	 */
	public Bolt() {
		super(UUID.randomUUID());
		shooterId = null;
		setWidth(16);
		setHeight(8);
		setOrigin(Align.center);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}
	
	/**
	 * Sets this Bolt's shooterId or the Id of the {@link Player} that fired the bolt.
	 * @param id new Id.
	 * @return This Bolt for method chaining.
	 */
	public Bolt setShooterId(UUID id) {
		shooterId = id;
		return this;
	}
	
	public UUID getShooterId() {
		return shooterId;
	}
	
	@Override
	public Bolt setNetId(UUID newId) {
		super.setNetId(newId);
		return this;
	}
	
	public Bolt setSpeed(float newSpeed) {
		speed = newSpeed;
		return this;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public float getSpeedCompX() {
		return speed * MathUtils.cosDeg(getRotation());
	}
	
	public float getSpeedCompY() {
		return speed * MathUtils.sinDeg(getRotation());
	}
	
	@Override
	public void reset() {
		setNetId(UUID.randomUUID());
		setShooterId(null);
		setRotation(0);
		setPosition(0, 0);
		setSpeed(0);
	}
}
