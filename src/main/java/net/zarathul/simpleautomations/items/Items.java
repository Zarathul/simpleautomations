package net.zarathul.simpleautomations.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simplemodslib.SimpleModsLib;

import java.util.Collections;

public class Items
{
	public static final SilenceTonicItem silenceTonicItem;
	public static final String SILENCE_TONIC_NAME = "silence_tonic";
	public static final Identifier SILENCE_TONIC_ID = Simpleautomations.modId(SILENCE_TONIC_NAME);

	public static final AntidoteItem antidoteItem;
	public static final String ANTIDOTE_NAME = "antidote";
	public static final Identifier ANTIDOTE_ID = Simpleautomations.modId(ANTIDOTE_NAME);

	static
	{
		silenceTonicItem = Registry.register(BuiltInRegistries.ITEM, SILENCE_TONIC_ID, new SilenceTonicItem(createItemKey(SILENCE_TONIC_NAME)));
		antidoteItem = Registry.register(BuiltInRegistries.ITEM, ANTIDOTE_ID, new AntidoteItem(createItemKey(ANTIDOTE_NAME)));

		Collections.addAll(SimpleModsLib.creativeModeTabItems,
			silenceTonicItem,
			antidoteItem
		);
	}

	public static void initialize() {}

	@Environment(EnvType.CLIENT)
	public static void registerTooltips()
	{
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			if (stack.getItem() == Items.antidoteItem)
			{
				Items.antidoteItem.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
			else if (stack.getItem() == Items.silenceTonicItem)
			{
				Items.silenceTonicItem.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
		});
	}

	public static ResourceKey<Item> createItemKey(String name)
	{
		return ResourceKey.create(Registries.ITEM, Simpleautomations.modId(name));
	}
}
