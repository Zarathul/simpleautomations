package net.zarathul.simpleautomations.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simpleautomations.blocks.entities.MultiBlockFluidInventory;
import net.zarathul.simpleautomations.blocks.entities.MultiBlockInventory;
import net.zarathul.simpleautomations.blocks.entities.StillCoreBlockEntity;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simplemodslib.api.block.BlockRegistrar;

public final class ModBlocks
{
	private static final BlockRegistrar REGISTRAR = new BlockRegistrar(SimpleAutomations.MOD_ID);

	public static final AlcoholBlock ALCOHOL = REGISTRAR.register("alcohol", properties -> new AlcoholBlock(ModFluids.ALCOHOL_STILL, properties), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.GOLD));
	public static final StillBlock STILL = REGISTRAR.register("still", StillBlock::new, Block.Properties.of().sound(SoundType.METAL).pushReaction(PushReaction.BLOCK));

	public static final BlockEntityType<MultiBlockInventory> MULTI_BLOCK_INVENTORY = REGISTRAR.register("multi_block_inventory", MultiBlockInventory::new, STILL);
	public static final BlockEntityType<MultiBlockFluidInventory> MULTI_BLOCK_FLUID_INVENTORY = REGISTRAR.register("multi_block_fluid_inventory", MultiBlockFluidInventory::new, STILL);
	public static final BlockEntityType<StillCoreBlockEntity> STILL_CORE = REGISTRAR.register("still_core", StillCoreBlockEntity::new, STILL);

	public static void init()
	{
		SimpleAutomations.LOG.info("Registering blocks.");
	}
}
