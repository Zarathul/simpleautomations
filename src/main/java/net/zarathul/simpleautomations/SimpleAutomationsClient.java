package net.zarathul.simpleautomations;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.zarathul.simpleautomations.client.AlcoholDistillationLevelProperty;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simpleautomations.items.ModItems;
import net.zarathul.simpleautomations.particles.ModParticles;

public class SimpleAutomationsClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ModItems.registerTooltips();
		ModFluids.registerRendering();
		ModParticles.registerClient();

		// Custom property for alcohol bucket (alcohol_bucket.json).
		SelectItemModelProperties.ID_MAPPER.put(SimpleAutomations.modId("alcohol_distillation_level"), AlcoholDistillationLevelProperty.TYPE);
	}
}
