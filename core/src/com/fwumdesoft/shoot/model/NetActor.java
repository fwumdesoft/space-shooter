package com.fwumdesoft.shoot.model;

import java.util.UUID;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * An Actor that has a netId so it can be associated with an Object on the server.
 * This class also features some slight improvements on the base Actor class.
 */
public class NetActor extends Actor {
	private float lastX, lastY, lastRot;
	
	private UUID netId;
	
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
	
	@Override
	protected void positionChanged() {
		positionChanged(getX() - lastX, getY() - lastY);
		lastX = getX();
		lastY = getY();
	}
	
	@Override
	protected void rotationChanged() {
		rotationChanged(getRotation() - lastRot);
		lastRot = getRotation();
	}
	
	/** 
	 * Called when this NetActor's position is changed.
	 * @param deltaX Change in x position
	 * @param deltaY Change in y position
	 */
	protected void positionChanged(float deltaX, float deltaY) {}
	
	/**
	 * Called when this NetActor's rotation is changed.
	 * @param deltaRot Change in rotation
	 */
	protected void rotationChanged(float deltaRot) {}
	
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
