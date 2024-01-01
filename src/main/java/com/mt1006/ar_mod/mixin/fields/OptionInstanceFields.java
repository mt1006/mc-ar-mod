package com.mt1006.ar_mod.mixin.fields;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.class)
public interface OptionInstanceFields
{
	@Accessor Object getValue();
}
