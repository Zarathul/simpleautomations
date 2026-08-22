package net.zarathul.simpleautomations;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.zarathul.simpleautomations.items.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simpleautomations implements ModInitializer
{
	public static final String MOD_ID = "simpleautomations";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		ModItems.init();
	}

	public static Identifier modId(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
}
