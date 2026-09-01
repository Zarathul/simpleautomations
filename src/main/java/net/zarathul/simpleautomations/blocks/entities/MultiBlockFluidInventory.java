package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.fluid.IFluidHandler;

public class MultiBlockFluidInventory extends BlockEntity implements IFluidHandler
{
	private static final String CAPACITY = "capacity";

	private FluidStack fluid = FluidStack.empty();
	private int capacity = FluidStack.BUCKET_VOLUME * 32;

	public MultiBlockFluidInventory(BlockPos worldPosition, BlockState blockState, int capacity)
	{
		this(worldPosition, blockState);

		this.capacity = capacity;
	}

	public MultiBlockFluidInventory(BlockPos worldPosition, BlockState blockState)
	{
		super(ModBlocks.MULTI_BLOCK_FLUID_INVENTORY, worldPosition, blockState);
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);

		fluid.load(input);
		capacity = input.getIntOr(CAPACITY, 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		fluid.save(output);
		output.putInt(CAPACITY, capacity);
	}

	@Override
	public FluidStack getFluid()
	{
		return fluid;
	}

	@Override
	public void setFluid(FluidStack newFluid)
	{
		fluid = newFluid.copy();
		// limit the stored fluid to the capacity
		if (!fluid.isEmpty()) fluid.setAmount(Math.min(fluid.getAmount(), capacity));
	}

	@Override
	public int getCapacity()
	{
		return capacity;
	}

	@Override
	public void fluidChanged(FluidChange change)
	{
		setChanged();
	}
}