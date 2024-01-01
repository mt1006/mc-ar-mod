package com.mt1006.ar_mod.mixin.fields;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerFields
{
	@Accessor void setJumpTriggerTime(int value);
}
