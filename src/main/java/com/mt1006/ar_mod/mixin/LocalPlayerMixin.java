package com.mt1006.ar_mod.mixin;

import com.mt1006.ar_mod.ar.movement.ArMovement;
import com.mt1006.ar_mod.config.ModConfig;
import com.mt1006.ar_mod.mixin.fields.PlayerFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin
{
	@Shadow @Final protected Minecraft minecraft;

	@Inject(method = "aiStep", at = @At(value = "RETURN"))
	private void atAiStepReturn(CallbackInfo ci)
	{
		LocalPlayer player = (LocalPlayer)(Object)this;
		boolean responsiveFlying = ModConfig.responsiveFlying.val;

		if (ArMovement.sprintTriggered)
		{
			if (ModConfig.responsiveSprinting.val)
			{
				if (!player.isSprinting()) { player.setSprinting(true); }
			}
			ArMovement.sprintTriggered = false;
		}

		if (ArMovement.flyingTriggered)
		{
			if (responsiveFlying && player.getAbilities().mayfly && minecraft.gameMode != null
					&& !minecraft.gameMode.isAlwaysFlying() && !player.isSwimming())
			{
				player.getAbilities().flying = !player.getAbilities().flying;
				player.onUpdateAbilities();
			}
			ArMovement.flyingTriggered = false;
		}

		if (responsiveFlying) { ((PlayerFields)player).setJumpTriggerTime(0); }
	}
}
