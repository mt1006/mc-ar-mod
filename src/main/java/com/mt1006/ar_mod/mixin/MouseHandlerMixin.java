package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mt1006.ar_mod.ar.movement.ArMouse;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
	@Shadow private double xpos;
	@Shadow private double ypos;

	@Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;setupMouseCallbacks(JLorg/lwjgl/glfw/GLFWCursorPosCallbackI;Lorg/lwjgl/glfw/GLFWMouseButtonCallbackI;Lorg/lwjgl/glfw/GLFWScrollCallbackI;Lorg/lwjgl/glfw/GLFWDropCallbackI;)V"))
	public void atSetup(long window, GLFWCursorPosCallbackI cb1, GLFWMouseButtonCallbackI cb2, GLFWScrollCallbackI cb3, GLFWDropCallbackI cb4)
	{
		InputConstants.setupMouseCallbacks(window, ArMouse::onMove, cb2, cb3, cb4);
	}

	@Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"), cancellable = true)
	private void atCameraMovement(long window, double x, double y, CallbackInfo ci)
	{
		this.xpos = x;
		this.ypos = y;
		ci.cancel();
	}
}
