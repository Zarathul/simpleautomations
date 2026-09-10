package net.zarathul.simplemodslib.api.fluid;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class FluidRegistrar
{
	private final String modId;

	public FluidRegistrar(String modId)
	{
		this.modId = modId;
	}

	public <T extends FlowingFluid> T register(String name, T fluid)
	{
		ResourceKey<Fluid> key = ResourceKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(modId, name));
		return Registry.register(BuiltInRegistries.FLUID, key, fluid);
	}
}