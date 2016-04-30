package com.fwumdesoft.shoot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class GameScreen extends ScreenAdapter {
	private int keysHeld = 0;
	
	private Stage stage;
	private Player me;
	
	@Override
	public void show() {
		FillViewport viewport = new FillViewport(250f, 250f * ((float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));
		stage = new Stage(viewport);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, new UserInput()));		
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
		
		if(keysHeld > 0) {
			if(Gdx.input.isKeyPressed(Keys.W)) {
				me.addAction(Actions.moveBy(0, 1));
			}
			if(Gdx.input.isKeyPressed(Keys.A)) {
				me.addAction(Actions.moveBy(-1, 0));
			}
			if(Gdx.input.isKeyPressed(Keys.S)) {
				me.addAction(Actions.moveBy(0, -1));
			}
			if(Gdx.input.isKeyPressed(Keys.D)) {
				me.addAction(Actions.moveBy(1, 0));
			}
		}
		
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		Gdx.app.log("GameScreen", "disposed");
		stage.dispose();
	}

	private class UserInput extends InputAdapter {
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
//			float rotationAmount = 0.0f;
//			Vector2 stageCoords = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
			return false;
		}

		@Override
		public boolean keyDown(int keycode) {
			keysHeld++;
			return true;
		}
		
		@Override
		public boolean keyUp(int keycode) {
			keysHeld--;
			return true;
		}
	}
}
