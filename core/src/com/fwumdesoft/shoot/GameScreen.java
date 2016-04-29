package com.fwumdesoft.shoot;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.fwumdesoft.shoot.net.ServerInterface;

public class GameScreen extends ScreenAdapter {
	private boolean keyPressed;
	
	private Stage stage;
	private Player me;
	
	@Override
	public void show() {
		FillViewport viewport = new FillViewport(250f, 250f * ((float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));
		stage = new Stage(viewport);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, new UserInput()));
		Gdx.app.log("ID", ServerInterface.getMyID().toString());
		
		me = new Player(ServerInterface.getMyID());
		stage.addActor(me);
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
		
		if(keyPressed && Gdx.input.isKeyPressed(Keys.W)) {
			
		}
		if(keyPressed && Gdx.input.isKeyPressed(Keys.A)) {
			
		}
		if(keyPressed && Gdx.input.isKeyPressed(Keys.S)) {
			
		}
		if(keyPressed && Gdx.input.isKeyPressed(Keys.D)) {
			
		}
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		stage.dispose();
		ServerInterface.disconnect();
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
			keyPressed = true;
			return true;
		}
		
		@Override
		public boolean keyUp(int keycode) {
			keyPressed = false;
			return true;
		}
	}
}
