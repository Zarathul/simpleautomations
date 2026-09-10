package net.zarathul.simplemodslib.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zarathul.simplemodslib.ModItems;

public abstract class WrenchableBlock extends BaseEntityBlock implements IWrenchableBlock
{
	protected WrenchableBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		ItemStack heldStack = player.getItemInHand(hand);

		if (!heldStack.isEmpty() && heldStack.getItem() == ModItems.WRENCH)
		{
			if (!level.isClientSide())
			{
				handleToolWrenchClick(level, pos, player, heldStack);
			}

			return InteractionResult.SUCCESS;
		}

		return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
	}

	public abstract void handleToolWrenchClick(Level world, BlockPos pos, Player player, ItemStack equippedItemStack);
}
