package com.fwumdesoft.shoot.model;

import java.util.UUID;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * An Actor that has a netId so it can be associated with an Object on the server.
 */
public class NetActor extends Actor {
	UUID netId;
	
	public NetActor() {}
	
	public NetActor(final UUID id) {
		netId = id;
	}
	
	public UUID getNetId() {
		return netId;
	}
	
	@Override
	public String toString() {
		return super.toString() + " {" + netId + "}";
	}
}
