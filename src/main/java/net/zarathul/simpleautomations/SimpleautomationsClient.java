package net.zarathul.simpleautomations;

import net.fabricmc.api.ClientModInitializer;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simpleautomations.items.ModItems;
import net.zarathul.simpleautomations.particles.ModParticles;

public class SimpleautomationsClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ModItems.registerTooltips();
		ModFluids.registerRendering();
		ModParticles.registerClient();
	}
}
