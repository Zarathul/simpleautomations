package net.zarathul.simpleautomations.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simpleautomations.common.DistillationLevel;
import net.zarathul.simpleautomations.components.AlcoholDistillationLevel;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simpleautomations.components.Tonic;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simplemodslib.SimpleModsLib;
import net.zarathul.simplemodslib.api.fluid.FluidContainerComponent;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.item.ItemRegistrar;

import java.util.Collections;

public final class ModItems
{
	public static final String TONIC_NAME = "tonic";
	public static final String ALCOHOL_BUCKET_NAME = "alcohol_bucket";

	private static final ItemRegistrar REGISTRAR = new ItemRegistrar(SimpleAutomations.MOD_ID);

	public static final TonicItem EMPTY_TONIC = REGISTRAR.register("empty_tonic", TonicItem::new,
		new Item.Properties()
			.stacksTo(16)
			.component(ModComponents.TONIC, new Tonic(Tonic.Type.EMPTY))
			.modelId(SimpleAutomations.modId(TONIC_NAME))
	);
	public static final TonicItem ANTIDOTE = REGISTRAR.register("antidote", TonicItem::new,
		new Item.Properties()
			.stacksTo(16)
			.component(ModComponents.TONIC, new Tonic(Tonic.Type.ANTIDOTE))
			.modelId(SimpleAutomations.modId(TONIC_NAME))
	);
	public static final TonicItem SILENCE_TONIC = REGISTRAR.register("silence_tonic", TonicItem::new,
		new Item.Properties()
			.stacksTo(16)
			.component(ModComponents.TONIC, new Tonic(Tonic.Type.SILENCE))
			.modelId(SimpleAutomations.modId(TONIC_NAME))
	);
	public static final TonicItem BINDING_TONIC = REGISTRAR.register("binding_tonic", TonicItem::new,
		new Item.Properties()
			.stacksTo(16)
			.component(ModComponents.TONIC, new Tonic(Tonic.Type.BINDING))
			.modelId(SimpleAutomations.modId(TONIC_NAME))
	);
	public static final AlcoholBucketItem ALCOHOL_BUCKET = REGISTRAR.register(ALCOHOL_BUCKET_NAME, properties -> new AlcoholBucketItem(ModFluids.ALCOHOL_STILL, properties),
		new Item.Properties()
			.craftRemainder(Items.BUCKET)
			.stacksTo(1)
			.component(net.zarathul.simplemodslib.ModComponents.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(
				new FluidStack(ModFluids.ALCOHOL_STILL, FluidStack.BUCKET_VOLUME).with(ModComponents.ALCOHOL_DISTILLATION_LEVEL, new AlcoholDistillationLevel(DistillationLevel.NORMAL)),
				FluidStack.BUCKET_VOLUME)
			)
	);
	public static final AlcoholBucketItem CONCENTRATED_ALCOHOL_BUCKET = REGISTRAR.register("concentrated_alcohol_bucket", properties -> new AlcoholBucketItem(ModFluids.ALCOHOL_STILL, properties),
		new Item.Properties()
			.craftRemainder(Items.BUCKET)
			.stacksTo(1)
			.component(net.zarathul.simplemodslib.ModComponents.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(
				new FluidStack(ModFluids.ALCOHOL_STILL, FluidStack.BUCKET_VOLUME).with(ModComponents.ALCOHOL_DISTILLATION_LEVEL, new AlcoholDistillationLevel(DistillationLevel.CONCENTRATED)),
				FluidStack.BUCKET_VOLUME)
			)
			.modelId(SimpleAutomations.modId(ALCOHOL_BUCKET_NAME))
	);
	public static final AlcoholBucketItem PURE_ALCOHOL_BUCKET = REGISTRAR.register("pure_alcohol_bucket", properties -> new AlcoholBucketItem(ModFluids.ALCOHOL_STILL, properties),
		new Item.Properties()
			.craftRemainder(Items.BUCKET)
			.stacksTo(1)
			.component(net.zarathul.simplemodslib.ModComponents.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(
				new FluidStack(ModFluids.ALCOHOL_STILL, FluidStack.BUCKET_VOLUME).with(ModComponents.ALCOHOL_DISTILLATION_LEVEL, new AlcoholDistillationLevel(DistillationLevel.PURE)),
				FluidStack.BUCKET_VOLUME)
			)
			.modelId(SimpleAutomations.modId(ALCOHOL_BUCKET_NAME))
	);

	public static final BlockItem STILL = REGISTRAR.register("still", ModBlocks.STILL, BlockItem::new, new Item.Properties().stacksTo(64));

	public static void init()
	{
		SimpleAutomations.LOG.info("Registering items.");

		Collections.addAll(SimpleModsLib.creativeModeTabItems,
			EMPTY_TONIC,
			ANTIDOTE,
			SILENCE_TONIC,
			BINDING_TONIC,
			ALCOHOL_BUCKET,
			CONCENTRATED_ALCOHOL_BUCKET,
			PURE_ALCOHOL_BUCKET,
			STILL
		);
	}

	public static void registerTooltips()
	{
		REGISTRAR.registerTooltips();
	}
}