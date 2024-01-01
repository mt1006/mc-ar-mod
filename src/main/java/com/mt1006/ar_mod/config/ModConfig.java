package com.mt1006.ar_mod.config;

import com.mt1006.ar_mod.ar.rendering.ArShaders;
import com.mt1006.ar_mod.config.gui.ModOptionList;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class ModConfig
{
	private static final ConfigFields configFields = new ConfigFields("ar_mod.txt");

	public static final ConfigFields.BooleanField cameraReprojection = configFields.add("camera_reprojection", true);
	public static final ConfigFields.BooleanField movementReprojection = configFields.add("movement_reprojection", true);
	public static final ConfigFields.IntegerField maxReprojectedFPS = configFields.add("max_reprojected_fps", 120);
	public static final ConfigFields.BooleanField reprojectionVSync = configFields.add("reprojection_vsync", true);
	private static final ConfigFields.IntegerField wrappingMode = configFields.add("wrapping_mode", 1);
	private static final ConfigFields.BooleanField linearTextureFiltering = configFields.add("linear_texture_filtering", false);
	public static final ConfigFields.FloatField fovScaling = configFields.add("fov_scaling", 1.0f);
	public static final ConfigFields.IntegerField sequenceN = configFields.add("sequence_n", 48);
	public static final ConfigFields.FloatField sequenceA0 = configFields.add("sequence_a0", 0.45f);
	public static final ConfigFields.FloatField sequenceR = configFields.add("sequence_r", 1.12f);
	public static final ConfigFields.BooleanField responsiveSprinting = configFields.add("responsive_sprinting", true);
	public static final ConfigFields.BooleanField responsiveFlying = configFields.add("responsive_flying", true);
	//public static final ConfigFields.BooleanField asyncTextures = configFields.add("asyncTextures", true);
	//public static final ConfigFields.BooleanField asyncFrameBuffer = configFields.add("asyncFrameBuffer", true);
	private static final ConfigFields.IntegerField debugFPSLimit = configFields.add("debug_fps_limit", 10);
	public static final ConfigFields.BooleanField simulateRealDelay = configFields.add("simulate_real_delay", false);
	private static final ConfigFields.FloatField offsetX = configFields.add("offset_x", 0.0f);
	private static final ConfigFields.FloatField offsetY = configFields.add("offset_y", 0.0f);
	private static final ConfigFields.FloatField offsetZ = configFields.add("offset_z", 0.0f);
	public static final ConfigFields.BooleanField printAsyncFPS = configFields.add("print_async_fps", false);
	public static final ConfigFields.BooleanField warnAboutWaiting = configFields.add("warn_about_waiting", false);
	public static final ConfigFields.BooleanField recreateBobbing = configFields.add("recreate_bobbing__broken", false);

	public static void initWidgets(ModOptionList list)
	{
		list.add(ModConfig.cameraReprojection.createSwitch());
		list.add(ModConfig.movementReprojection.createSwitch());
		list.add(ModConfig.maxReprojectedFPS.createSlider(3, 37, 10, List.of(37)));
		list.add(ModConfig.reprojectionVSync.createSwitch());

		list.addLabel("plane_reprojection_settings");
		list.add(ModConfig.wrappingMode.createSwitch(List.of(0, 1, 2, 3)));
		list.add(ModConfig.linearTextureFiltering.createSwitch());
		list.add(ModConfig.fovScaling.createPercentageSlider(100, 200));

		list.addLabel("movement_reprojection_quality");
		list.add(ModConfig.sequenceN.createSlider(4, 128));
		list.add(ModConfig.sequenceA0.createSlider(0.05f, 2.0f, 0.05f, 2));
		list.add(ModConfig.sequenceR.createSlider(1.05f, 1.7f, 0.01f, 2));
		list.addViewDistanceLabel();

		list.addLabel("improvements");
		list.add(ModConfig.responsiveSprinting.createSwitch());
		list.add(ModConfig.responsiveFlying.createSwitch());

		list.addLabel("debugging_options");
		list.add(ModConfig.debugFPSLimit.createSlider(1, 10, 1, List.of(10)));
		list.add(ModConfig.simulateRealDelay.createSwitch());
		list.add(ModConfig.offsetX.createSlider(-3.0f, 3.0f, 0.05f, 2));
		list.add(ModConfig.offsetY.createSlider(-3.0f, 3.0f, 0.05f, 2));
		list.add(ModConfig.offsetZ.createSlider(-3.0f, 3.0f, 0.05f, 2));
		list.add(ModConfig.printAsyncFPS.createSwitch());
		list.add(ModConfig.warnAboutWaiting.createSwitch());
	}

	public static void load()
	{
		configFields.load();
	}

	public static void save()
	{
		configFields.save();
	}

	public static void reset()
	{
		configFields.reset();
	}

	public static boolean isFPSLimitEnabled()
	{
		return maxReprojectedFPS.val >= 1 && maxReprojectedFPS.val <= 360;
	}

	public static int getWrappingMode()
	{
		return switch (wrappingMode.val)
		{
			case 0 -> GL30.GL_CLAMP_TO_BORDER;
			case 2 -> GL30.GL_REPEAT;
			case 3 -> GL30.GL_MIRRORED_REPEAT;
			default -> GL30.GL_CLAMP_TO_EDGE;
		};
	}

	public static int getFilteringMode()
	{
		return linearTextureFiltering.val ? GL30.GL_LINEAR : GL30.GL_NEAREST;
	}

	public static void setUniforms()
	{
		if (!ArShaders.isFullArEnabled()) { return; }
		ArShaders.setSequenceParameters(sequenceN.val, sequenceR.val, sequenceA0.val);
	}

	public static Vector3f getOffset()
	{
		return new Vector3f(offsetX.val, offsetY.val, offsetZ.val);
	}

	public static int getLevelShader()
	{
		if (cameraReprojection.val || movementReprojection.val)
		{
			return movementReprojection.val ? ArShaders.fullArShader : ArShaders.timewarpShader;
		}
		else
		{
			return ArShaders.frameShader;
		}
	}

	public static int getDebugFPSLimit(int fromVideoSettings)
	{
		return (debugFPSLimit.val > 0 && debugFPSLimit.val < 10) ? debugFPSLimit.val : fromVideoSettings;
	}
}
