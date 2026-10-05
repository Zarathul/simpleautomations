package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.blocks.FluidGrateBlock;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.fluid.IFluidHandler;
import net.zarathul.simplemodslib.api.fluid.IModFluidBlock;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public class FluidGrateBlockEntity extends BlockEntity implements IFluidHandler
{
	public static final int SEARCH_OFFSET = 7;

	private FluidStack fluid = FluidStack.empty();

	public FluidGrateBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		super(ModBlocks.FLUID_GRATE_ENTITY, worldPosition, blockState);
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);

		fluid.load(input);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		fluid.save(output);
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return saveWithoutMetadata(registries);
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
		if (!fluid.isEmpty()) fluid.setAmount(Math.min(fluid.getAmount(), getCapacity()));

		setChanged();
	}

	@Override
	public long getCapacity()
	{
		return FluidStack.BUCKET_VOLUME;
	}

	@Override
	public EnumSet<Direction> getConnectableSides()
	{
		return EnumSet.of(getBlockState().getValue(FluidGrateBlock.FACING).getOpposite());
	}

	@Override
	public void fluidChanged(FluidChange change)
	{
		setChanged();
	}

	@Override
	public long fill(FluidStack fillFluid)
	{
		long fillAmount = IFluidHandler.super.fill(fillFluid);

		if (getRemainingCapacity() == 0)	// If the grate is completely filled, push out a fluid block if possible.
		{
			SearchResult result = findEmptyPos();

			if (result.success())
			{
				BlockState newFluidBlockState = fluid.getFluid().defaultFluidState().createLegacyBlock();

				if (newFluidBlockState.getBlock() instanceof IModFluidBlock modFluidBlock)
				{
					// The mod fluid needs to override "createLegacyBlock(FluidState fluidState)" for this to work.
					newFluidBlockState = modFluidBlock.getStateFromFluidStack(fillFluid).createLegacyBlock();
				}

				setFluid(FluidStack.empty());
				level.setBlock(result.pos(), newFluidBlockState, Block.UPDATE_ALL);
			}
		}

		return fillAmount;
	}

	@Override
	public FluidStack drain(FluidStack drainFluid)
	{
		FluidStack drainedFluid = IFluidHandler.super.drain(drainFluid);

		if (fluid.isEmpty())	// If the grate is fully drained, suck in another fluid block if possible.
		{
			SearchResult result = findFluid();

			if (result.success())
			{
				FluidStack fluid = fluidFromResult(result);
				setFluid(fluid);
				level.setBlock(result.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
		}

		return drainedFluid;
	}

	private FluidStack fluidFromResult(SearchResult result)
	{
		if (!result.success()) return FluidStack.empty();

		BlockState fluidBlockState = level.getBlockState(result.pos());
		FluidStack fluid = (fluidBlockState.getBlock() instanceof IModFluidBlock modFluidBlock) ?
						   modFluidBlock.getFluidStackFromState(result.fluidState()) :
						   new FluidStack(result.fluidState.getType(), FluidStack.BUCKET_VOLUME);

		return fluid;
	}

	private SearchResult findFluid()
	{
		return findFluidOrEmptyPos(true);
	}

	private SearchResult findEmptyPos()
	{
		return findFluidOrEmptyPos(false);
	}

	// For now, search in a fixed cuboid above or below the grate, depending on its facing.
	private SearchResult findFluidOrEmptyPos(boolean findFluid)
	{
		int minX = worldPosition.getX() - SEARCH_OFFSET;
		int maxX = worldPosition.getX() + SEARCH_OFFSET;
		int minZ = worldPosition.getZ() - SEARCH_OFFSET;
		int maxZ = worldPosition.getZ() + SEARCH_OFFSET;

		int startY;
		int endY;

		if (getBlockState().getValue(FluidGrateBlock.FACING) == Direction.UP)
		{
			startY  = worldPosition.relative(Direction.UP).getY();
			endY    = startY + SEARCH_OFFSET;
		}
		else
		{
			endY    = worldPosition.relative(Direction.DOWN).getY();
			startY  = endY - SEARCH_OFFSET;
		}

		for (int y = startY; y <= endY; y++)
		{
			for (int x = minX; x <= maxX; x++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					BlockPos currentPos = new BlockPos(x, y, z);
					FluidState fluidState = level.getFluidState(currentPos);

					if (findFluid)
					{
						if (!fluidState.isEmpty() && fluidState.isSource()) return SearchResult.success(currentPos, fluidState);
					}
					else if (!fluidState.isSource())
					{
						BlockState blockState = level.getBlockState(currentPos);
						if (blockState.canBeReplaced())
						{
							return SearchResult.success(currentPos, fluidState);
						}
					}
				}
			}
		}

		return SearchResult.failure();
	}

	private record SearchResult(boolean success, BlockPos pos, FluidState fluidState)
	{
		public static SearchResult success(BlockPos pos, FluidState fluidState)
		{
			return new SearchResult(true, pos, fluidState);
		}

		public static SearchResult failure()
		{
			return new SearchResult(false, null, null);
		}
	}
}