package net.zarathul.simpleautomations.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simplemodslib.SimpleModsLib;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.item.ItemRegistrar;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

public final class ModItems
{
	public static final String ANTIDOTE_NAME = "antidote";
	public static final String SILENCE_TONIC_NAME = "silence_tonic";

	private static final String ANTIDOTE_TOOLTIP_KEY         = "item." + Simpleautomations.MOD_ID + "." + ANTIDOTE_NAME + ".tooltip";
	private static final String ANTIDOTE_TOOLTIP_DETAILS_KEY = "item." + Simpleautomations.MOD_ID + "." + ANTIDOTE_NAME + ".tooltip_details";
	private static final String SILENCE_TONIC_TOOLTIP_KEY         = "item." + Simpleautomations.MOD_ID + "." + SILENCE_TONIC_NAME + ".tooltip";
	private static final String SILENCE_TONIC_TOOLTIP_DETAILS_KEY = "item." + Simpleautomations.MOD_ID + "." + SILENCE_TONIC_NAME + ".tooltip_details";

	public static final ItemRegistrar REGISTRAR = new ItemRegistrar(Simpleautomations.MOD_ID);
	public static final AntidoteItem antidoteItem = REGISTRAR.register(ANTIDOTE_NAME, AntidoteItem::new, new Item.Properties().stacksTo(1));
	public static final SilenceTonicItem silenceTonicItem = REGISTRAR.register(SILENCE_TONIC_NAME, SilenceTonicItem::new, new Item.Properties().stacksTo(1));

	public static void init()
	{
		Simpleautomations.LOG.info("Registering items.");

		Collections.addAll(SimpleModsLib.creativeModeTabItems,
			antidoteItem,
			silenceTonicItem
		);
	}

	@Environment(EnvType.CLIENT)
	public static void registerTooltips()
	{
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			String tooltipKey;
			String tooltipDetailsKey;

			if (stack.getItem() == ModItems.antidoteItem)
			{
				tooltipKey = ANTIDOTE_TOOLTIP_KEY;
				tooltipDetailsKey = ANTIDOTE_TOOLTIP_DETAILS_KEY;
			}
			else if (stack.getItem() == ModItems.silenceTonicItem)
			{
				tooltipKey = SILENCE_TONIC_TOOLTIP_KEY;
				tooltipDetailsKey = SILENCE_TONIC_TOOLTIP_DETAILS_KEY;
			}
			else
			{
				return;
			}

			var mc = Minecraft.getInstance();
			long windowHandle = mc.getWindow().handle();
			boolean isShiftPressed = (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS);
			int maxWidth = mc.getWindow().getGuiScaledWidth() / 3;

			if (isShiftPressed)
			{
				lines.addAll(Utils.multiLineTranslateWithMaxWidth(tooltipDetailsKey, maxWidth));
			}
			else
			{
				lines.add(Component.literal(Utils.translate(tooltipKey)));
			}
		});
	}
}