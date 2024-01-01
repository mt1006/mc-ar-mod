package com.mt1006.ar_mod.ar.movement;

import com.mojang.math.Axis;
import com.mt1006.ar_mod.config.ModConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class ArPlayerBob
{
	private static final Timer timer = new Timer(20.0f, 0);
	public static volatile float walkDist = 0.0f, oldWalkDist = 0.0f, bob = 0.0f;

	public static void applyBobIfNeeded(Matrix4f viewMatrix)
	{
		//TODO: fix
		if (ModConfig.recreateBobbing.val && Minecraft.getInstance().options.bobView().get())
		{
			float diff = walkDist - oldWalkDist;
			float val = -(walkDist + diff * timer.partialTick);
			float bobVal = bob;
			viewMatrix.translate(Mth.sin(val * (float)Math.PI) * bobVal * 0.5f, -Math.abs(Mth.cos(val * (float)Math.PI) * bobVal), 0.0f);
			viewMatrix.rotate(Axis.ZP.rotationDegrees(Mth.sin(val * (float)Math.PI) * bobVal * 3.0f));
			viewMatrix.rotate(Axis.XP.rotationDegrees(Math.abs(Mth.cos(val * (float) Math.PI - 0.2f) * bobVal) * 5.0f));
		}
	}

	public static void updateTimer()
	{
		timer.advanceTime(Util.getMillis());
	}
}
