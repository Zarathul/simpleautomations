package net.zarathul.simpleautomations;

import net.fabricmc.api.ClientModInitializer;
import net.zarathul.simpleautomations.items.ModItems;

public class SimpleautomationsClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ModItems.registerTooltips();
	}
}
