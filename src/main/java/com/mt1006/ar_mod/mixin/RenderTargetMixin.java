package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mt1006.ar_mod.ar.ArWindow;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public class RenderTargetMixin
{
	@Shadow public int viewWidth;
	@Shadow public int viewHeight;
	@Shadow public int frameBufferId;

	@Inject(method = "_bindWrite", at = @At(value = "HEAD"), cancellable = true)
	private void atBindWrite(boolean setViewport, CallbackInfo ci)
	{
		if (ArWindow.initialized && frameBufferId == Minecraft.getInstance().getMainRenderTarget().frameBufferId)
		{
			ArWindow.getWriteFrame().getStageSubFrame().bindWrite();
			if (setViewport) { GlStateManager._viewport(0, 0, viewWidth, viewHeight); }
			ci.cancel();
		}
	}
}
