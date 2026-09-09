package net.zarathul.simpleautomations.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.zarathul.simpleautomations.components.FluidPipePlacementMode;
import net.zarathul.simpleautomations.components.ModComponents;

public class FluidPipeItem extends BlockItem
{
	public FluidPipeItem(Block block, Properties properties)
	{
		super(block, properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand)
	{
		if (!level.isClientSide())
		{
			ItemStack itemStack = player.getItemInHand(hand);
			FluidPipePlacementMode component = itemStack.get(ModComponents.FLUID_PIPE);

			if (component != null)
			{
				// Cycle the placement mode.
				int newModeOrdinal = Math.floorMod(component.mode().ordinal() + ((player.isCrouching()) ? -1 : 1), FluidPipePlacementMode.Mode.values().length);
				FluidPipePlacementMode.Mode newMode = FluidPipePlacementMode.Mode.values()[newModeOrdinal];
				itemStack.set(ModComponents.FLUID_PIPE, new FluidPipePlacementMode(newMode));
				player.sendSystemMessage(Component.literal(newMode.name()));

				return InteractionResult.SUCCESS_SERVER;
			}
		}

		return InteractionResult.SUCCESS;
	}
}
