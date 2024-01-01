package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mt1006.ar_mod.ar.ArThread;
import com.mt1006.ar_mod.ar.ArWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VirtualScreen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VirtualScreen.class)
public class VirtualScreenMixin
{
	@Shadow @Final private Minecraft minecraft;
	@Shadow @Final private ScreenManager screenManager;

	@Inject(method = "newWindow", at = @At(value = "HEAD"), cancellable = true)
	private void atNewWindow(DisplayData p_110873_, String p_110874_, String p_110875_, CallbackInfoReturnable<Window> cir)
	{
		cir.setReturnValue((Window)ArThread.executeAndGet(() -> new Window(minecraft, screenManager, p_110873_, p_110874_, p_110875_)));
		GLFW.glfwMakeContextCurrent(ArWindow.hiddenWindow);
		GL.createCapabilities();
		cir.cancel();
	}
}
