package net.zarathul.simpleautomations.fluids;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simpleautomations.common.Colors;
import net.zarathul.simpleautomations.common.DistillationLevel;

import java.util.Set;

public final class ModFluids
{
	public static final EnumProperty<DistillationLevel> DISTILLATION_LEVEL = EnumProperty.create("distillation_level", DistillationLevel.class);

	public static final ResourceKey<Fluid> ALCOHOL_STILL_KEY = createKey("alcohol");
	public static final ResourceKey<Fluid> ALCOHOL_FLOWING_KEY = createKey("flowing_alcohol");
	public static final FlowingFluid ALCOHOL_STILL = register(ALCOHOL_STILL_KEY, new AlcoholFluid.Source());
	public static final FlowingFluid ALCOHOL_FLOWING = register(ALCOHOL_FLOWING_KEY, new AlcoholFluid.Flowing());

	private static FlowingFluid register(ResourceKey<Fluid> key, FlowingFluid fluid)
	{
		return Registry.register(BuiltInRegistries.FLUID, key, fluid);
	}

	public static void init()
	{
		Simpleautomations.LOG.info("Registering fluids.");
	}

	private static ResourceKey<Fluid> createKey(String name)
	{
		return ResourceKey.create(Registries.FLUID, Simpleautomations.modId(name));
	}

	@Environment(EnvType.CLIENT)
	public static void registerRendering()
	{
		FluidRenderingRegistry.register(
			ALCOHOL_STILL,
			ALCOHOL_FLOWING,
			new FluidModel.Unbaked(
				new Material(Identifier.withDefaultNamespace("block/water_still")),
				new Material(Identifier.withDefaultNamespace("block/water_flow")),
				new Material(Identifier.withDefaultNamespace("block/water_overlay")),
				new BlockTintSource()
				{
					@Override
					public Set<Property<?>> relevantProperties()
					{
						return Set.of(AlcoholFluid.DISTILLATION_LEVEL);
					}

					@Override
					public int color(BlockState state)
					{
						var distillationLevel = state.getFluidState().getOptionalValue(AlcoholFluid.DISTILLATION_LEVEL);
						return (distillationLevel.isPresent())
							   ? switch (distillationLevel.get())
							   {
								   case CONCENTRATED  -> Colors.ALCOHOL_CONCENTRATED;
								   case PURE  		  -> Colors.ALCOHOL_PURE;
								   default 			  -> Colors.ALCOHOL_NORMAL;
							   }
							   : Colors.ALCOHOL_NORMAL;
					}
				}
			)
		);
	}
}