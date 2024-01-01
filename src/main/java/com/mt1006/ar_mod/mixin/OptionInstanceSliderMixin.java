package com.mt1006.ar_mod.mixin;

import com.mt1006.ar_mod.mixin.fields.OptionInstanceFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {"net.minecraft.client.OptionInstance$OptionInstanceSliderButton"})
public class OptionInstanceSliderMixin
{
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	private static Object atInit(OptionInstance<?> instance)
	{
		return (instance == Minecraft.getInstance().options.fov()) ? ((OptionInstanceFields)(Object)instance).getValue() : instance.get();
	}

	@Redirect(method = "updateMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	private Object atUpdateMessage(OptionInstance<?> instance)
	{
		return (instance == Minecraft.getInstance().options.fov()) ? ((OptionInstanceFields)(Object)instance).getValue() : instance.get();
	}

	@Redirect(method = "applyValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	private Object atApplyValue(OptionInstance<?> instance)
	{
		return (instance == Minecraft.getInstance().options.fov()) ? ((OptionInstanceFields)(Object)instance).getValue() : instance.get();
	}
}
