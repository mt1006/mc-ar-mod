package com.mt1006.ar_mod;

import com.mt1006.ar_mod.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArMod implements ModInitializer
{
	public static final String MOD_ID = "ar_mod";
	public static final String VERSION = "0.1";
	public static final String FOR_VERSION = "1.20.1";
	public static final String FOR_LOADER = "Fabric";
	public static final Logger LOGGER = LogManager.getLogger();

	@Override public void onInitialize()
	{
		ArMod.LOGGER.info(getFullName() + " - Author: mt1006 (mt1006x)");
		ModConfig.load();
	}

	public static String getFullName()
	{
		return "ArMod v" + VERSION + " for Minecraft " + FOR_VERSION + " [" + FOR_LOADER + "]";
	}
}
