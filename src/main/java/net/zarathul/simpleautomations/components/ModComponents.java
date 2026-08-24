package net.zarathul.simpleautomations.components;

import net.minecraft.core.component.DataComponentType;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simplemodslib.api.component.ComponentRegistrar;

public final class ModComponents
{
	private static final ComponentRegistrar REGISTRAR = new ComponentRegistrar(Simpleautomations.MOD_ID);
	public static final DataComponentType<AlcoholDistillationLevel> ALCOHOL_DISTILLATION_LEVEL = REGISTRAR.register("alcohol_distillation_level", AlcoholDistillationLevel.CODEC, AlcoholDistillationLevel.STREAM_CODEC);

	public static void init()
	{
		Simpleautomations.LOG.info("Registering components.");
	}
}
