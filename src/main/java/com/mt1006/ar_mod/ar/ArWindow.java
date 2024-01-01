package com.mt1006.ar_mod.ar;

import com.mt1006.ar_mod.ArMod;
import com.mt1006.ar_mod.ar.rendering.ArFrame;
import com.mt1006.ar_mod.config.ModConfig;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

public class ArWindow
{
	public static boolean initialized = false;
	public static long visibleWindow = 0;
	public static volatile long hiddenWindow = 0;

	private static final ArFrame[] frames = new ArFrame[3];
	private static volatile int writeFramePos = 0, readFramePos = 0;
	public static boolean renderingLevel = false;
	public static int currentFrameID = 0, lastFrameID = 0;
	public static int w = -1, h = -1;

	public static long init(long oldWindow, int w, int h)
	{
		if (initialized) { return visibleWindow; }
		ArWindow.w = w;
		ArWindow.h = h;

		visibleWindow = oldWindow;

		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		hiddenWindow = GLFW.glfwCreateWindow(640, 480, "MC-AR-MOD window", 0L, visibleWindow);

		GLFW.glfwMakeContextCurrent(oldWindow);
		GL.createCapabilities();
		GLFW.glfwMakeContextCurrent(hiddenWindow);
		for (int i = 0; i < 3; i++) { frames[i] = new ArFrame(w, h); }
		GLFW.glfwMakeContextCurrent(0L);

		initialized = true;
		return visibleWindow;
	}

	public static void resize(int w, int h)
	{
		for (int i = 0; i < 3; i++)
		{
			frames[i].resize(w, h);
		}
		ArWindow.w = w;
		ArWindow.h = h;
	}

	public static ArFrame getReadFrame()
	{
		return getNextFrame(false);
	}

	public static ArFrame getWriteFrame()
	{
		if (lastFrameID != currentFrameID)
		{
			lastFrameID = currentFrameID;
			return getNextFrame(true);
		}
		return frames[writeFramePos];
	}

	private static ArFrame getNextFrame(boolean forWrite)
	{
		int value = forWrite ? writeFramePos : readFramePos;
		int newValue = value < 2 ? value + 1 : 0;

		if (forWrite)
		{
			if (newValue == readFramePos)
			{
				if (ModConfig.warnAboutWaiting.val) { ArMod.LOGGER.warn("AR thread slower than render!"); }
				while (newValue == readFramePos) { Thread.yield(); }
			}

			writeFramePos = newValue;
			return frames[newValue];
		}
		else
		{
			if (newValue != writeFramePos)
			{
				readFramePos = newValue;
				return frames[newValue];
			}
			else
			{
				return frames[readFramePos];
			}
		}
	}
}