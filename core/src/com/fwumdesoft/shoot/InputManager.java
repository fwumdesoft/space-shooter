package com.fwumdesoft.shoot;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;

/**
 * Manages input for the local client.
 */
public class InputManager extends InputListener {
	private RepeatAction moveUp, moveDown, moveLeft, moveRight;
	private final Player me;

	/**
	 * Instantiates an InputListener object for the local client.
	 * <p><b>Precondition:</b> {@code localPlayer.isLocalPlayer() == true}
	 * @param localPlayer The player that the local client controls.
	 */
	public InputManager(final Player localPlayer) {
		if(!localPlayer.isLocalPlayer()) throw new IllegalArgumentException("Must be the local player");
		me = localPlayer;
	}
	
	@Override
	public boolean keyDown(InputEvent event, int keycode) {
		switch(keycode) {
		case Keys.W:
			me.addAction(moveUp = Actions.forever(Actions.moveBy(0, Player.SPEED)));
			return true;
		case Keys.A:
			me.addAction(moveUp = Actions.forever(Actions.moveBy(-Player.SPEED, 0)));
			return true;
		case Keys.S:
			me.addAction(moveUp = Actions.forever(Actions.moveBy(0, -Player.SPEED)));
			return true;
		case Keys.D:
			me.addAction(moveUp = Actions.forever(Actions.moveBy(Player.SPEED, 0)));
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(InputEvent event, int keycode) {
		switch(keycode) {
		case Keys.W:
			moveUp.finish();
			return true;
		case Keys.A:
			moveLeft.finish();
			return true;
		case Keys.S:
			moveDown.finish();
			return true;
		case Keys.D:
			moveRight.finish();
			return true;
		}
		return false;
	}
}
