package com.fwumdesoft.shoot.net.server;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * A version of the Stage class that can be used in Headless mode.
 */
class HeadlessStage extends Stage {
	public HeadlessStage() {
		super(new HeadlessViewport(), new HeadlessBatch());
		setActionsRequestRendering(false);
	}
	
	@Override
	public void draw() {
		if(Gdx.app.getType() == ApplicationType.HeadlessDesktop) throw new IllegalStateException("Headless mode");
		super.draw();
	}
	
	@Override
	public void calculateScissors(Rectangle localRect, Rectangle scissorRect) {
		if(Gdx.app.getType() == ApplicationType.HeadlessDesktop) throw new IllegalStateException("Headless mode");
		super.calculateScissors(localRect, scissorRect);
	}
	
	/**
	 * Basically a mock viewport so a HeadlessStage can exist.
	 */
	private static class HeadlessViewport extends Viewport {
		@Override
		public void update(int screenWidth, int screenHeight, boolean centerCamera) {}
	}
	
	/**
	 * Basically a mock batch so a HeadlessStage can exist.
	 */
	private static class HeadlessBatch implements Batch {
		@Override
		public void dispose() {}

		@Override
		public void begin() {}

		@Override
		public void end() {}

		@Override
		public void setColor(Color tint) {}

		@Override
		public void setColor(float r, float g, float b, float a) {}

		@Override
		public void setColor(float color) {}

		@Override
		public Color getColor() {
			return null;
		}

		@Override
		public float getPackedColor() {
			return 0;
		}

		@Override
		public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY,
				float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {}

		@Override
		public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX,
				boolean flipY) {}

		@Override
		public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {}

		@Override
		public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {}

		@Override
		public void draw(Texture texture, float x, float y) {}

		@Override
		public void draw(Texture texture, float x, float y, float width, float height) {}

		@Override
		public void draw(Texture texture, float[] spriteVertices, int offset, int count) {}

		@Override
		public void draw(TextureRegion region, float x, float y) {}

		@Override
		public void draw(TextureRegion region, float x, float y, float width, float height) {}

		@Override
		public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY,
				float rotation) {}

		@Override
		public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY,
				float rotation, boolean clockwise) {}

		@Override
		public void draw(TextureRegion region, float width, float height, Affine2 transform) {}

		@Override
		public void flush() {}

		@Override
		public void disableBlending() {}

		@Override
		public void enableBlending() {}

		@Override
		public void setBlendFunction(int srcFunc, int dstFunc) {}

		@Override
		public int getBlendSrcFunc() {
			return 0;
		}

		@Override
		public int getBlendDstFunc() {
			return 0;
		}

		@Override
		public Matrix4 getProjectionMatrix() {
			return null;
		}

		@Override
		public Matrix4 getTransformMatrix() {
			return null;
		}

		@Override
		public void setProjectionMatrix(Matrix4 projection) {}

		@Override
		public void setTransformMatrix(Matrix4 transform) {}

		@Override
		public void setShader(ShaderProgram shader) {}

		@Override
		public ShaderProgram getShader() {
			return null;
		}

		@Override
		public boolean isBlendingEnabled() {
			return false;
		}

		@Override
		public boolean isDrawing() {
			return false;
		}
	}
}
