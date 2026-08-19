package net.zarathul.simpleautomations.items;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.zarathul.simpleautomations.Simpleautomations;

public class Items
{
	private static SilenceTonicItem silenceTonicItem;
	public static SilenceTonicItem silenceTonicItem() { return silenceTonicItem; }
	public static final String SILENCE_TONIC_NAME = "silence_tonic";
	public static final Identifier SILENCE_TONIC_ID = Simpleautomations.id(SILENCE_TONIC_NAME);

	private static AntidoteItem antidoteItem;
	public static AntidoteItem antidoteItem() { return antidoteItem; }
	public static final String ANTIDOTE_NAME = "antidote";
	public static final Identifier ANTIDOTE_ID = Simpleautomations.id(ANTIDOTE_NAME);

	public static void register()
	{
		silenceTonicItem = Registry.register(BuiltInRegistries.ITEM, SILENCE_TONIC_ID, new SilenceTonicItem(createItemKey(SILENCE_TONIC_NAME)));
		antidoteItem = Registry.register(BuiltInRegistries.ITEM, ANTIDOTE_ID, new AntidoteItem(createItemKey(ANTIDOTE_NAME)));
	}

	public static ResourceKey<Item> createItemKey(String name)
	{
		return ResourceKey.create(Registries.ITEM, Simpleautomations.id(name));
	}
}
