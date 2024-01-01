package com.mt1006.ar_mod.ar;

import com.mt1006.ar_mod.ArMod;
import com.mt1006.ar_mod.ar.movement.ArMouse;
import com.mt1006.ar_mod.ar.movement.ArMovement;
import com.mt1006.ar_mod.ar.movement.ArPlayerBob;
import com.mt1006.ar_mod.ar.rendering.ArRenderer;
import com.mt1006.ar_mod.config.ModConfig;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ArThread
{
	public static Thread mainThread;
	public static @Nullable Thread gameThread = null;
	private static EventLoop initEventLoop;
	private static final AtomicBoolean initFinished = new AtomicBoolean(false);
	private static volatile Object tempReference = null;
	private static double lastDrawTime = 0.0;
	private static int oldInterval = -1;
	private static int fps = 0;
	private static long maxFrameTime = -1, second = -1, oldNanoTime = -1;

	public static void init(Thread newThread)
	{
		mainThread = Thread.currentThread();
		gameThread = newThread;
		initEventLoop = new EventLoop(mainThread, "ArMod main thread");
	}

	public static void run()
	{
		while (!initFinished.get())
		{
			initEventLoop.runAllTasks();
			checkGameThread();
		}

		ArRenderer.init();
		while (true)
		{
			if (ModConfig.isFPSLimitEnabled()) { limitFPS(ModConfig.maxReprojectedFPS.val); }
			updateVSync(ModConfig.reprojectionVSync.val);

			GLFW.glfwPollEvents();
			ArPlayerBob.updateTimer();
			ArRenderer.render();
			initEventLoop.runAllTasks();

			if (ModConfig.printAsyncFPS.val) { asyncFPS(); }
			checkGameThread();
		}
	}

	private static void limitFPS(int limit)
	{
		double expectedTime = lastDrawTime + 1.0 / (double)limit;

		double currentTime = GLFW.glfwGetTime();
		while (currentTime < expectedTime)
		{
			GLFW.glfwWaitEventsTimeout(expectedTime - currentTime);
			currentTime = GLFW.glfwGetTime();
		}

		lastDrawTime = currentTime;
	}

	private static void updateVSync(boolean val)
	{
		int interval = val ? 1 : 0;
		if (interval != oldInterval)
		{
			GLFW.glfwSwapInterval(interval);
			oldInterval = interval;
		}
	}

	private static void asyncFPS()
	{
		long nanoTime = System.nanoTime();
		long currentSecond = nanoTime / 1000000000L;

		long frameTime = nanoTime - oldNanoTime;
		if (frameTime > maxFrameTime) { maxFrameTime = frameTime; }
		oldNanoTime = nanoTime;

		if (currentSecond != second)
		{
			double msFrameTime = (double)maxFrameTime / 1000000.0;
			if (second != -1)
			{
				ArMod.LOGGER.info(String.format("Async FPS: %d (Worst: %.1f ms = %d FPS)", fps, msFrameTime, (int)(1000.0 / msFrameTime)));
			}

			fps = 0;
			maxFrameTime = -1;
			second = currentSecond;
		}
		fps++;
	}

	private static void checkGameThread()
	{
		if (gameThread != null && !gameThread.isAlive()) { System.exit(-1); }
	}

	public static void finishInit()
	{
		initFinished.set(true);
	}

	public static void nextFrame()
	{
		if (ModConfig.simulateRealDelay.val) { setNextFrame(); }
		ArMouse.updateMouseRenderThread();
		ArMovement.updatePlayerPosition();
	}

	public static void finishFrame()
	{
		ArWindow.getWriteFrame().finishFrame();
		if (!ModConfig.simulateRealDelay.val) { setNextFrame(); }
	}

	private static void setNextFrame()
	{
		ArWindow.currentFrameID++;
		ArWindow.renderingLevel = false;
		ArWindow.getWriteFrame().refreshSize();
	}

	public static void execute(Runnable runnable)
	{
		initEventLoop.executeBlocking(runnable);
	}

	public static void executeAsync(Runnable runnable)
	{
		initEventLoop.execute(runnable);
	}

	public static Object executeAndGet(Supplier<Object> runnable)
	{
		initEventLoop.executeBlocking(() -> tempReference = runnable.get());
		return tempReference;
	}

	public static boolean isMainThread()
	{
		return Thread.currentThread() == mainThread;
	}

	public static class EventLoop extends ReentrantBlockableEventLoop<Runnable>
	{
		private final Thread thread;

		public EventLoop(Thread thread, String name)
		{
			super(name);
			this.thread = thread;
		}

		@Override public void runAllTasks()
		{
			super.runAllTasks();
		}

		@Override protected @NotNull Runnable wrapRunnable(@NotNull Runnable runnable)
		{
			return runnable;
		}

		@Override protected boolean shouldRun(@NotNull Runnable runnable)
		{
			return true;
		}

		@Override protected @NotNull Thread getRunningThread()
		{
			return thread;
		}
	}
}
