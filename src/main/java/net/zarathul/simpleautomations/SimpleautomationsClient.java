package net.zarathul.simpleautomations;

import net.fabricmc.api.ClientModInitializer;
import net.zarathul.simpleautomations.items.Items;

public class SimpleautomationsClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		Items.registerTooltips();
	}
}
