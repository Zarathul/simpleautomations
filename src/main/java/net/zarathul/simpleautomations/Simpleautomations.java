package net.zarathul.simpleautomations;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import net.zarathul.simpleautomations.items.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simpleautomations implements ModInitializer
{
	public static final String MOD_ID = "simpleautomations";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		Items.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
