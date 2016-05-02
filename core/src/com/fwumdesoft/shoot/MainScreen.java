package com.fwumdesoft.shoot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fwumdesoft.shoot.net.ServerInterface;

public class MainScreen extends ScreenAdapter {
	private Stage stage;
	
	@Override
	public void show() {
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		
		TextButton btnJoin = new TextButton("Join Server", Main.uiskin);
		btnJoin.setName("Join");
		btnJoin.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				//TODO properly check for connection
				if(!ServerInterface.isConnected()) {
					ServerInterface.connect();
					Main.game.setScreen(new GameScreen());
				} else {
					new Dialog("Network Error", Main.uiskin)
					.text("Failed to connect to the game server")
					.button("Ok")
					.key(Keys.ESCAPE, null).key(Keys.ENTER, null)
					.show(stage);
				}
			}
		});
		
		root.add(btnJoin).width(100f).height(30f);
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act();
		stage.draw();
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		stage.dispose();
	}
}
