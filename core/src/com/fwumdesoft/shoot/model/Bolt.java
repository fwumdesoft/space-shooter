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
	
	public Bolt() {}
	
	public Bolt(final UUID netId, final UUID shooter) {
		super(netId);
		this.shooterId = shooter;
	}
	
	/**
	 * Sets this Bolt's netId.
	 * @param id new Id.
	 * @return This Bolt for method chaining.
	 */
	public Bolt setNetId(UUID id) {
		netId = id;
		return this;
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
		return SPEED*MathUtils.cosDeg(getRotation());
	}
	
	public float getSpeedCompY() {
		return SPEED*MathUtils.sinDeg(getRotation());
	}

	@Override
	public void reset() {
		netId = null;
		shooterId = null;
	}
}
