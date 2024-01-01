package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mt1006.ar_mod.ar.ArThread;
import com.mt1006.ar_mod.ar.ArWindow;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowMixin
{
	@Shadow private int width;
	@Shadow private int height;
	@Mutable @Shadow @Final private long window;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V", remap = false))
	private void atConstructor(WindowEventHandler windowEventHandler, ScreenManager screenManager, DisplayData displayData, String string, String string2, CallbackInfo ci)
	{
		window = ArWindow.init(window, width, height);
	}

	@Inject(method = "updateDisplay", at = @At(value = "HEAD"), cancellable = true)
	public void atUpdateDisplay(CallbackInfo ci)
	{
		if (ArWindow.initialized)
		{
			ArThread.finishFrame();

			//RenderSystem.flipFrame without polling events
			RenderSystem.replayQueue();
			Tesselator.getInstance().getBuilder().clear();
			GLFW.glfwSwapBuffers(ArWindow.hiddenWindow);

			ci.cancel();
		}
	}

	@Redirect(method = "setIcon", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowIcon(JLorg/lwjgl/glfw/GLFWImage$Buffer;)V", remap = false))
	private void atSetIcon(long window, GLFWImage.Buffer images)
	{
		ArThread.execute(() -> GLFW.glfwSetWindowIcon(window, images));
	}

	@Inject(method = "onResize", at = @At(value = "HEAD"))
	private void atOnResize(long window, int w, int h, CallbackInfo ci)
	{
		if (ArWindow.initialized) { ArWindow.resize(w, h); }
	}

	@Redirect(method = "setMode", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V", remap = false))
	private void atSetMode(long window, long monitor, int xpos, int ypos, int width, int height, int refreshRate)
	{
		ArThread.executeAsync(() -> GLFW.glfwSetWindowMonitor(window, monitor, xpos, ypos, width, height, refreshRate));
	}

	@Redirect(method = "setTitle", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowTitle(JLjava/lang/CharSequence;)V", remap = false))
	private void atSetTitle(long window, CharSequence title)
	{
		ArThread.executeAsync(() -> GLFW.glfwSetWindowTitle(window, title));
	}
}
