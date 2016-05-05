package com.fwumdesoft.shoot.model;

import java.util.UUID;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Represents a bolt on the client and on the server. Only gets drawn for the client.
 * This class is designed be be used only in a {@link Pool}.
 */
public class Bolt extends NetActor implements Poolable {
	private float speed;
	private UUID shooterId;
	
	/**
	 * Instantiates a blank Bolt Actor.
	 * <b>Should only be called by a pool when a new instance needs to be created.<b>
	 */
	public Bolt() {
		super(UUID.randomUUID());
		shooterId = null;
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
	
	public Bolt setSpeed(float newSpeed) {
		speed = newSpeed;
		return this;
	}
	
	public float getSpeedCompX() {
		return speed * MathUtils.cosDeg(getRotation());
	}
	
	public float getSpeedCompY() {
		return speed * MathUtils.sinDeg(getRotation());
	}
	
	@Override
	public void reset() {
		setNetId(null);
		setShooterId(null);
		setRotation(0);
		setPosition(0, 0);
	}
}
