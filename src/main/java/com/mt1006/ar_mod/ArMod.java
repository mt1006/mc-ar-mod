package com.mt1006.ar_mod;

import com.mt1006.ar_mod.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ArMod.MOD_ID)
@Mod.EventBusSubscriber(modid = ArMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ArMod
{
	public static final String MOD_ID = "ar_mod";
	public static final String VERSION = "0.1";
	public static final String FOR_VERSION = "1.20.1";
	public static final String FOR_LOADER = "Forge";
	public static final Logger LOGGER = LogManager.getLogger();

	public ArMod()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event)
	{
		ArMod.LOGGER.info(getFullName() + " - Author: mt1006 (mt1006x)");
		ModConfig.load();
	}

	public static String getFullName()
	{
		return "ArMod v" + VERSION + " for Minecraft " + FOR_VERSION + " [" + FOR_LOADER + "]";
	}
}
