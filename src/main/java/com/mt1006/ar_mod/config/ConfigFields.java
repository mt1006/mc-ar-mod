package com.mt1006.ar_mod.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mt1006.ar_mod.ArMod;
import com.mt1006.ar_mod.config.gui.ModOptionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigFields
{
	private static @Nullable Map<String, String> defaultLanguageKeys = null;
	private final File file;
	private final List<Field<?>> fields = new ArrayList<>();
	private final Map<String, Field<?>> fieldMap = new HashMap<>();
	private final Set<Field<?>> fieldSet = new HashSet<>();

	public ConfigFields(String filename)
	{
		this.file = new File(Minecraft.getInstance().gameDirectory, "config/" + filename);
	}

	public ConfigFields.IntegerField add(String name, int val)
	{
		ConfigFields.IntegerField field = new ConfigFields.IntegerField(name, val);
		addField(field, name);
		return field;
	}

	public ConfigFields.FloatField add(String name, float val)
	{
		ConfigFields.FloatField field = new ConfigFields.FloatField(name, val);
		addField(field, name);
		return field;
	}

	public ConfigFields.BooleanField add(String name, boolean val)
	{
		ConfigFields.BooleanField field = new ConfigFields.BooleanField(name, val);
		addField(field, name);
		return field;
	}

	private void addField(Field<?> field, String name)
	{
		fields.add(field);
		if (fieldMap.put(name, field) != null) { throw new RuntimeException("Duplicate field names!"); };
		if (!fieldSet.add(field)) { throw new RuntimeException("Duplicate fields!"); }
	}

	public void save()
	{
		file.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file))))
		{
			fields.forEach((field) -> field.save(writer));
		}
		catch (IOException ignore) {}
	}

	public void load()
	{
		int loadedCount = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty()) { continue; }
				if (line.charAt(0) == '#') { continue; }
				if (StringUtils.isBlank(line)) { continue; }

				int equalSignPos = line.indexOf('=');
				if (equalSignPos == -1) { throw new IOException(); }

				String name = line.substring(0, equalSignPos).trim();
				String value = line.substring(equalSignPos + 1).trim();

				Field<?> field = fieldMap.get(name);
				if (field == null) { throw new IOException(); }

				field.load(value);
				loadedCount++;
			}
		}
		catch (IOException exception) { save(); }

		if (loadedCount != fields.size()) { save(); }
	}

	public void reset()
	{
		fields.forEach(Field::reset);
	}

	private static void loadDefaultLanguageKeys()
	{
		defaultLanguageKeys = new HashMap<>();

		try (InputStream stream = ArMod.class.getResourceAsStream("/assets/ar_mod/lang/en_us.json"))
		{
			if (stream == null) { return; }

			JsonObject json = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
			Pattern replacePattern = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");

			for(Map.Entry<String, JsonElement> entry : json.entrySet())
			{
				String str = replacePattern.matcher(GsonHelper.convertToString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
				defaultLanguageKeys.put(entry.getKey(), str);
			}
		}
		catch (JsonParseException | IOException ioexception)
		{
			ArMod.LOGGER.error("Failed to load default language keys!");
		}
	}

	public abstract static class Field<T>
	{
		private static final String NAME_KEY_PREFIX = "ar_mod.options.field.";
		private static final String DESC_KEY_SUFFIX = ".desc";
		private static final String DESC_ERROR = "[failed to load description]";
		public final String name;
		private final T defVal;
		public volatile T val;

		protected Field(String name, T val)
		{
			this.name = name;
			this.val = val;
			this.defVal = val;
		}

		protected void save(PrintWriter writer)
		{
			String description = String.format("%s\nDefault value: %s", getDefaultDescription(), this);
			BufferedReader reader = new BufferedReader(new StringReader(description));
			reader.lines().forEach((line) -> writer.println("# " + line));

			writer.printf("%s = %s\n\n", name, this);
		}

		protected void load(String str) throws IOException
		{
			try { fromString(str); }
			catch (NumberFormatException exception) { throw new IOException(); }
		}

		public void reset()
		{
			val = defVal;
		}

		@Override public String toString()
		{
			return val.toString();
		}

		public Component getWidgetName()
		{
			return Component.translatable(NAME_KEY_PREFIX + name);
		}

		public String getWidgetNameKey()
		{
			return NAME_KEY_PREFIX + name;
		}

		public Component getWidgetTooltip()
		{
			return Component.translatable("ar_mod.options.common.tooltip",
					Component.translatable(getDescriptionKey()), this.toString());
		}

		private String getDefaultDescription()
		{
			if (defaultLanguageKeys == null) { loadDefaultLanguageKeys(); }
			return defaultLanguageKeys.getOrDefault(getDescriptionKey(), DESC_ERROR);
		}

		private String getDescriptionKey()
		{
			return NAME_KEY_PREFIX + name + DESC_KEY_SUFFIX;
		}

		abstract void fromString(String str);
	}

	public static class IntegerField extends Field<Integer>
	{
		public IntegerField(String name, Integer val)
		{
			super(name, val);
		}

		@Override public void fromString(String str)
		{
			val = Integer.valueOf(str);
		}

		public AbstractWidget createSwitch(List<Integer> options)
		{
			return new ModOptionList.IntegerSwitch(this, options);
		}

		public AbstractWidget createSlider(int min, int max)
		{
			return new ModOptionList.IntegerSlider(this, min, max, 1, null);
		}

		public AbstractWidget createSlider(int min, int max, int multiplier, @Nullable List<Integer> specialValues)
		{
			return new ModOptionList.IntegerSlider(this, min, max, multiplier, specialValues);
		}
	}

	public static class FloatField extends Field<Float>
	{
		public FloatField(String name, Float val)
		{
			super(name, val);
		}

		@Override public void fromString(String str)
		{
			val = Float.valueOf(str);
		}

		public AbstractWidget createSlider(float min, float max, float step, int tailDigits)
		{
			return new ModOptionList.FloatSlider(this, min, max, step, tailDigits);
		}

		public AbstractWidget createPercentageSlider(int min, int max)
		{
			return new ModOptionList.FloatPercentageSlider(this, min, max);
		}
	}

	public static class BooleanField extends Field<Boolean>
	{
		public BooleanField(String name, Boolean val)
		{
			super(name, val);
		}

		@Override public void fromString(String str)
		{
			val = Boolean.valueOf(str);
		}

		public AbstractWidget createSwitch()
		{
			return new ModOptionList.BooleanSwitch(this);
		}
	}
}
