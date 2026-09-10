package net.zarathul.simplemodslib.api.particles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ParticleRegistrar
{
	private String modId;

	public ParticleRegistrar(String modId)
	{
		this.modId = modId;
	}

	public <T extends SimpleParticleType> T register(String name, T type)
	{
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(modId, name), type);
	}

	public <T extends ParticleOptions> void registerClientSideProvider(ParticleType<T> type, ParticleProviderRegistry.PendingParticleProvider<T> constructor)
	{
		ParticleProviderRegistry.getInstance().register(type, constructor);
	}
}
