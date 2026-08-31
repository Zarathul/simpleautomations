package net.zarathul.simpleautomations;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simpleautomations.items.ModItems;
import net.zarathul.simpleautomations.particles.ModParticles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAutomations implements ModInitializer
{
	public static final String MOD_ID = "simpleautomations";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		ModComponents.init();
		ModFluids.init();
		ModItems.init();
		ModBlocks.init();
		ModParticles.init();
	}

	public static Identifier modId(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
}
