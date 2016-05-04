package com.fwumdesoft.shoot;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;

/**
 * Manages input for the local client.
 */
public class InputManager extends InputListener {
	private RepeatAction moveForward, moveBackward, rotateClockwise, rotateCounterclockwise;
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
		case Keys.W: //move the player forward
			
			final MoveByAction moveByForward = Actions.moveBy(me.getSpeedCompX(), me.getSpeedCompY(), 0.01f, Interpolation.linear);
			Action runnableForward = Actions.run(() -> {
				moveByForward.setAmount(me.getSpeedCompX(), me.getSpeedCompY());
			});
			moveForward = Actions.forever(Actions.parallel(moveByForward, runnableForward));
			me.addAction(moveForward);
			return true;
			
		case Keys.A: //rotate the player counter clockwise
			
			rotateCounterclockwise = Actions.forever(Actions.rotateBy(Player.ROTATE_SPEED, 0.01f, Interpolation.linear));
			me.addAction(rotateCounterclockwise);
			return true;
			
		case Keys.S: //move the player backward
			
			final MoveByAction moveByBackward = Actions.moveBy(-me.getSpeedCompX()*0.25f, -me.getSpeedCompY()*0.25f, 0.01f, Interpolation.linear);
			Action runnableBackward = Actions.run(() -> {
				moveByBackward.setAmount(-me.getSpeedCompX()*0.25f, -me.getSpeedCompY()*0.25f);
			});
			moveBackward = Actions.forever(Actions.parallel(moveByBackward, runnableBackward));
			me.addAction(moveBackward);
			return true;
			
		case Keys.D: //rotate the player clockwise
			
			rotateClockwise = Actions.forever(Actions.rotateBy(-Player.ROTATE_SPEED, 0.01f, Interpolation.linear));
			me.addAction(rotateClockwise);
			return true;
			
		}
		return false;
	}

	@Override
	public boolean keyUp(InputEvent event, int keycode) {
		switch(keycode) {
		case Keys.W:
			moveForward.finish();
			me.addAction(Actions.moveBy(me.getSpeedCompX()*10, me.getSpeedCompY()*10, 1.5f, Interpolation.pow5Out));
			return true;
		case Keys.A:
			rotateCounterclockwise.finish();
			return true;
		case Keys.S:
			moveBackward.finish();
			me.addAction(Actions.moveBy(-me.getSpeedCompX()*2.5f, -me.getSpeedCompY()*2.5f, 1.5f, Interpolation.pow5Out));
			return true;
		case Keys.D:
			rotateClockwise.finish();
			return true;
		}
		return false;
	}
}
