package net.zarathul.simpleautomations.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zarathul.simpleautomations.components.FluidPipePlacementMode;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.fluid.IFluidHandler;
import org.jspecify.annotations.Nullable;

public class FluidPipeBlock extends Block
{
	public static final EnumProperty<PipeConnection> CONNECTION = EnumProperty.create("connection", PipeConnection.class);
	public static final BooleanProperty LINE_CONNECTED = BooleanProperty.create("line_connected");

	private static final VoxelShape[] SHAPES =
	{
		Block.box(4.0d, 0.0d, 4.0d, 12.0d, 12.0d, 12.0d),	// DOWN
		Block.box(4.0d, 4.0d, 4.0d, 12.0d, 16.0d, 12.0d),	// UP
		Block.box(4.0d, 4.0d, 0.0d, 12.0d, 12.0d, 12.0d),	// NORTH
		Block.box(4.0d, 4.0d, 4.0d, 12.0d, 12.0d, 16.0d),	// SOUTH
		Block.box(0.0d, 4.0d, 4.0d, 12.0d, 12.0d, 12.0d),	// WEST
		Block.box(4.0d, 4.0d, 4.0d, 16.0d, 12.0d, 12.0d)		// EAST
	};

	public FluidPipeBlock(Properties properties)
	{
		super(properties);

		registerDefaultState(getStateDefinition().any()
			.setValue(CONNECTION, PipeConnection.NORTH_TO_SOUTH)
			.setValue(LINE_CONNECTED, false)
		);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state)
	{
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(CONNECTION);
		builder.add(LINE_CONNECTED);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		Direction fromDirection = null;
		Direction toDirection = null;

		ItemStack pipeItem = context.getItemInHand();
		FluidPipePlacementMode.Mode mode = pipeItem.get(ModComponents.FLUID_PIPE).mode();

		switch (mode)
		{
			case SMART ->	// Try to automatically connect to existing pipes.
			{
				Direction[] connections = getConnectionDirections(context.getLevel(), context.getClickedPos());
				fromDirection = connections[0];
				toDirection   = connections[1];

				// Order is important here, because it is possible that only one connection direction was found and fromDirection already has a value, but toDirection does not.
				// In case there is no toDirection, get a direction towards the player. And if no connection was found previously, set fromDirection to the opposite of that.
				if (toDirection   == null) toDirection   = Utils.getRelativeDirection(context.getClickedPos(), context.getPlayer().blockPosition(), true);
				if (fromDirection == null) fromDirection = toDirection.getOpposite();
			}
			default ->
			{
				if (context.getPlayer().isCrouching())
				{
					fromDirection = context.getClickedFace().getOpposite();
					toDirection   = Utils.getRelativeDirection(context.getClickedPos(), context.getPlayer().blockPosition(), true);
				}
				else
				{
					fromDirection = context.getClickedFace();
					toDirection   = context.getClickedFace().getOpposite();
				}
			}
		}

		PipeConnection pipeConnection = PipeConnection.get(fromDirection, toDirection);
		if (pipeConnection == null) return super.getStateForPlacement(context);

		boolean lineConnected = isLineConnected(context.getLevel(), context.getClickedPos(), pipeConnection);

		return defaultBlockState().setValue(CONNECTION, pipeConnection).setValue(LINE_CONNECTED, lineConnected);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		PipeConnection connection = state.getValue(CONNECTION);

		return Shapes.or(SHAPES[connection.from().get3DDataValue()], SHAPES[connection.to().get3DDataValue()]);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
	{
		// TODO: remove / adjust to be used with wrench
		if (player.getItemInHand(player.getUsedItemHand()).isEmpty())
		{
			if (!state.getValue(LINE_CONNECTED))
			{
				PipeConnection pipeConnection = state.getValue(CONNECTION);
				int nextOrdinal = Math.floorMod(pipeConnection.ordinal() + (player.isCrouching() ? -1 : 1), PipeConnection.values().length);
				PipeConnection nextPipeConnection = PipeConnection.values()[nextOrdinal];
				boolean lineConnected = isLineConnected(level, pos, nextPipeConnection);

				level.setBlockAndUpdate(pos, state.setValue(CONNECTION, nextPipeConnection).setValue(LINE_CONNECTED, lineConnected));
			}
		}

		return (level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if (itemStack.getItem() instanceof BlockItem)
		{
			return InteractionResult.FAIL;
		}

		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston)
	{
		// TODO: incomplete handle pipe, fluid handler and pump destruction

		PipeConnection pipeConnection = state.getValue(CONNECTION);
		BlockPos fromPos = pos.relative(pipeConnection.from());
		BlockState fromState = level.getBlockState(fromPos);
		BlockPos toPos = pos.relative(pipeConnection.to());
		BlockState toState = level.getBlockState(toPos);

		boolean oldLineConnected = state.getValue(LINE_CONNECTED);
		boolean lineConnected = ((findConnectedFluidHandlerPos(level, pos, fromPos) != null) ||
								 (findConnectedFluidHandlerPos(level, pos, toPos) != null));

		if (lineConnected != oldLineConnected) level.setBlockAndUpdate(pos, state.setValue(LINE_CONNECTED, lineConnected));
	}

	private Direction[] getConnectionDirections(Level level, BlockPos pos)
	{
		Direction[] directions = new Direction[2];
		int dirIndex = 0;

		for (int i = 0; i < Direction.values().length; i++)
		{
			Direction dir = Direction.BY_ID.apply(i);
			BlockPos candidatePos = pos.offset(dir.getUnitVec3i());
			BlockState state = level.getBlockState(candidatePos);

			if ((state.is(ModBlocks.FLUID_PIPE) && state.getValue(CONNECTION).has(dir.getOpposite())) ||
				(state.is(ModBlocks.FLUID_PUMP) && ModBlocks.FLUID_PUMP.hasConnectorOnSide(state, dir)) ||
				(level.getBlockEntity(candidatePos) instanceof IFluidHandler)
			)
			{
				directions[dirIndex++] = dir;

				if (dirIndex >= directions.length) break;
			}
		}

		return directions;
	}

	private boolean isLineConnected(Level level, BlockPos pos, PipeConnection pipeConnection)
	{
		BlockPos fromPos = pos.relative(pipeConnection.from());
		BlockState fromState = level.getBlockState(fromPos);
		BlockPos toPos = pos.relative(pipeConnection.to());
		BlockState toState = level.getBlockState(toPos);

		return ((fromState.is(ModBlocks.FLUID_PIPE) && fromState.getValue(LINE_CONNECTED)) ||
				(toState.is(ModBlocks.FLUID_PIPE) && toState.getValue(LINE_CONNECTED)) ||
				(level.getBlockEntity(fromPos) instanceof IFluidHandler) ||
				(level.getBlockEntity(toPos) instanceof IFluidHandler)
		);
	}

	public BlockPos findConnectedFluidHandlerPos(Level level, BlockPos origin, BlockPos startingPos)
	{
		BlockPos lastPos = origin;
		BlockPos posToCheck = startingPos;

		do
		{
			BlockState state = level.getBlockState(posToCheck);

			if (state.is(ModBlocks.FLUID_PIPE) && state.getValue(FluidPipeBlock.LINE_CONNECTED))
			{
				Direction fromDirection = Utils.getRelativeDirection(posToCheck, lastPos);
				PipeConnection pipeConnection = state.getValue(FluidPipeBlock.CONNECTION);
				Direction toDirection = pipeConnection.getOtherDirection(fromDirection);

				lastPos = posToCheck;
				posToCheck = (toDirection != null) ? posToCheck.relative(toDirection) : null;
			}
			else if (level.getBlockEntity(posToCheck) instanceof IFluidHandler)
			{
				return posToCheck;
			}
			else
			{
				posToCheck = null;
			}
		}
		while (posToCheck != null);

		return null;
	}
}