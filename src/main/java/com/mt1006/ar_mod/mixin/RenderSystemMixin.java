package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mt1006.ar_mod.ArMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemMixin
{
	@Redirect(method = "limitDisplayFPS", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWaitEventsTimeout(D)V", remap = false))
	private static void atLimitDisplayFPS(double timeout)
	{
		try { Thread.sleep((long)Math.floor(timeout * 1000.0)); }
		catch (InterruptedException exception) { ArMod.LOGGER.warn("Thread.sleep() interruption! - RenderSystemMixin"); }
	}
}
