package net.zarathul.simpleautomations.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simpleautomations.common.Colors;
import net.zarathul.simplemodslib.api.particles.ParticleRegistrar;

public final class ModParticles
{
	private static final ParticleRegistrar REGISTRAR = new ParticleRegistrar(SimpleAutomations.MOD_ID);

	public static SimpleParticleType ALCOHOL_EVAPORATION = REGISTRAR.register("alcohol_evaporation", FabricParticleTypes.simple());

	public static void init()
	{
		SimpleAutomations.LOG.info("Registering particles.");
	}

	public static void registerClient()
	{
		REGISTRAR.registerClientSideProvider(ALCOHOL_EVAPORATION, spriteSet -> new AlcoholFluidEvaporationParticle.Provider(spriteSet, Colors.ALCOHOL_NORMAL, Colors.ALCOHOL_CONCENTRATED, Colors.ALCOHOL_PURE));
	}
}
