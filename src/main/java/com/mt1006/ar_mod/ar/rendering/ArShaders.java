package com.mt1006.ar_mod.ar.rendering;

import com.mt1006.ar_mod.ArMod;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ArShaders
{
	private static final Map<Integer, String> programNameMap = new HashMap<>();
	private static final Map<String, Integer> shaderObjectMap = new HashMap<>();

	private static int currentShader = 0;
	public static int frameShader = 0;
	public static int vignetteShader = 0;
	public static int timewarpShader = 0;
	public static int fullArShader = 0;

	private static int frameColorTexture = 0;

	private static int vignetteColorTexture = 0;
	private static int vignetteColor = 0;

	private static int timewarpColorTexture = 0;
	private static int timewarpViewMatrix = 0;
	private static int timewarpHeightMul = 0;
	private static int timewarpTopLeft = 0;
	private static int timewarpTopRight = 0;
	private static int timewarpBottomLeft = 0;
	private static int timewarpBottomRight = 0;
	private static int timewarpScreenSize = 0;
	private static int timewarpFarClip = 0;

	private static int fullArColorTexture = 0;
	private static int fullArDepthTexture = 0;
	private static int fullArViewMatrix = 0;
	private static int fullArHeightMul = 0;
	private static int fullArCameraVector = 0;
	private static int fullArCameraPosition = 0;
	private static int fullArTopLeft = 0;
	private static int fullArTopRight = 0;
	private static int fullArBottomLeft = 0;
	private static int fullArBottomRight = 0;
	private static int fullArScreenSize = 0;
	private static int fullArFarClip = 0;
	private static int fullArSequenceN = 0;
	private static int fullArSequenceR = 0;
	private static int fullArSequenceA0 = 0;

	public static void init()
	{
		frameShader = compile("frame");
		vignetteShader = compile("vignette");
		timewarpShader = compile("timewarp");
		fullArShader = compile("full_ar");

		frameColorTexture = findUniform(frameShader, "colorTexture");

		vignetteColorTexture = findUniform(vignetteShader, "colorTexture");
		vignetteColor = findUniform(vignetteShader, "color");

		timewarpColorTexture = findUniform(timewarpShader, "colorTexture");
		timewarpViewMatrix = findUniform(timewarpShader, "viewMatrix");
		timewarpHeightMul = findUniform(timewarpShader, "heightMul");
		timewarpTopLeft = findUniform(timewarpShader, "topLeft");
		timewarpTopRight = findUniform(timewarpShader, "topRight");
		timewarpBottomLeft = findUniform(timewarpShader, "bottomLeft");
		timewarpBottomRight = findUniform(timewarpShader, "bottomRight");
		timewarpScreenSize = findUniform(timewarpShader, "screenSize");
		timewarpFarClip = findUniform(timewarpShader, "farClip");

		fullArColorTexture = findUniform(fullArShader, "colorTexture");
		fullArDepthTexture = findUniform(fullArShader, "depthTexture");
		fullArViewMatrix = findUniform(fullArShader, "viewMatrix");
		fullArHeightMul = findUniform(fullArShader, "heightMul");
		fullArCameraVector = findUniform(fullArShader, "cameraVector");
		fullArCameraPosition = findUniform(fullArShader, "cameraPosition");
		fullArTopLeft = findUniform(fullArShader, "topLeft");
		fullArTopRight = findUniform(fullArShader, "topRight");
		fullArBottomLeft = findUniform(fullArShader, "bottomLeft");
		fullArBottomRight = findUniform(fullArShader, "bottomRight");
		fullArScreenSize = findUniform(fullArShader, "screenSize");
		fullArFarClip = findUniform(fullArShader, "farClip");
		fullArSequenceN = findUniform(fullArShader, "sequenceN");
		fullArSequenceR = findUniform(fullArShader, "sequenceR");
		fullArSequenceA0 = findUniform(fullArShader, "sequenceA0");
	}

	public static void switchShader(int shader)
	{
		if (shader == currentShader) { return; }
		GL30.glUseProgram(shader);
		currentShader = shader;
	}

	public static void setColorTexture(int texture)
	{
		int location = 0;
		if (currentShader == frameShader) { location = frameColorTexture; }
		else if (currentShader == vignetteShader) { location = vignetteColorTexture; }
		else if (currentShader == timewarpShader) { location = timewarpColorTexture; }
		else if (currentShader == fullArShader) { location = fullArColorTexture; }

		GL30.glUniform1i(location, 0);
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
	}

	public static void setDepthTexture(int texture)
	{
		GL30.glUniform1i(fullArDepthTexture, 1);
		GL30.glActiveTexture(GL30.GL_TEXTURE1);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
	}

	public static void setVignetteColor(float r, float g, float b)
	{
		GL30.glUniform3f(vignetteColor, r, g, b);
	}

	public static void setViewMatrix(float[] matrix)
	{
		GL30.glUniformMatrix4fv(currentShader == fullArShader ? fullArViewMatrix : timewarpViewMatrix, false, matrix);
	}

	public static void setProjectionMatrix11(float matrix11)
	{
		float heightMul = 2.0f / matrix11;
		GL30.glUniform1f(currentShader == fullArShader ? fullArHeightMul : timewarpHeightMul, heightMul);
	}

	public static void setCameraVector(Vector3f vector)
	{
		GL30.glUniform3f(fullArCameraVector, vector.x, vector.y, vector.z);
	}

	public static void setCameraPosition(Vector3f vector)
	{
		GL30.glUniform3f(fullArCameraPosition, vector.x, vector.y, vector.z);
	}

	public static void setVectorTopLeft(Vector3f vector)
	{
		GL30.glUniform3f(currentShader == fullArShader ? fullArTopLeft : timewarpTopLeft, vector.x, vector.y, vector.z);
	}

	public static void setVectorTopRight(Vector3f vector)
	{
		GL30.glUniform3f(currentShader == fullArShader ? fullArTopRight : timewarpTopRight, vector.x, vector.y, vector.z);
	}

	public static void setVectorBottomLeft(Vector3f vector)
	{
		GL30.glUniform3f(currentShader == fullArShader ? fullArBottomLeft : timewarpBottomLeft, vector.x, vector.y, vector.z);
	}

	public static void setVectorBottomRight(Vector3f vector)
	{
		GL30.glUniform3f(currentShader == fullArShader ? fullArBottomRight : timewarpBottomRight, vector.x, vector.y, vector.z);
	}

	public static void setScreenSize(int w, int h)
	{
		GL30.glUniform2f(currentShader == fullArShader ? fullArScreenSize : timewarpScreenSize, (float)w, (float)h);
	}

	public static void setFarClip(float val)
	{
		GL30.glUniform1f(currentShader == fullArShader ? fullArFarClip : timewarpFarClip, val);
	}

	public static void setSequenceParameters(int n, float r, float a0)
	{
		GL30.glUniform1i(fullArSequenceN, n);
		GL30.glUniform1f(fullArSequenceR, r);
		GL30.glUniform1f(fullArSequenceA0, a0);
	}

	public static boolean isReprojectionEnabled()
	{
		return currentShader != frameShader;
	}

	public static boolean isFullArEnabled()
	{
		return currentShader == fullArShader;
	}

	private static int compile(String fshFile)
	{
		int program = GL30.glCreateProgram();
		programNameMap.put(program, "common+" + fshFile);

		int vsh = getOrCompileShaderObject(GL30.GL_VERTEX_SHADER, "common.vsh");
		int fsh = getOrCompileShaderObject(GL30.GL_FRAGMENT_SHADER, fshFile + ".fsh");
		GL30.glAttachShader(program, vsh);
		GL30.glAttachShader(program, fsh);

		GL30.glLinkProgram(program);
		return program;
	}

	private static int findUniform(int program, String name)
	{
		int location = GL30.glGetUniformLocation(program, name);
		if (location == -1) { ArMod.LOGGER.error("Failed to find shader uniform: {}/{}", programNameMap.get(program), name); }
		return location;
	}

	private static int getOrCompileShaderObject(int type, String name)
	{
		Integer shaderObject = shaderObjectMap.get(name);
		if (shaderObject != null) { return shaderObject; }

		int shader = GL30.glCreateShader(type);
		GL30.glShaderSource(shader, loadShaderFile(name));
		compileAndCheck(name, shader);

		shaderObjectMap.put(name, shader);
		return shader;
	}

	private static String loadShaderFile(String name)
	{
		try (InputStream stream = ArMod.class.getResourceAsStream("/ar_shaders/" + name))
		{
			if (stream == null) { throw new RuntimeException("Shader InputStream is null!"); }
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}

	private static void compileAndCheck(String name, int shader)
	{
		GL30.glCompileShader(shader);

		if (GL30.glGetShaderi(shader, GL30.GL_COMPILE_STATUS) == GL30.GL_FALSE)
		{
			ArMod.LOGGER.error("Shader compilation error - {}!", name);
			if (GL30.glGetShaderi(shader, GL30.GL_INFO_LOG_LENGTH) != 0)
			{
				ArMod.LOGGER.error(GL30.glGetShaderInfoLog(shader));
			}
			else
			{
				ArMod.LOGGER.error("No more information available!");
			}
		}
	}
}
