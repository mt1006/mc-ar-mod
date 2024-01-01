package com.mt1006.ar_mod.ar.rendering;

import com.mt1006.ar_mod.ar.ArWindow;
import com.mt1006.ar_mod.config.ModConfig;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class ArFrame
{
	private volatile int w, h, newW, newH;
	public volatile float rotX, rotY;
	public volatile boolean renderHand = false;
	public final SubFrame gui, level;
	public final ShaderInput shaderInput = new ShaderInput();

	public ArFrame(int w, int h)
	{
		this.w = w;
		this.h = h;
		this.newW = w;
		this.newH = h;
		gui = new SubFrame(this, false);
		level = new SubFrame(this, true);
	}

	public void resize(int w, int h)
	{
		newW = w;
		newH = h;
	}

	public void refreshSize()
	{
		if ((w != newW || h != newH) && newW != 0 && newH != 0)
		{
			w = newW;
			h = newH;
			gui.resize();
			level.resize();
		}
	}

	public void finishFrame()
	{
		gui.finishFrame();
		level.finishFrame();
	}

	public void setUniforms()
	{
		ArShaders.setScreenSize(w, h);
		shaderInput.setUniforms();
	}

	public SubFrame getStageSubFrame()
	{
		return ArWindow.renderingLevel ? level : gui;
	}

	public static class SubFrame
	{
		private final ArFrame parent;
		private final boolean isLevel;
		private final ArTexture colorTexture, depthTexture;
		//private final int colorRBO, depthRBO;
		private final FrameRenderTarget renderTarget;
		private volatile ArTexture.Format colorFormat;
		private volatile ByteBuffer colorPixels, depthPixels;
		private volatile boolean newPixels = false;

		public SubFrame(ArFrame parent, boolean isLevel)
		{
			this.parent = parent;
			this.isLevel = isLevel;

			colorTexture = new ArTexture();
			depthTexture = new ArTexture();

			renderTarget = new FrameRenderTarget(parent.w, parent.h);
			//colorRBO = GL30.glGenRenderbuffers();
			//depthRBO = GL30.glGenRenderbuffers();

			colorPixels = BufferUtils.createByteBuffer(parent.w * parent.h * 4);
			depthPixels = isLevel ? BufferUtils.createByteBuffer(parent.w * parent.h * 4) : null;

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, renderTarget.fbo);

			//GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, colorRBO);
			//GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, colorFormat, parent.w, parent.h); //TODO: check resizing
			//GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_RENDERBUFFER, colorRBO);

			//GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthRBO);
			//GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT, parent.w, parent.h);
			//GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthRBO);

			//GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		}

		public void bindWrite()
		{
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, renderTarget.fbo);
			GL30.glViewport(0, 0, parent.w, parent.h);

			if (GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			{
				GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
			}
		}

		public void bindRead()
		{
			if (newPixels)
			{
				newPixels = false;

				/*int pbo = renderTarget.asyncBuffer;

				if (pbo != 0)
				{
					colorTexture.setDataAsync(parent.w, parent.h, colorFormat, colorPixels, pbo);
					if (depthUsed) { depthTexture.setDataAsync(parent.w, parent.h, ArTexture.Format.DEPTH, depthPixels, pbo); }
				}
				else
				{
					colorTexture.setData(parent.w, parent.h, colorFormat, colorPixels);
					if (depthUsed) { depthTexture.setData(parent.w, parent.h, ArTexture.Format.DEPTH, depthPixels); }
				}*/

				colorTexture.setData(parent.w, parent.h, colorFormat, colorPixels);
				if (isDepthUsed()) { depthTexture.setData(parent.w, parent.h, ArTexture.Format.DEPTH, depthPixels); }
			}

			colorTexture.updateParameters();
			ArShaders.setColorTexture(colorTexture.id);
			if (isDepthUsed())
			{
				depthTexture.updateParameters();
				ArShaders.setDepthTexture(depthTexture.id);
			}
		}

		private void resize()
		{
			renderTarget.resize(parent.w, parent.h);
			colorPixels = BufferUtils.createByteBuffer(parent.w * parent.h * 4);
			depthPixels = isLevel ? BufferUtils.createByteBuffer(parent.w * parent.h * 4) : null;
		}

		private void finishFrame()
		{
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, renderTarget.fbo);

			colorFormat = (isLevel || Minecraft.getInstance().level == null) ? ArTexture.Format.RGB : ArTexture.Format.RGBA;
			colorPixels.clear();
			GL30.glReadPixels(0, 0, parent.w, parent.h, colorFormat.id, GL30.GL_UNSIGNED_BYTE, colorPixels);

			if (isDepthUsed())
			{
				depthPixels.clear();
				GL30.glReadPixels(0, 0, parent.w, parent.h, GL30.GL_DEPTH_COMPONENT, GL30.GL_FLOAT, depthPixels);
			}

			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
			newPixels = true;
		}

		private boolean isDepthUsed()
		{
			return isLevel && ModConfig.movementReprojection.val;
		}
	}

	public static class FrameRenderTarget
	{
		public int fbo;
		//public int asyncBuffer;
		private final ArTexture colorTexture, depthTexture;

		public FrameRenderTarget(int w, int h)
		{
			fbo = 0;
			//asyncBuffer = 0;
			colorTexture = new ArTexture();
			depthTexture = new ArTexture();
			resize(w, h);
		}

		public void resize(int w, int h)
		{
			destroyBuffers();
			createBuffers(w, h);
		}

		private void createBuffers(int w, int h)
		{
			fbo = GL30.glGenFramebuffers();

			colorTexture.setParameters(w, h, ArTexture.Format.RGBA);
			depthTexture.setParameters(w, h, ArTexture.Format.DEPTH);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, colorTexture.id, 0);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, depthTexture.id, 0);

			GL30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			GL30.glClearDepth(1.0);
			GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

			//TODO: refresh on setting change
			/*if (ModConfig.asyncTextures.val)
			{
				asyncBuffer = GL30.glGenBuffers();
				GL30.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, asyncBuffer);
				GL30.glBufferData(GL30.GL_PIXEL_UNPACK_BUFFER, (long)w * (long)h * 4L, GL30.GL_STREAM_DRAW);
				GL30.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);
			}*/
		}

		private void destroyBuffers()
		{
			/*if (asyncBuffer != 0)
			{
				GL30.glDeleteBuffers(asyncBuffer);
				asyncBuffer = 0;
			}*/

			if (fbo != 0)
			{
				GL30.glDeleteFramebuffers(fbo);
				fbo = 0;
			}
		}
	}

	public static class ShaderInput
	{
		private static final float[] TEMP_BUFFER = new float[16];
		public volatile float projectionMatrix11 = 0.0f;
		public volatile Matrix4f viewMatrix = new Matrix4f();
		public volatile Vector3f cameraPosition = new Vector3f();
		public volatile Vector3f playerPosition = new Vector3f();
		public volatile Vector3f cameraForward = new Vector3f();
		public volatile float farClip = 0.0f;

		public void setUniforms()
		{
			ArShaders.setProjectionMatrix11(projectionMatrix11);

			//TODO: check synchronization
			ArShaders.setViewMatrix(viewMatrix.get(TEMP_BUFFER));
			ArShaders.setFarClip(farClip);
		}
	}
}
