package com.fwumdesoft.shoot.model;

import java.util.UUID;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Represents a bolt on the client and on the server. Only
 * gets drawn for the client.
 */
public class Bolt extends NetActor implements Poolable {
	public static final float SPEED = 15f;
	
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
	 * Sets this Bolt's shooterId.
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
	
	public float getSpeedCompX() {
		return SPEED * MathUtils.cosDeg(getRotation());
	}
	
	public float getSpeedCompY() {
		return SPEED * MathUtils.sinDeg(getRotation());
	}
	
	@Override
	public void reset() {
		setNetId(null);
		setShooterId(null);
		setRotation(0);
		setPosition(0, 0);
	}
}
