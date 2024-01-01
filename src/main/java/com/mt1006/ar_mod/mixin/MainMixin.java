package com.mt1006.ar_mod.mixin;

import com.mt1006.ar_mod.ArMod;
import com.mt1006.ar_mod.ar.ArThread;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin
{
	@Inject(method = "main", at = @At(value = "HEAD"), cancellable = true, remap = false)
	private static void atMain(String[] args, CallbackInfo ci)
	{
		if (Thread.currentThread() == ArThread.gameThread) { return; }
		ci.cancel();

		if (ArThread.gameThread != null)
		{
			ArMod.LOGGER.error("\"main()\" called from other thread after creating game thread!");
			return;
		}

		ArThread.init(new Thread(() -> Main.main(args)));
		ArThread.gameThread.start();
		ArThread.run();
	}
}
