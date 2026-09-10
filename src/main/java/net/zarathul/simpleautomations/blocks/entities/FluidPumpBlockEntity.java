package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.blocks.FluidPumpBlock;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simpleautomations.blocks.StillBlock;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.fluid.FluidHelper;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.fluid.IFluidHandler;
import org.jspecify.annotations.Nullable;

public class FluidPumpBlockEntity extends BlockEntity
{
	private static final String SOURCE = "source";
	private static final String DESTINATION = "destination";
	private static final String PROGRESS = "progress";

	private BlockPos sourcePos;
	private BlockPos destinationPos;
	private int progress;

	public FluidPumpBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		super(ModBlocks.FLUID_PUMP_ENTITY, worldPosition, blockState);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, FluidPumpBlockEntity blockEntity)
	{
		if (!(level instanceof ServerLevel serverLevel)) return;

		blockEntity.serverTick(serverLevel, state);
	}

	private void serverTick(ServerLevel level, BlockState state)
	{
		if (!state.getValue(FluidPumpBlock.POWERED_ON) || sourcePos == null || destinationPos == null)
		{
			resetProgress();
			return;
		}

		progress++;

		if (progress < FluidPumpBlock.BASE_PUMP_SPEED.ticks())
		{
			setChanged();
			return;
		}

		progress = 0;
		setChanged();

		BlockEntity sourceBlockEntity = level.getBlockEntity(sourcePos);
		BlockEntity destinationBlockEntity = level.getBlockEntity(destinationPos);

		if (sourceBlockEntity instanceof IFluidHandler source &&
			destinationBlockEntity instanceof IFluidHandler destination)
		{
			int speedSetting = getBlockState().getValue(FluidPumpBlock.SPEED);
			long drainAmount = FluidPumpBlock.MODIFIED_PUMP_SPEEDS[speedSetting];

			FluidHelper.transfer(source, destination, drainAmount);
		}
	}

	private void resetProgress()
	{
		if (progress != 0)
		{
			progress = 0;
			setChanged();
		}
	}

	@Override
	public void setChanged()
	{
		super.setChanged();

		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), StillBlock.UPDATE_ALL);
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);

		var sourceResult = input.getIntArray(SOURCE);
		sourcePos = (sourceResult.isPresent()) ? Utils.posFromArray(sourceResult.get()) : null;
		var destinationResult = input.getIntArray(DESTINATION);
		destinationPos = (destinationResult.isPresent()) ? Utils.posFromArray(destinationResult.get()) : null;

		progress = input.getIntOr(PROGRESS, 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		int[] sourceOutput = Utils.arrayFromPos(sourcePos);
		if (sourceOutput != null) output.putIntArray(SOURCE, sourceOutput);
		int[] destinationOutput = Utils.arrayFromPos(destinationPos);
		if (destinationOutput != null) output.putIntArray(DESTINATION, destinationOutput);

		output.putInt(PROGRESS, progress);
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

	public Direction getInputSide()
	{
		Direction facing = getBlockState().getValue(FluidPumpBlock.FACING);
		return facing.getCounterClockWise();
	}

	public Direction getOutputSide()
	{
		Direction facing = getBlockState().getValue(FluidPumpBlock.FACING);
		return facing.getClockWise();
	}

	public void updateConnections()
	{
		BlockPos oldSource 		= sourcePos;
		BlockPos oldDestination = destinationPos;

		sourcePos = ModBlocks.FLUID_PIPE.findConnectedFluidHandlerPos(level, worldPosition, worldPosition.relative(getInputSide()));
		destinationPos = ModBlocks.FLUID_PIPE.findConnectedFluidHandlerPos(level, worldPosition, worldPosition.relative(getOutputSide()));

		if (sourcePos != oldSource || destinationPos != oldDestination) setChanged();
	}

	public record PumpSpeed(int amount, int ticks) {};
}