package com.mt1006.ar_mod.ar.rendering;

import com.mt1006.ar_mod.ar.ArWindow;
import com.mt1006.ar_mod.ar.movement.ArMouse;
import com.mt1006.ar_mod.ar.movement.ArMovement;
import com.mt1006.ar_mod.config.ModConfig;
import com.mt1006.ar_mod.mixin.fields.AbstractTextureFields;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ArRenderer
{
	private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
	private static int oldW = -1, oldH = -1;
	private static int vao = 0;

	public static void init()
	{
		GLFW.glfwMakeContextCurrent(ArWindow.visibleWindow);
		GL.createCapabilities();
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		GL30.glEnable(GL30.GL_BLEND);

		ArShaders.init();

		float[] vaoPos = {
				-1.0f,-1.0f,0.0f,
				 1.0f,-1.0f,0.0f,
				-1.0f, 1.0f,0.0f,
				 1.0f, 1.0f,0.0f };
		float[] vaoUV = {
				0.0f,0.0f,
				1.0f,0.0f,
				0.0f,1.0f,
				1.0f,1.0f };

		vao = GL30.glGenVertexArrays();
		int[] vbo = new int[2];
		GL30.glGenBuffers(vbo);

		GL30.glBindVertexArray(vao);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[0]);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vaoPos, GL30.GL_STATIC_DRAW);
		GL30.glEnableVertexAttribArray(0);
		GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0);

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo[1]);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vaoUV, GL30.GL_STATIC_DRAW);
		GL30.glEnableVertexAttribArray(1);
		GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0);

		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}

	public static void render()
	{
		if (ArWindow.w != oldW || ArWindow.h != oldH)
		{
			GL11.glViewport(0, 0, ArWindow.w, ArWindow.h);
			oldW = ArWindow.w;
			oldH = ArWindow.h;
		}

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GL30.glBindVertexArray(vao);

		renderLevel();
		renderVignette();
		renderGui();

		GL11.glFlush();
		GLFW.glfwSwapBuffers(ArWindow.visibleWindow);
	}

	private static void renderLevel()
	{
		if (Minecraft.getInstance().level == null) { return; }
		ArFrame frame = ArWindow.getReadFrame();

		ArShaders.switchShader(ModConfig.getLevelShader());
		if (ArShaders.isReprojectionEnabled())
		{
			frame.setUniforms();
			ArMouse.setUniforms(frame);
			ModConfig.setUniforms();
		}

		frame.level.bindRead();
		if (ArShaders.isFullArEnabled())
		{
			ArShaders.setCameraVector(new Vector3f(ArMouse.forwardX, ArMouse.forwardY, ArMouse.forwardZ));
			ArShaders.setCameraPosition(ArMovement.getNewCameraPosition().sub(frame.shaderInput.playerPosition).add(ModConfig.getOffset()));
		}
		GL30.glDrawArrays(GL30.GL_TRIANGLE_STRIP, 0, 4);
	}

	private static void renderGui()
	{
		ArShaders.switchShader(ArShaders.frameShader);
		ArWindow.getReadFrame().gui.bindRead();
		GL30.glDrawArrays(GL30.GL_TRIANGLE_STRIP, 0, 4);
	}

	private static void renderVignette()
	{
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || !Minecraft.useFancyGraphics()) { return; }

		Entity entity = minecraft.getCameraEntity();
		if (entity == null) { return; }

		WorldBorder worldBorder = minecraft.level.getWorldBorder();
		double dist = worldBorder.getDistanceToBorder(entity);
		double d0 = Math.min(worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime() * 1000.0,
				Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
		double d1 = Math.max(worldBorder.getWarningBlocks(), d0);

		dist = dist < d1 ? 1.0 - (dist / d1) : 0.0;

		int texture = ((AbstractTextureFields)minecraft.getTextureManager().getTexture(VIGNETTE_LOCATION)).getIdValue();
		if (texture == -1) { return; }

		ArShaders.switchShader(ArShaders.vignetteShader);
		if (dist > 0.0F)
		{
			float color = (float)Mth.clamp(dist, 0.0, 1.0);
			ArShaders.setVignetteColor(0.0f, color, color);
		}
		else
		{
			float color = Mth.clamp(minecraft.gui.vignetteBrightness, 0.0F, 1.0f);
			ArShaders.setVignetteColor(color, color, color);
		}

		GL30.glBlendFuncSeparate(GL30.GL_ZERO, GL30.GL_ONE_MINUS_SRC_COLOR, GL30.GL_ONE, GL30.GL_ZERO);
		ArShaders.setColorTexture(texture);
		GL30.glDrawArrays(GL30.GL_TRIANGLE_STRIP, 0, 4);
		GL30.glBlendFuncSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ZERO);
	}
}
