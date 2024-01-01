package com.mt1006.ar_mod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mt1006.ar_mod.ar.ArWindow;
import com.mt1006.ar_mod.ar.movement.ArMouse;
import com.mt1006.ar_mod.ar.movement.ArMovement;
import com.mt1006.ar_mod.ar.movement.ArPlayerBob;
import com.mt1006.ar_mod.ar.rendering.ArFrame;
import com.mt1006.ar_mod.config.ModConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
	@Shadow @Final Minecraft minecraft;
	@Shadow private boolean renderHand;
	@Shadow public abstract float getDepthFar();
	@Shadow protected abstract void renderItemInHand(PoseStack p_109121_, Camera p_109122_, float p_109123_);
	@Shadow public abstract Matrix4f getProjectionMatrix(double p_254507_);
	@Shadow protected abstract double getFov(Camera p_109142_, float p_109143_, boolean p_109144_);

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", remap = false))
	public void bindLevelFBO(float f1, long l1, boolean renderLevel, CallbackInfo ci)
	{
		if (renderLevel && this.minecraft.level != null)
		{
			ArWindow.renderingLevel = true;
			ArWindow.getWriteFrame().level.bindWrite();
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", remap = false))
	public void cancelRenderClear(int val, boolean b)
	{
		if (!ArWindow.renderingLevel) { RenderSystem.clear(val, b); }
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setOrtho(FFFFFF)Lorg/joml/Matrix4f;", remap = false))
	public void bindGuiFBO(float f1, long l1, boolean renderLevel, CallbackInfo ci)
	{
		ArWindow.renderingLevel = false;
		ArWindow.getWriteFrame().gui.bindWrite();

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		if (renderLevel && minecraft.level != null && renderHand)
		{
			this.renderItemInHand(new PoseStack(), new Camera(), f1);
		}

		GL11.glClearDepth(1.0f);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	public void cancelBobView(GameRenderer instance, PoseStack poseStack, float ticks)
	{
		if (ModConfig.recreateBobbing.val && minecraft.getCameraEntity() instanceof Player)
		{
			Player player = (Player)this.minecraft.getCameraEntity();
			ArPlayerBob.walkDist = player.walkDist;
			ArPlayerBob.oldWalkDist = player.walkDistO;
			ArPlayerBob.bob = player.bob;
		}
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getXRot()F"))
	public float captureCamera(Camera camera)
	{
		//TODO: add roll
		ArFrame frame = ArWindow.getWriteFrame();
		frame.shaderInput.viewMatrix = ArMouse.recreateViewMatrix(camera.getXRot(), camera.getYRot(), 0.0f);
		frame.shaderInput.cameraForward = new Vector3f(camera.getLookVector()).normalize();

		if (minecraft.player != null)
		{
			frame.rotX = camera.getXRot();
			frame.rotY = camera.getYRot();

			//TODO: fix eye height
			Vec3 cameraPos = camera.getPosition().subtract(0.0, minecraft.player.getEyeHeight(), 0.0);
			ArMovement.newPos = cameraPos;
			ArMovement.frameCounter.incrementAndGet();
			frame.shaderInput.playerPosition = cameraPos.toVector3f();
		}
		else
		{
			frame.shaderInput.playerPosition = new Vector3f();
		}
		return camera.getXRot();
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V"))
	public void captureFrameData(LevelRenderer instance, PoseStack poseStack, float f1, long l1, boolean b1, Camera camera,
								 GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix)
	{
		//TODO: move it
		ArMouse.frozenTick = f1;
		Matrix4f newMatrix = new Matrix4f(matrix);
		if (ModConfig.fovScaling.val != 1.0f)
		{
			newMatrix.mul(getProjectionMatrix(getFov(camera, f1, true)).invert());
			newMatrix.mul(getProjectionMatrix(getFov(camera, f1, true) / ModConfig.fovScaling.val));
		}
		ArMouse.projectionMatrix.set(newMatrix);

		ArFrame frame = ArWindow.getWriteFrame();
		frame.renderHand = renderHand;
		frame.shaderInput.projectionMatrix11 = matrix.get(1, 1);
		frame.shaderInput.cameraPosition = camera.getPosition().toVector3f();
		frame.shaderInput.farClip = getDepthFar();

		instance.renderLevel(poseStack, f1, l1, b1, camera, gameRenderer, lightTexture, matrix);
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", remap = false))
	public void cancelRenderLevelClear(int val, boolean b) {}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V"))
	public void cancelRenderItemInHand(GameRenderer gameRenderer, PoseStack poseStack, Camera camera, float f) {}
}
