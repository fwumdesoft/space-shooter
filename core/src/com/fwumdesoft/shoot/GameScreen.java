package com.fwumdesoft.shoot;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.fwumdesoft.shoot.net.NetMessage;
import com.fwumdesoft.shoot.net.ServerInterface;

public class GameScreen extends ScreenAdapter {
	private int keysHeld = 0;
	
	private Stage stage;
	private Player me;
	
	private Thread netReceiveThread;
	
	@Override
	public void show() {
		//start net receive thread
		netReceiveThread = new Thread(() -> {
			while(!Thread.currentThread().isInterrupted()) {
				ByteBuffer msg = ServerInterface.receive(1);
				if(msg == null) continue;
				@SuppressWarnings("unused")
				final int dataLength = msg.getInt();
				final byte netmsg = msg.get();
				final UUID senderId = new UUID(msg.getLong(), msg.getLong());
				final ByteBuffer data = msg;
				
				switch(netmsg) {
				case NetMessage.CONNECT:
					Player newPlayer = new Player(senderId);
					stage.addActor(newPlayer);
					break;
				case NetMessage.UPDATE_PLAYER:
					float newX = data.getFloat(), newY = data.getFloat();
					for(Actor a : stage.getActors()) {
						if(senderId.equals(a.getUserObject())) {
							a.setPosition(newX, newY);
							break;
						}
					}
					break;
				case NetMessage.DISCONNECT:
					Actor removeActor = null;
					for(Actor a: stage.getActors()) {
						if(senderId.equals(a.getUserObject())) {
							removeActor = a;
							break;
						}
					}
					if(removeActor != null)
						removeActor.remove();
					break;
				}
				Thread.yield();
			}
		}, "net_receive");
		netReceiveThread.start();
		
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
		
		if(keysHeld > 0) {
			boolean moved = false;
			if(Gdx.input.isKeyPressed(Keys.W)) {
				me.addAction(Actions.moveBy(0, 1));
				moved = true;
			}
			if(Gdx.input.isKeyPressed(Keys.A)) {
				me.addAction(Actions.moveBy(-1, 0));
				moved = true;
			}
			if(Gdx.input.isKeyPressed(Keys.S)) {
				me.addAction(Actions.moveBy(0, -1));
				moved = true;
			}
			if(Gdx.input.isKeyPressed(Keys.D)) {
				me.addAction(Actions.moveBy(1, 0));
				moved = true;
			}
			if(moved) {
				ByteBuffer data = ByteBuffer.allocate(8);
				data.putFloat(me.getX());
				data.putFloat(me.getY());
				data.flip();
				ServerInterface.send(NetMessage.UPDATE_PLAYER, data);
			}
		}
		
		ServerInterface.send(NetMessage.HEARTBEAT, null);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		Gdx.app.log("GameScreen", "disposed");
		stage.dispose();
		try {
			netReceiveThread.interrupt();
			netReceiveThread.join();
		} catch(InterruptedException e) {
			netReceiveThread.interrupt();
		}
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
