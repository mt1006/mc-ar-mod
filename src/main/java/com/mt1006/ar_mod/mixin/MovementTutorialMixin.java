package com.mt1006.ar_mod.mixin;

import net.minecraft.client.player.Input;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementTutorialStepInstance.class)
public class MovementTutorialMixin
{
	@Shadow private boolean moved;
	@Shadow private boolean turned;

	@Inject(method = "onInput", at = @At(value = "HEAD"))
	public void atOnInput(Input input, CallbackInfo ci)
	{
		moved = true;
	}

	@Inject(method = "onMouse", at = @At(value = "HEAD"))
	public void atOnMouse(double d1, double d2, CallbackInfo ci)
	{
		turned = true;
	}
}
