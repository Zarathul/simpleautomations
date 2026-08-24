package net.zarathul.simpleautomations.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simpleautomations.common.DistillationLevel;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simpleautomations.components.AlcoholDistillationLevel;
import net.zarathul.simplemodslib.SimpleModsLib;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.item.ItemRegistrar;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

public final class ModItems
{
	public static final String ANTIDOTE_NAME = "antidote";
	public static final String SILENCE_TONIC_NAME = "silence_tonic";
	public static final String ALCOHOL_BUCKET_NAME = "alcohol_bucket";
	public static final String CONCENTRATED_ALCOHOL_BUCKET_NAME = "concentrated_alcohol_bucket";
	public static final String PURE_ALCOHOL_BUCKET_NAME = "pure_alcohol_bucket";

	private static final String ANTIDOTE_TOOLTIP_KEY         = "item." + Simpleautomations.MOD_ID + "." + ANTIDOTE_NAME + ".tooltip";
	private static final String ANTIDOTE_TOOLTIP_DETAILS_KEY = "item." + Simpleautomations.MOD_ID + "." + ANTIDOTE_NAME + ".tooltip_details";
	private static final String SILENCE_TONIC_TOOLTIP_KEY         = "item." + Simpleautomations.MOD_ID + "." + SILENCE_TONIC_NAME + ".tooltip";
	private static final String SILENCE_TONIC_TOOLTIP_DETAILS_KEY = "item." + Simpleautomations.MOD_ID + "." + SILENCE_TONIC_NAME + ".tooltip_details";

	private static final ItemRegistrar REGISTRAR = new ItemRegistrar(Simpleautomations.MOD_ID);

	public static final AntidoteItem ANTIDOTE = REGISTRAR.register(ANTIDOTE_NAME, AntidoteItem::new, new Item.Properties().stacksTo(1));
	public static final SilenceTonicItem SILENCE_TONIC = REGISTRAR.register(SILENCE_TONIC_NAME, SilenceTonicItem::new, new Item.Properties().stacksTo(1));
	public static final AlcoholBucketItem ALCOHOL_BUCKET = REGISTRAR.register(ALCOHOL_BUCKET_NAME, properties -> new AlcoholBucketItem(ModFluids.ALCOHOL_STILL, properties),
		new Item.Properties()
			.craftRemainder(Items.BUCKET)
			.stacksTo(1)
			.component(ModComponents.ALCOHOL_DISTILLATION_LEVEL, new AlcoholDistillationLevel(DistillationLevel.NORMAL)));
	public static final AlcoholBucketItem CONCENTRATED_ALCOHOL_BUCKET = REGISTRAR.register(CONCENTRATED_ALCOHOL_BUCKET_NAME, properties -> new AlcoholBucketItem(ModFluids.ALCOHOL_STILL, properties),
		new Item.Properties()
			.craftRemainder(Items.BUCKET)
			.stacksTo(1)
			.component(ModComponents.ALCOHOL_DISTILLATION_LEVEL, new AlcoholDistillationLevel(DistillationLevel.CONCENTRATED))
			.modelId(Simpleautomations.modId(ALCOHOL_BUCKET_NAME)));
	public static final AlcoholBucketItem PURE_ALCOHOL_BUCKET = REGISTRAR.register(PURE_ALCOHOL_BUCKET_NAME, properties -> new AlcoholBucketItem(ModFluids.ALCOHOL_STILL, properties),
		new Item.Properties()
			.craftRemainder(Items.BUCKET)
			.stacksTo(1)
			.component(ModComponents.ALCOHOL_DISTILLATION_LEVEL, new AlcoholDistillationLevel(DistillationLevel.PURE))
			.modelId(Simpleautomations.modId(ALCOHOL_BUCKET_NAME)));

	public static void init()
	{
		Simpleautomations.LOG.info("Registering items.");

		Collections.addAll(SimpleModsLib.creativeModeTabItems,
			ANTIDOTE,
			SILENCE_TONIC,
			ALCOHOL_BUCKET,
			CONCENTRATED_ALCOHOL_BUCKET,
			PURE_ALCOHOL_BUCKET
		);
	}

	@Environment(EnvType.CLIENT)
	public static void registerTooltips()
	{
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			String tooltipKey;
			String tooltipDetailsKey;

			if (stack.getItem() == ModItems.ANTIDOTE)
			{
				tooltipKey = ANTIDOTE_TOOLTIP_KEY;
				tooltipDetailsKey = ANTIDOTE_TOOLTIP_DETAILS_KEY;
			}
			else if (stack.getItem() == ModItems.SILENCE_TONIC)
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