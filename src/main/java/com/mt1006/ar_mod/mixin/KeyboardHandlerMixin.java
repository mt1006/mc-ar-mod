package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mt1006.ar_mod.ar.movement.ArMovement;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin
{
	@Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;setupKeyboardCallbacks(JLorg/lwjgl/glfw/GLFWKeyCallbackI;Lorg/lwjgl/glfw/GLFWCharModsCallbackI;)V"))
	public void atSetup(long window, GLFWKeyCallbackI cb1, GLFWCharModsCallbackI cb2)
	{
		InputConstants.setupKeyboardCallbacks(window, ArMovement::keyPress, cb2);
	}
}
