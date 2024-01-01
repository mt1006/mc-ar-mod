package com.mt1006.ar_mod.mixin.fields;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractTexture.class)
public interface AbstractTextureFields
{
	@Accessor(value = "id") int getIdValue();
}
