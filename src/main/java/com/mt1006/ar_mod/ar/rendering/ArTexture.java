package com.mt1006.ar_mod.ar.rendering;

import com.mt1006.ar_mod.config.ModConfig;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ArTexture
{
	public final int id;
	protected int w = -1, h = -1;
	protected Format format = Format.NONE;
	private int filteringMode, wrappingMode;

	public ArTexture()
	{
		id = GL30.glGenTextures();
	}

	public void setParameters(int w, int h, Format format)
	{
		this.w = w;
		this.h = h;
		this.format = format;

		filteringMode = ModConfig.getFilteringMode();
		wrappingMode = ModConfig.getWrappingMode();

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, filteringMode);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, filteringMode);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, wrappingMode);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, wrappingMode);

		if (format.isDepth) { GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, format.internal, w, h, 0, format.id, GL30.GL_FLOAT, (FloatBuffer)null); }
		else { GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, format.internal, w, h, 0, format.id, GL30.GL_UNSIGNED_BYTE, (ByteBuffer)null); }

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
	}

	public void updateParameters()
	{
		int newFilteringMode = ModConfig.getFilteringMode();
		int newWrappingMode = ModConfig.getWrappingMode();

		if (newFilteringMode != filteringMode || newWrappingMode != wrappingMode)
		{
			filteringMode = newFilteringMode;
			wrappingMode = newWrappingMode;

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, filteringMode);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, filteringMode);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, wrappingMode);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, wrappingMode);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}
	}

	public void setData(int w, int h, Format format, ByteBuffer buffer)
	{
		if (w != this.w || h != this.h || format != this.format) { setParameters(w, h, format); }
		buffer.clear();

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);

		if (format.isDepth) { GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, format.internal, w, h, 0, format.id, GL30.GL_FLOAT, buffer); }
		else { GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, format.internal, w, h, 0, format.id, GL30.GL_UNSIGNED_BYTE, buffer); }
		//if (format.isDepth) { GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, w, h, format.id, GL30.GL_FLOAT, buffer); }
		//else { GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, w, h, format.id, GL30.GL_UNSIGNED_BYTE, buffer); }

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
	}

	//TODO: merge with setData (maybe)
	//Credits: http://www.songho.ca/opengl/gl_pbo.html
	/*public void setDataAsync(int w, int h, Format format, ByteBuffer buffer, int pbo)
	{
		if (w != this.w || h != this.h || format != this.format) { setParameters(w, h, format); }
		buffer.clear();

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
		GL30.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, pbo);

		if (format.isDepth) { GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, format.internal, w, h, 0, format.id, GL30.GL_FLOAT, (ByteBuffer)null); }
		else { GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, format.internal, w, h, 0, format.id, GL30.GL_UNSIGNED_BYTE, (ByteBuffer)null); }

		GL30.glBufferData(GL30.GL_PIXEL_UNPACK_BUFFER, (long)w * (long)h * 4L, GL30.GL_STREAM_DRAW);
		ByteBuffer dest = GL30.glMapBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, GL30.GL_WRITE_ONLY);
		if (dest != null)
		{
			//dest.clear();
			dest.put(buffer);
		}
		GL30.glUnmapBuffer(GL30.GL_PIXEL_UNPACK_BUFFER);

		GL30.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
	}*/

	public enum Format
	{
		NONE(0, 0, 0, false),
		RGB(GL30.GL_RGB, GL30.GL_RGB8, 3, false),
		RGBA(GL30.GL_RGBA, GL30.GL_RGBA8, 3, false),
		DEPTH(GL30.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT32F, 4, true);

		public final int id;
		public final int internal;
		public final long pixelSize; //TODO: remove if not used
		public final boolean isDepth;

		Format(int id, int internal, int pixelSize, boolean isDepth)
		{
			this.id = id;
			this.internal = internal;
			this.pixelSize = pixelSize;
			this.isDepth = isDepth;
		}
	}
}
