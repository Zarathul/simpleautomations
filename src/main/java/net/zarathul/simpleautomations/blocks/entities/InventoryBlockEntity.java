package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.blocks.ModBlocks;

public class InventoryBlockEntity extends BlockEntity implements Container
{
	private final NonNullList<ItemStack> items;
	private final int maxStackSize;

	public InventoryBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		this(worldPosition, blockState, 0, 0);
	}

	public InventoryBlockEntity(BlockPos worldPosition, BlockState blockState, int size, int maxStackSize)
	{
		super(ModBlocks.BASIC_INVENTORY_ENTITY, worldPosition, blockState);

		items = NonNullList.withSize(size, ItemStack.EMPTY);
		this.maxStackSize = maxStackSize;
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);
		ContainerHelper.loadAllItems(input, items);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		ContainerHelper.saveAllItems(output, items);
		super.saveAdditional(output);
	}

	// Container

	@Override
	public int getContainerSize()
	{
		return items.size();
	}

	@Override
	public boolean isEmpty()
	{
		for (ItemStack slot : items)
		{
			if (!slot.isEmpty()) return false;
		}

		return true;
	}

	@Override
	public ItemStack getItem(int slot)
	{
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int count)
	{
		ItemStack removedItem = ContainerHelper.removeItem(items, slot, count);
		if (!removedItem.isEmpty()) setChanged();

		return removedItem;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot)
	{
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack itemStack)
	{
		items.set(slot, itemStack);
		itemStack.limitSize(maxStackSize);
	}

	@Override
	public boolean stillValid(Player player)
	{
		return true;
	}

	@Override
	public void clearContent()
	{
		items.clear();
	}

	@Override
	public int getMaxStackSize()
	{
		return maxStackSize;
	}
}