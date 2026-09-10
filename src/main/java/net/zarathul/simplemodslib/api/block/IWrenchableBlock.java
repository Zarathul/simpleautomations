package net.zarathul.simplemodslib.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IWrenchableBlock
{
	/**
	 * Handles clicks with wrenches on a block.
	 *
	 * @param state
	 * The state of the clicked block.
	 * @param level
	 * The level.
	 * @param pos
	 * The blocks' coordinates.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the block.
	 */
	void handleToolWrenchClick(BlockState state, Level level, BlockPos pos, Player player, ItemStack equippedItemStack);
}