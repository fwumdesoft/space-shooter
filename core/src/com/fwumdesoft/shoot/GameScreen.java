package com.fwumdesoft.shoot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.fwumdesoft.shoot.net.ServerInterface;

public class GameScreen extends ScreenAdapter {
	private Stage stage;
	
	@Override
	public void show() {
		FillViewport viewport = new FillViewport(250f, 250f * (Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));
		stage = new Stage(viewport);
		Gdx.input.setInputProcessor(stage);
		Gdx.app.log("ID", ServerInterface.getMyID().toString());
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
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
}
