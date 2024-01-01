package com.mt1006.ar_mod.config.gui;

import com.mt1006.ar_mod.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen
{
	private final Screen lastScreen;
	private ModOptionList list;
	private Button doneButton, resetButton;

	public ConfigScreen(Screen lastScreen)
	{
		super(Component.translatable("ar_mod.options"));
		this.lastScreen = lastScreen;
	}

	@Override public void init()
	{
		list = new ModOptionList(Minecraft.getInstance(), width, height, 32, height - 32, 25, font);

		doneButton = Button.builder(CommonComponents.GUI_DONE, (b) -> onDonePress(lastScreen))
				.pos(width / 2 - 155, height - 27).size(150, 20).build();
		resetButton = Button.builder(Component.translatable("ar_mod.options.common.reset_settings"), (b) -> onResetPress(list))
				.pos(width / 2 + 5, height - 27).size(150, 20).build();

		ModConfig.initWidgets(list);

		addWidget(list);
		addWidget(doneButton);
		addWidget(resetButton);
	}

	private static void onDonePress(Screen lastScreen)
	{
		ModConfig.save();
		Minecraft.getInstance().setScreen(lastScreen);
	}

	private static void onResetPress(ModOptionList list)
	{
		ModConfig.reset();
		list.updateValues();
	}

	@Override public void onClose()
	{
		ModConfig.save();
		Minecraft.getInstance().setScreen(this.lastScreen);
	}

	@Override public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float idk)
	{
		renderBackground(guiGraphics);
		list.render(guiGraphics, mouseX, mouseY, idk);
		doneButton.render(guiGraphics, mouseX, mouseY, idk);
		resetButton.render(guiGraphics, mouseX, mouseY, idk);
		guiGraphics.drawCenteredString(font, title, width / 2, 20, 16777215);
		super.render(guiGraphics, mouseX, mouseY, idk);
	}
}