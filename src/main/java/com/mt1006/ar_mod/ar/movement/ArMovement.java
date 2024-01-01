package com.mt1006.ar_mod.ar.movement;

import com.mt1006.ar_mod.ar.ArWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicLong;

public class ArMovement
{
	private static final double DOUBLE_PRESS_TIME = 0.4;
	private static volatile double cameraX, cameraY, cameraZ;
	private static final SynchronizedVector initialPosition = new SynchronizedVector();
	private static final SynchronizedVector velocity = new SynchronizedVector();
	private static double currentTimePos = 0;
	private static volatile double newTimePos = 0;
	private static int currentFrameID = 0;
	private static volatile int newFrameID = 0;
	private static boolean updatedWithProperFrame = false;
	private static double oldTimeDiff = 0.01;
	private static double lastForwardTime = -DOUBLE_PRESS_TIME;
	private static double lastJumpTime = -DOUBLE_PRESS_TIME;
	public static Vec3 newPos = Vec3.ZERO;
	private static Vec3 oldPos = Vec3.ZERO;
	public static volatile boolean sprintTriggered = false;
	public static volatile boolean flyingTriggered = false;
	public static AtomicLong frameCounter = new AtomicLong(0);
	private static final double CORRECTION = 0.2; //TODO: add to config

	public static void keyPress(long window, int key, int scancode, int action, int mods)
	{
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.execute(() -> minecraft.keyboardHandler.keyPress(window, key, scancode, action, mods));

		if (window == ArWindow.visibleWindow && minecraft.screen == null && action == 1)
		{
			if (minecraft.options.keyUp.matches(key, scancode))
			{
				double newForwardTime = getPreciseTime();
				if (newForwardTime - lastForwardTime < DOUBLE_PRESS_TIME)
				{
					sprintTriggered = true;
					lastForwardTime = -DOUBLE_PRESS_TIME;
				}
				else
				{
					lastForwardTime = newForwardTime;
				}
			}
			else if (minecraft.options.keyJump.matches(key, scancode))
			{
				double newJumpTime = getPreciseTime();
				if (newJumpTime - lastJumpTime < DOUBLE_PRESS_TIME)
				{
					flyingTriggered = true;
					lastJumpTime = -DOUBLE_PRESS_TIME;
				}
				else
				{
					lastJumpTime = newJumpTime;
				}
			}
		}
	}

	public static void updatePlayerPosition()
	{
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null) { return; }

		if (updatedWithProperFrame && isProperFrame())
		{
			double timeDiff = getPreciseTime() - newTimePos;
			double newCameraX = initialPosition.x + (velocity.x * timeDiff);
			double newCameraY = initialPosition.y + (velocity.y * timeDiff);
			double newCameraZ = initialPosition.z + (velocity.z * timeDiff);

			//double desiredVX = (newPos.x - newCameraX) / oldTimeDiff;
			//double desiredVY = (newPos.y - newCameraY) / oldTimeDiff;
			//double desiredVZ = (newPos.z - newCameraZ) / oldTimeDiff;
			double desiredVX = (((newPos.x - newCameraX) / oldTimeDiff) + velocity.x) / 2.0;
			double desiredVY = (((newPos.y - newCameraY) / oldTimeDiff) + velocity.y) / 2.0;
			double desiredVZ = (((newPos.z - newCameraZ) / oldTimeDiff) + velocity.z) / 2.0;

			//velocity.x = (newPos.x - newCameraX) / oldTimeDiff;
			//velocity.y = (newPos.y - newCameraY) / oldTimeDiff;
			//velocity.z = (newPos.z - newCameraZ) / oldTimeDiff;
			//velocity.x = (newPos.x - oldPos.x) / oldTimeDiff;
			//velocity.y = (newPos.y - oldPos.y) / oldTimeDiff;
			//velocity.z = (newPos.z - oldPos.z) / oldTimeDiff;
			velocity.x = ((newPos.x - oldPos.x) / oldTimeDiff) * (1.0 - CORRECTION) + desiredVX * CORRECTION;
			velocity.y = ((newPos.y - oldPos.y) / oldTimeDiff) * (1.0 - CORRECTION) + desiredVY * CORRECTION;
			velocity.z = ((newPos.z - oldPos.z) / oldTimeDiff) * (1.0 - CORRECTION) + desiredVZ * CORRECTION;

			//velocity.x = (((newPos.x - newCameraX) / oldTimeDiff) + velocity.x) / 2.0;
			//velocity.y = (((newPos.y - newCameraY) / oldTimeDiff) + velocity.y) / 2.0;
			//velocity.z = (((newPos.z - newCameraZ) / oldTimeDiff) + velocity.z) / 2.0;

			initialPosition.x = newCameraX;
			initialPosition.y = newCameraY;
			initialPosition.z = newCameraZ;

			double newTime = getPreciseTime();
			oldTimeDiff = newTime - newTimePos;
			newTimePos = newTime;
		}
		else
		{
			cameraX = initialPosition.x = newPos.x;
			cameraY = initialPosition.y = newPos.y;
			cameraZ = initialPosition.z = newPos.z;

			velocity.x = velocity.y = velocity.z = 0.0;
			newTimePos = getPreciseTime();
			updatedWithProperFrame = isProperFrame();
		}

		oldPos = newPos;
		newFrameID = ArWindow.currentFrameID;
	}

	private static boolean isProperFrame()
	{
		return frameCounter.get() > 1;
	}

	public static Vector3f getNewCameraPosition()
	{
		if (newFrameID != currentFrameID)
		{
			currentFrameID = newFrameID;
			currentTimePos = newTimePos;

			initialPosition.synchronize();
			velocity.synchronize();
		}

		double timeDiff = getPreciseTime() - currentTimePos;
		cameraX = initialPosition.vector.x + (velocity.vector.x * timeDiff);
		cameraY = initialPosition.vector.y + (velocity.vector.y * timeDiff);
		cameraZ = initialPosition.vector.z + (velocity.vector.z * timeDiff);

		return new Vector3f((float)cameraX, (float)cameraY, (float)cameraZ);
	}

	private static double getPreciseTime()
	{
		return (double)System.nanoTime() / 1.0E9;
	}

	private static class SynchronizedVector
	{
		public volatile double x, y, z;
		public final Vector3d vector = new Vector3d();

		public void synchronize()
		{
			vector.set(x, y, z);
		}
	}
}
