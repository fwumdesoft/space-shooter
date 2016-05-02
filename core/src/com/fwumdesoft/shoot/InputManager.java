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
	 * @param localPlayer The player that the local client controls.
	 */
	public InputManager(Player localPlayer) {
		me = localPlayer;
	}
	
	@Override
	public boolean keyDown(InputEvent event, int keycode) {
		switch(keycode) {
		case Keys.W:
			me.addAction(moveUp = Actions.forever(Actions.moveBy(0, 1)));
			return true;
		case Keys.A:
			me.addAction(moveLeft = Actions.forever(Actions.moveBy(-1, 0)));
			return true;
		case Keys.S:
			me.addAction(moveDown = Actions.forever(Actions.moveBy(0, -1)));
			return true;
		case Keys.D:
			me.addAction(moveRight = Actions.forever(Actions.moveBy(1, 0)));
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
