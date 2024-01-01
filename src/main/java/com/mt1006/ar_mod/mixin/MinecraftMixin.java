package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mt1006.ar_mod.ar.ArThread;
import com.mt1006.ar_mod.ar.ArWindow;
import com.mt1006.ar_mod.ar.movement.ArMovement;
import com.mt1006.ar_mod.config.ModConfig;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.repository.PackRepository;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin
{
	@Shadow @Final private PackRepository resourcePackRepository;
	@Shadow @Final private Window window;
	@Shadow @Nullable public ClientLevel level;
	@Shadow @Nullable public Screen screen;
	@Shadow @Nullable private Overlay overlay;

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"))
	private void makeCurrentContext(PackRepository packRepository)
	{
		GLFW.glfwMakeContextCurrent(ArWindow.hiddenWindow);
		resourcePackRepository.reload();
	}

	/*@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/loading/ClientModLoader;begin(Lnet/minecraft/client/Minecraft;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/packs/resources/ReloadableResourceManager;)V", remap = false))
	private void atClientModLoaderBegin(Minecraft minecraft, PackRepository defaultResourcePacks, ReloadableResourceManager mcResourceManager)
	{
		ArThread.execute(() -> ClientModLoader.begin(minecraft, defaultResourcePacks, mcResourceManager));
	}*/

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;setup(J)V"))
	private void atMouseHandlerSetup(MouseHandler mouseHandler, long window)
	{
		ArThread.execute(() -> mouseHandler.setup(window));
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;setup(J)V"))
	private void atKeyboardHandlerSetup(KeyboardHandler keyboardHandler, long window)
	{
		ArThread.execute(() -> keyboardHandler.setup(window));
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setErrorCallback(Lorg/lwjgl/glfw/GLFWErrorCallbackI;)V", remap = false))
	private void atSetErrorCallback(GLFWErrorCallbackI callback)
	{
		ArThread.execute(() -> RenderSystem.setErrorCallback(callback));
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateRawMouseInput(Z)V"))
	private void atUpdateRawMouseInput(Window window, boolean val)
	{
		ArThread.execute(() -> window.updateRawMouseInput(val));
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setDefaultErrorCallback()V"))
	private void atSetWindowErrorCallback(Window instance)
	{
		ArThread.execute(() -> window.setDefaultErrorCallback());
	}

	@Inject(method = "run", at = @At(value = "HEAD"))
	private void atRun(CallbackInfo ci)
	{
		ArThread.finishInit();
	}

	@Inject(method = "runTick", at = @At(value = "HEAD"))
	private void initFrame(boolean renderLevel, CallbackInfo ci)
	{
		ArThread.nextFrame();
	}

	@Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(II)V"))
	private void ignoreBlitToScreen(RenderTarget renderTarget, int i1, int i2) {}

	@Inject(method = "getFramerateLimit", at = @At(value = "HEAD"), cancellable = true)
	private void debugFPSLimit(CallbackInfoReturnable<Integer> cir)
	{
		int actualLimit = (level != null || screen == null && overlay == null) ? this.window.getFramerateLimit() : 60;
		cir.setReturnValue(ModConfig.getDebugFPSLimit(actualLimit));
		cir.cancel();
	}

	@Inject(method = "useShaderTransparency", at = @At(value = "HEAD"), cancellable = true)
	private static void cancelShaderTransparency(CallbackInfoReturnable<Boolean> cir)
	{
		cir.setReturnValue(false);
		cir.cancel();
	}

	@Inject(method = "setLevel", at = @At(value = "HEAD"))
	private void atSetLevel(ClientLevel clientLevel, CallbackInfo ci)
	{
		ArMovement.frameCounter.set(0L);
	}
}
