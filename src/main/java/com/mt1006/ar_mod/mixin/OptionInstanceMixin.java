package com.mt1006.ar_mod.mixin;

import com.mt1006.ar_mod.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public class OptionInstanceMixin
{
	@Shadow Object value;

	@Inject(method = "get", at = @At(value = "HEAD"), cancellable = true)
	private void get(CallbackInfoReturnable<Integer> cir)
	{
		if (this == (Object)Minecraft.getInstance().options.fov())
		{
			cir.setReturnValue((int)((Integer)value * ModConfig.fovScaling.val));
			cir.cancel();
		}
	}
}
