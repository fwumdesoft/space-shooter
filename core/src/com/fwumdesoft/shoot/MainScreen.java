package com.fwumdesoft.shoot;

import static com.fwumdesoft.shoot.net.NetConstants.*;

import java.nio.ByteBuffer;

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
				//Connect client to the server
				if(!ServerInterface.isConnected()) { //ensure preconditions are followed
					ServerInterface.connect();
					if(ServerInterface.isConnected()) { //ensure preconditions are followed
						ByteBuffer buffer = ServerInterface.receiveData();
						if(buffer == null) {
							displayNetError("Failed to connect to game server");
							ServerInterface.disconnect();
							return;
						}
						
						if(buffer.get(MSG_ID_OFFSET) == MSG_CONNECT_HANDSHAKE)
							Main.game.setScreen(new GameScreen());
						else {
							displayNetError("No response from server");
							ServerInterface.disconnect();
							return;
						}
					}
				} else {
					displayNetError("Client already connected. Try again");
					ServerInterface.disconnect();
				}
			}
		});
		
		root.add(btnJoin).width(100f).height(30f);
	}
	
	private void displayNetError(String desc) {
		new Dialog("Network Error", Main.uiskin)
		.text(desc)
		.button("Ok")
		.key(Keys.ESCAPE, null).key(Keys.ENTER, null)
		.show(stage);
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
