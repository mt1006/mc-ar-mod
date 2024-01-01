package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mt1006.ar_mod.ar.ArThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputConstants.class)
public class InputConstantsMixin
{
	@Inject(method = "grabOrReleaseMouse", at = @At(value = "HEAD"), cancellable = true)
	private static void grabOrReleaseMouse(long window, int val, double x, double y, CallbackInfo ci)
	{
		if (!ArThread.isMainThread())
		{
			ArThread.executeAsync(() -> InputConstants.grabOrReleaseMouse(window, val, x, y));
			ci.cancel();
		}
	}
}
