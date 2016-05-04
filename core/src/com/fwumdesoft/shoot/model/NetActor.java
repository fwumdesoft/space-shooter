package com.fwumdesoft.shoot.model;

import java.util.UUID;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * An Actor that has a netId so it can be associated with an Object on the server.
 */
public abstract class NetActor extends Actor implements Poolable {
	private UUID netId;
	
	/**
	 * Instantiates a blank NetActor. Should only be called by when obtaining
	 * a NetActor from a pool and should immediately have its netId set.
	 */
	public NetActor() {}
	
	/**
	 * Instantiates a NetActor with a netId. All NetActors should
	 * have their netId set.
	 * @param id netId of this NetActor.
	 */
	public NetActor(UUID id) {
		netId = id;
	}
	
	public UUID getNetId() {
		return netId;
	}
	
	/**
	 * Changes the netId of this NetActor.
	 * @param newId The new netId.
	 * @return This NetActor for method chaining.
	 */
	public NetActor setNetId(UUID newId) {
		netId = newId;
		return this;
	}
	
	@Override
	public String toString() {
		return super.toString() + " {" + netId + "}";
	}
}
