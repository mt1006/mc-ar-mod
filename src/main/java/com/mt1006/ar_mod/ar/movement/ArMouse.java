package com.mt1006.ar_mod.ar.movement;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mt1006.ar_mod.ar.ArWindow;
import com.mt1006.ar_mod.ar.rendering.ArFrame;
import com.mt1006.ar_mod.ar.rendering.ArShaders;
import com.mt1006.ar_mod.config.ModConfig;
import com.mt1006.ar_mod.mixin.fields.MouseHandlerFields;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;

public class ArMouse
{
	private static final Vector2f TOP_LEFT = new Vector2f(0.0f, 1.0f);
	private static final Vector2f TOP_RIGHT = new Vector2f(1.0f, 1.0f);
	private static final Vector2f BOTTOM_LEFT = new Vector2f(0.0f, 0.0f);
	private static final Vector2f BOTTOM_RIGHT = new Vector2f(1.0f, 0.0f);
	private static final Vector4f NEAR_VECTOR = new Vector4f();
	private static final Vector4f FAR_VECTOR = new Vector4f();

	private static double lastUpdateTime = 0.0;
	private static final AtomicDouble accumulatedDeltaX = new AtomicDouble(0.0);
	private static final AtomicDouble accumulatedDeltaY = new AtomicDouble(0.0);
	private static final SmoothDouble smoothTurnX = new SmoothDouble();
	private static final SmoothDouble smoothTurnY = new SmoothDouble();
	private static volatile float frozenRotX = 0.0f, frozenRotY = 0.0f;
	public static volatile float frozenTick = 0.0f;
	private static final AtomicBoolean ignoreFirstMove = new AtomicBoolean(false);
	private static final AtomicBoolean oldIgnoreFirstMove = new AtomicBoolean(false);
	private static final AtomicBoolean mouseGrabbed = new AtomicBoolean(false);
	private static final AtomicBoolean isPlayerScoping = new AtomicBoolean(false);
	public static AtomicMatrix projectionMatrix = new AtomicMatrix();
	private static double cursorX, cursorY;
	public static volatile float forwardX, forwardY, forwardZ;

	public static void onMove(long window, double x, double y)
	{
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.execute(() -> ((MouseHandlerFields)minecraft.mouseHandler).callOnMove(window, x, y));

		if (window != ArWindow.visibleWindow) { return; }

		double updateTime = GLFW.glfwGetTime();
		double updateTimeDiff = updateTime - lastUpdateTime;
		lastUpdateTime = updateTime;

		int deltaX, deltaY;
		if (ignoreFirstMove.get())
		{
			deltaX = 0;
			deltaY = 0;
			ignoreFirstMove.set(false);
		}
		else
		{
			deltaX = (int)(x - cursorX);
			deltaY = (int)(y - cursorY);
		}
		cursorX = x;
		cursorY = y;

		if (mouseGrabbed.get() && minecraft.isWindowActive())
		{
			updateCamera(minecraft, deltaX, deltaY, updateTimeDiff);
		}
	}

	private static void updateCamera(Minecraft minecraft, int cursorDeltaX, int cursorDeltaY, double updateTimeDiff)
	{
		double sensitivitySetting = minecraft.options.sensitivity().get() * 0.6 + 0.2;
		double lowSensitivity = sensitivitySetting * sensitivitySetting * sensitivitySetting;
		double normalSensitivity = lowSensitivity * 8.0;

		double deltaX, deltaY;

		if (minecraft.options.smoothCamera)
		{
			deltaX = smoothTurnX.getNewDeltaValue(cursorDeltaX * normalSensitivity, updateTimeDiff * normalSensitivity);
			deltaY = smoothTurnY.getNewDeltaValue(cursorDeltaY * normalSensitivity, updateTimeDiff * normalSensitivity);
		}
		else
		{
			smoothTurnX.reset();
			smoothTurnY.reset();
			double sensitivity = (minecraft.options.getCameraType().isFirstPerson() && isPlayerScoping.get())
					? lowSensitivity : normalSensitivity;
			deltaX = cursorDeltaX * sensitivity;
			deltaY = cursorDeltaY * sensitivity;
		}

		accumulatedDeltaY.addAndGet(deltaX);
		accumulatedDeltaX.addAndGet(deltaY);
	}

	public static void updateMouseRenderThread()
	{
		Minecraft minecraft = Minecraft.getInstance();

		boolean ignoreFirstMoveVal = ((MouseHandlerFields)minecraft.mouseHandler).getIgnoreFirstMove();
		if (ignoreFirstMoveVal && !oldIgnoreFirstMove.get()) { ignoreFirstMove.set(true); }
		oldIgnoreFirstMove.set(ignoreFirstMoveVal);

		mouseGrabbed.set(((MouseHandlerFields)minecraft.mouseHandler).getMouseGrabbed());
		isPlayerScoping.set(minecraft.player != null && minecraft.player.isScoping());

		double deltaX = accumulatedDeltaX.getAndSet(0.0);
		double deltaY = accumulatedDeltaY.getAndSet(0.0);

		minecraft.getTutorial().onMouse(deltaX, deltaY);
		if (minecraft.player != null && minecraft.level != null)
		{
			int invertMouse = minecraft.options.invertYMouse().get() ? -1 : 1;
			minecraft.player.turn(deltaY, deltaX * invertMouse);

			Camera camera = new Camera();
			camera.setup(minecraft.level,
					minecraft.getCameraEntity() == null ? minecraft.player : minecraft.getCameraEntity(),
					!minecraft.options.getCameraType().isFirstPerson(),
					minecraft.options.getCameraType().isMirrored(), frozenTick);
			frozenRotX = camera.getXRot();
			frozenRotY = camera.getYRot();

			Vector3f forwardVector = new Vector3f(camera.getLookVector()).normalize();
			forwardX = forwardVector.x;
			forwardY = forwardVector.y;
			forwardZ = forwardVector.z;
		}
	}

	public static void setUniforms(ArFrame frame)
	{
		Matrix4f viewMatrix;
		if (ModConfig.cameraReprojection.val)
		{
			viewMatrix = recreateViewMatrix(getRotX(), getRotY(), 0.0f);
			ArPlayerBob.applyBobIfNeeded(viewMatrix);
		}
		else
		{
			viewMatrix = frame.shaderInput.viewMatrix;
		}

		Matrix4f matrix = projectionMatrix.get().mul(viewMatrix, new Matrix4f()).invert();
		ArShaders.setVectorTopLeft(viewportPointToRay(TOP_LEFT, matrix));
		ArShaders.setVectorTopRight(viewportPointToRay(TOP_RIGHT, matrix));
		ArShaders.setVectorBottomLeft(viewportPointToRay(BOTTOM_LEFT, matrix));
		ArShaders.setVectorBottomRight(viewportPointToRay(BOTTOM_RIGHT, matrix));
	}

	public static Matrix4f recreateViewMatrix(float xRot, float yRot, float zRot)
	{
		PoseStack poseStack = new PoseStack();
		poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
		poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
		poseStack.mulPose(Axis.YP.rotationDegrees(yRot + 180.0f));
		return poseStack.last().pose();
	}

	public static Vector3f viewportPointToRay(Vector2f point, Matrix4f matrix)
	{
		//Credits: https://discussions.unity.com/t/code-behind-camera-viewportpointtoray/230871/3/
		Vector4f vec = new Vector4f(point.x * 2.0f - 1.0f, point.y * 2.0f - 1.0f, -1.0f, 1.0f);
		vec.mul(matrix, NEAR_VECTOR);
		NEAR_VECTOR.div(NEAR_VECTOR.w);

		vec.z = 1.0f;
		vec.mul(matrix, FAR_VECTOR);
		FAR_VECTOR.div(FAR_VECTOR.w);

		Vector4f retVec = FAR_VECTOR.sub(NEAR_VECTOR).normalize();
		return new Vector3f(retVec.x, retVec.y, retVec.z);
	}

	private static float getRotX()
	{
		double direction = Minecraft.getInstance().options.invertYMouse().get() ? -1.0 : 1.0;
		return Mth.clamp((float)(accumulatedDeltaX.get() * 0.15 * direction) + frozenRotX, -90.0f, 90.0f);
	}

	private static float getRotY()
	{
		return (float)(accumulatedDeltaY.get() * 0.15) + frozenRotY;
	}

	public static class AtomicMatrix
	{
		private volatile Matrix4f matrix1 = new Matrix4f();
		private volatile Matrix4f matrix2 = new Matrix4f();
		private final AtomicBoolean matrixSwitch = new AtomicBoolean(false);

		public Matrix4f get()
		{
			return matrixSwitch.get() ? matrix2 : matrix1;
		}

		public void set(Matrix4f newMatrix)
		{
			boolean matrixSwitchVal = matrixSwitch.get();
			if (matrixSwitchVal) { matrix1 = newMatrix; }
			else { matrix2 = newMatrix; }
			matrixSwitch.set(!matrixSwitchVal);
		}
	}
}
