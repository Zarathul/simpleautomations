package net.zarathul.simpleautomations.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simpleautomations.blocks.entities.InventoryBlockEntity;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simplemodslib.api.block.BlockRegistrar;

public final class ModBlocks
{
	private static final BlockRegistrar REGISTRAR = new BlockRegistrar(Simpleautomations.MOD_ID);

	public static final AlcoholBlock ALCOHOL = REGISTRAR.register("alcohol", properties -> new AlcoholBlock(ModFluids.ALCOHOL_STILL, properties), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.GOLD));
	public static final StillBlock STILL = REGISTRAR.register("still", StillBlock::new, Block.Properties.of().sound(SoundType.METAL).pushReaction(PushReaction.BLOCK));

	public static final BlockEntityType<InventoryBlockEntity> BASIC_INVENTORY_ENTITY = REGISTRAR.register("basic_inventory", InventoryBlockEntity::new, STILL);

	public static void init()
	{
		Simpleautomations.LOG.info("Registering blocks.");
	}
}
