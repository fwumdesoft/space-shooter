package com.fwumdesoft.shoot;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Represents an Actor on a network.
 */
public class NetActor extends Actor {
	public final byte id;
	
	public NetActor(byte id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		return str + " ID: " + id;
	}
}
