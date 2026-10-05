package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.fluid.IFluidHandler;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public class MultiBlockFluidInventory extends BlockEntity implements IFluidHandler
{
	private static final String CAPACITY = "capacity";
	private static final String CONNECTOR_FACING = "connector_facing";

	private FluidStack fluid = FluidStack.empty();
	private long capacity = FluidStack.BUCKET_VOLUME * 32;
	private Direction connectorFacing;

	public MultiBlockFluidInventory(BlockPos worldPosition, BlockState blockState, long capacity, Direction connectorFacing)
	{
		this(worldPosition, blockState);

		this.capacity = capacity;
		this.connectorFacing = connectorFacing;
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
		capacity = input.getLongOr(CAPACITY, 0);
		connectorFacing = Direction.from3DDataValue(input.getIntOr(CONNECTOR_FACING, 0));
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		fluid.save(output);
		output.putLong(CAPACITY, capacity);
		output.putInt(CONNECTOR_FACING, connectorFacing.get3DDataValue());
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
		if (!fluid.isEmpty()) fluid.setAmount(Math.min(fluid.getAmount(), capacity));

		setChanged();
	}

	@Override
	public long getCapacity()
	{
		return capacity;
	}

	@Override
	public EnumSet<Direction> getConnectableSides()
	{
		return EnumSet.of(connectorFacing);
	}

	@Override
	public void fluidChanged(FluidChange change)
	{
		setChanged();
	}
}