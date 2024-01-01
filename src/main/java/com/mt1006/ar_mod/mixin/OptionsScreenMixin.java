package com.mt1006.ar_mod.mixin;

import com.mt1006.ar_mod.ArMod;
import com.mt1006.ar_mod.config.gui.ConfigScreen;
import com.mt1006.ar_mod.mixin.fields.ScreenFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin
{
	@Unique private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(ArMod.MOD_ID, "textures/gui/button.png");

	@Inject(method = "init", at = @At(value = "TAIL"))
	private void addButton(CallbackInfo ci)
	{
		Screen screen = (Screen)(Object)this;
		Button button = new ImageButton(screen.width - 24, 4, 20, 20, 0, 0, 20, BUTTON_TEXTURE, 32, 64,
				(b) -> Minecraft.getInstance().setScreen(new ConfigScreen(screen)));

		((ScreenFields)this).getRenderables().add(button);
		((ScreenFields)this).getChildren().add(button);
		((ScreenFields)this).getNarratables().add(button);
	}
}
