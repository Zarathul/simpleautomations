package net.zarathul.simplemodslib.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IWrenchableBlock
{
	/**
	 * Handles clicks with wrenches on a block.
	 *
	 * @param world
	 * The world.
	 * @param pos
	 * The blocks' coordinates.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the block.
	 */
	void handleToolWrenchClick(Level world, BlockPos pos, Player player, ItemStack equippedItemStack);
}