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

public class MultiBlockInventory extends BlockEntity implements Container
{
	private static final String MAX_STACK_SIZE = "maxStackSize";
	private static final String SLOTS = "slots";

	private NonNullList<ItemStack> items;
	private int maxStackSize;
	private int slots;

	public MultiBlockInventory(BlockPos worldPosition, BlockState blockState)
	{
		this(worldPosition, blockState, 0, 0);
	}

	public MultiBlockInventory(BlockPos worldPosition, BlockState blockState, int slots, int maxStackSize)
	{
		super(ModBlocks.MULTI_BLOCK_INVENTORY, worldPosition, blockState);

		items = NonNullList.withSize(slots, ItemStack.EMPTY);
		this.maxStackSize = maxStackSize;
		this.slots = slots;
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);

		maxStackSize = input.getIntOr(MAX_STACK_SIZE, 0);
		slots = input.getIntOr(SLOTS, 0);
		items = NonNullList.withSize(slots, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		output.putInt(MAX_STACK_SIZE, maxStackSize);
		output.putInt(SLOTS, slots);
		ContainerHelper.saveAllItems(output, items);
	}

	public void takeItem(Player player)
	{
		for (int i = 0; i < getContainerSize(); i++)
		{
			ItemStack itemInSlot = getItem(i);

			if (!itemInSlot.isEmpty())
			{
				ItemStack removedItem = removeItem(i, itemInSlot.count());

				if (!player.getInventory().add(removedItem))
				{
					player.drop(removedItem, false);
				}

				setChanged();
				break;
			}
		}
	}

	public void putItem(ItemStack itemStack)
	{
		ItemStack originalStack = itemStack.copy();

		for (int i = 0; i < getContainerSize(); i++)
		{
			ItemStack itemInSlot = getItem(i);
			if (itemInSlot.isEmpty())
			{
				int amountToStore = Math.max(itemStack.count(), getMaxStackSize());
				setItem(i, itemStack.copy());
				itemStack.consume(amountToStore, null);
			}
			else if (itemInSlot.count() < getMaxStackSize() && ItemStack.isSameItemSameComponents(itemStack, itemInSlot))
			{
				int spaceLeftInSlot = getMaxStackSize() - itemInSlot.count();
				int amountToStore = Math.min(itemStack.count(), spaceLeftInSlot);

				itemStack.consume(amountToStore, null);
				itemInSlot.setCount(itemInSlot.count() + amountToStore);
			}

			if (itemStack.isEmpty()) break;
		}

		if (!ItemStack.isSameItemSameComponents(itemStack, originalStack)) setChanged();
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