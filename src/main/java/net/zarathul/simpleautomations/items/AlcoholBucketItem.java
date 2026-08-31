package net.zarathul.simpleautomations.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simpleautomations.fluids.ModFluids;
import org.jspecify.annotations.Nullable;

public class AlcoholBucketItem extends BucketItem
{
	public AlcoholBucketItem(Fluid content, Properties properties)
	{
		super(content, properties);
	}

	@Override
	public void checkExtraContent(@Nullable LivingEntity user, Level level, ItemStack itemStack, BlockPos pos)
	{
		var distillationLevel = itemStack.get(ModComponents.ALCOHOL_DISTILLATION_LEVEL);
		if (distillationLevel == null) return;

		BlockState oldBlockState = level.getBlockState(pos);
		if (!oldBlockState.is(ModBlocks.ALCOHOL)) return;

		BlockState newBlockState = oldBlockState.setValue(ModFluids.DISTILLATION_LEVEL, distillationLevel.level());
		if (newBlockState != oldBlockState) level.setBlock(pos, newBlockState, Block.UPDATE_ALL_IMMEDIATE);
	}
}