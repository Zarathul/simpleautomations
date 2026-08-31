package net.zarathul.simpleautomations.particles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simpleautomations.common.Colors;

public final class ModParticles
{
	public static SimpleParticleType ALCOHOL_EVAPORATION = register("alcohol_evaporation", FabricParticleTypes.simple());

	public static void init()
	{
		SimpleAutomations.LOG.info("Registering particles.");
	}

	public static SimpleParticleType register(String name, SimpleParticleType type)
	{
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, SimpleAutomations.modId(name), type);
	}

	public static void registerClient()
	{
		var providerRegistry = ParticleProviderRegistry.getInstance();

		providerRegistry.register(ALCOHOL_EVAPORATION, spriteSet -> new AlcoholFluidEvaporationParticle.Provider(spriteSet, Colors.ALCOHOL_NORMAL, Colors.ALCOHOL_CONCENTRATED, Colors.ALCOHOL_PURE));
	}
}
