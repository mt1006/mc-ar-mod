package com.mt1006.ar_mod.mixin.fields;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerFields
{
	@Accessor boolean getIgnoreFirstMove();
	@Accessor boolean getMouseGrabbed();
	@Invoker void callOnMove(long window, double x, double y);
}
