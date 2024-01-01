package com.mt1006.ar_mod.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin
{
	@Inject(method = "renderVignette", at = @At(value = "HEAD"), cancellable = true)
	private void cancelRenderVignette(GuiGraphics guiGraphics, Entity entity, CallbackInfo ci)
	{
		ci.cancel();
	}
}
