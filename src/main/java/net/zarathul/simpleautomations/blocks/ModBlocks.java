package net.zarathul.simpleautomations.blocks;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simplemodslib.api.block.BlockRegistrar;

public final class ModBlocks
{
	private static final BlockRegistrar REGISTRAR = new BlockRegistrar(Simpleautomations.MOD_ID);

	public static final AlcoholBlock ALCOHOL = REGISTRAR.register("alcohol", properties -> new AlcoholBlock(ModFluids.ALCOHOL_STILL, properties), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.GOLD));
	public static final StillBlock STILL = REGISTRAR.register("still", StillBlock::new);

	public static void init()
	{
		Simpleautomations.LOG.info("Registering blocks.");
	}
}
