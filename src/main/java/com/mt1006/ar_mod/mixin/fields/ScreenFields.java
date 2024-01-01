package com.mt1006.ar_mod.mixin.fields;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenFields
{
	@Accessor List<Renderable> getRenderables();
	@Accessor List<GuiEventListener> getChildren();
	@Accessor List<NarratableEntry> getNarratables();
}
