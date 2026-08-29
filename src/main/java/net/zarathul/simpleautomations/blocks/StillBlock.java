package net.zarathul.simpleautomations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simplemodslib.Utils;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class StillBlock extends Block
{
	public static final MapCodec<StillBlock> CODEC = simpleCodec(StillBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<MultiBlockPartType> PART = EnumProperty.create("part", MultiBlockPartType.class);
	public static final BooleanProperty POWERED_ON = BooleanProperty.create("powered_on");
	public static final BooleanProperty FUEL_HATCH_OPEN = BooleanProperty.create("fuel_hatch_open");
	public static final BooleanProperty PRESSURE_RELEASE_PULLED = BooleanProperty.create("pressure_release_pulled");
	public static final IntegerProperty PRESSURE = IntegerProperty.create("pressure", 0, 7);

	private static final MultiBlockPart[][] PARTS;

	static
	{
		int northIndex = Direction.NORTH.get2DDataValue();
		PARTS = new MultiBlockPart[4][];
		PARTS[northIndex] = new MultiBlockPart[]
		{
			new MultiBlockPart(new Vec3i( 0, 0, -1), MultiBlockPartType.FUEL_INPUT),	// North: Fuel Hatch
			new MultiBlockPart(new Vec3i( 1, 0, -1), MultiBlockPartType.INTERACTABLE),	// North, East: Power Lever
			new MultiBlockPart(new Vec3i(-1, 0, -1), MultiBlockPartType.INTERACTABLE),	// North, West: Power Gauge + Pressure Release

			new MultiBlockPart(new Vec3i( 0, 1, -1), MultiBlockPartType.PROXY),		// North, Up: Proxy
			new MultiBlockPart(new Vec3i( 1, 1, -1), MultiBlockPartType.PROXY),		// North, Up, East: Proxy
			new MultiBlockPart(new Vec3i(-1, 1, -1), MultiBlockPartType.PROXY),		// North, Up, West: Proxy

			new MultiBlockPart(new Vec3i( 1, 0, 0), MultiBlockPartType.FLUID_OUTPUT),	// East: Fluid Output
			new MultiBlockPart(new Vec3i(-1, 0, 0), MultiBlockPartType.PROXY),			// West: Proxy

			new MultiBlockPart(new Vec3i( 0, 1, 0), MultiBlockPartType.PROXY),			// Up: Proxy
			new MultiBlockPart(new Vec3i( 1, 1, 0), MultiBlockPartType.PROXY),			// Up, East: Proxy
			new MultiBlockPart(new Vec3i(-1, 1, 0), MultiBlockPartType.ITEMS_INPUT),	// Up, West: Item Input

			new MultiBlockPart(new Vec3i( 0, 0, 1), MultiBlockPartType.FLUID_INPUT),	// South: Fluid Input
			new MultiBlockPart(new Vec3i( 1, 0, 1), MultiBlockPartType.PROXY),			// South, East: Proxy
			new MultiBlockPart(new Vec3i(-1, 0, 1), MultiBlockPartType.PROXY),			// South, West: Proxy

			new MultiBlockPart(new Vec3i( 0, 1, 1), MultiBlockPartType.PROXY),			// South, Up: Proxy
			new MultiBlockPart(new Vec3i( 1, 1, 1), MultiBlockPartType.PROXY),			// South, Up, East: Proxy
			new MultiBlockPart(new Vec3i(-1, 1, 1), MultiBlockPartType.PROXY)			// South, Up, West: Proxy
		};

		List<Direction> directions = List.of(Direction.SOUTH, Direction.EAST, Direction.WEST);
		int totalParts = PARTS[northIndex].length;

		for (Direction direction : directions)
		{
			int directionIndex = direction.get2DDataValue();
			PARTS[directionIndex] = new MultiBlockPart[totalParts];

			for (int i = 0; i < totalParts; i++)
			{
				PARTS[directionIndex][i] = PARTS[northIndex][i].rotateOffset(direction);
			}
		}
	}

	public StillBlock(Properties properties)
	{
		super(properties);
		registerDefaultState(getStateDefinition().any()
			.setValue(FACING, Direction.NORTH)
			.setValue(PART, MultiBlockPartType.CORE)
			.setValue(POWERED_ON, false)
			.setValue(FUEL_HATCH_OPEN, false)
			.setValue(PRESSURE_RELEASE_PULLED, false)
			.setValue(PRESSURE, 0)
		);
	}

	@Override
	protected MapCodec<? extends Block> codec()
	{
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
		builder.add(PART);
		builder.add(POWERED_ON);
		builder.add(FUEL_HATCH_OPEN);
		builder.add(PRESSURE_RELEASE_PULLED);
		builder.add(PRESSURE);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockPos corePos = context.getClickedPos();
		Level level = context.getLevel();
		Direction facing = context.getHorizontalDirection().getOpposite();
		MultiBlockPart[] parts = getMultiBlockParts(facing);

		for (MultiBlockPart part : parts)
		{
			BlockPos pos = corePos.offset(part.offsetToCore());
			if (!level.getBlockState(pos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(pos)) return null;
		}

		BlockState blockState = defaultBlockState()
			.setValue(FACING, facing)
			.setValue(PART, MultiBlockPartType.CORE)
			.setValue(POWERED_ON, false)
			.setValue(FUEL_HATCH_OPEN, false)
			.setValue(PRESSURE_RELEASE_PULLED, false)
			.setValue(PRESSURE, 0);

		return blockState;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type)
	{
		return false;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
	{
		super.onPlace(state, level, pos, oldState, movedByPiston);

		if (state.getValue(PART) != MultiBlockPartType.CORE) return;

		Direction facing = state.getValue(FACING);
		MultiBlockPart[] parts = getMultiBlockParts(facing);
		BlockState defaultState = defaultBlockState();

		for (MultiBlockPart part : parts)
		{
			BlockPos partPos = pos.offset(part.offsetToCore());
			level.setBlockAndUpdate(partPos, defaultState.setValue(PART, part.type()).setValue(FACING, facing));
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
	{
		BlockPos corePos = getCorePos(level, pos, state);
		if (corePos == null)
		{
			Simpleautomations.LOG.error("Core block not found for multiblock part at {}.", Utils.getReadableBlockPos(pos));
			return (level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
		}

		BlockState coreState = level.getBlockState(corePos);

		switch (state.getValue(PART))
		{
			case FUEL_INPUT ->
			{
				boolean hatchOpen = coreState.getValue(FUEL_HATCH_OPEN);
				level.setBlockAndUpdate(corePos, coreState.setValue(FUEL_HATCH_OPEN, !hatchOpen));
			}
			case INTERACTABLE ->
			{
				int partIndex = getMultiBlockPartIndex(corePos, pos, state.getValue(FACING));

				if (partIndex == 0)
				{
					boolean pressureReleaseEngaged = coreState.getValue(PRESSURE_RELEASE_PULLED);
					level.setBlockAndUpdate(corePos, coreState.setValue(PRESSURE_RELEASE_PULLED, !pressureReleaseEngaged));
				}
				else if (partIndex == 2)
				{
					boolean poweredOn = coreState.getValue(POWERED_ON);
					level.setBlockAndUpdate(corePos, coreState.setValue(POWERED_ON, !poweredOn));
				}
			}
			default -> {}
		}

		return (level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos)
	{
		return Shapes.box(-1.0d, 0.0d, -1.0d, 2.0d, 2.0d, 2.0d);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state)
	{
		return Shapes.empty();
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state)
	{
		return false;
	}

	@Override
	protected int getLightDampening(BlockState state)
	{
		return 0;
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state)
	{
		// TODO: Drop item, and inventory eventually.
		BlockPos corePos = getCorePos(level, pos, state);

		if (corePos != null)
		{
			Direction facing = state.getValue(FACING);
			MultiBlockPart[] parts = getMultiBlockParts(facing);

			for (MultiBlockPart part : parts)
			{
				BlockPos partPos = corePos.offset(part.offsetToCore());
				level.setBlock(partPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}

			level.setBlock(corePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	protected BlockPos getCorePos(LevelAccessor level, BlockPos pos, BlockState state)
	{
		if (state.getValue(PART) == MultiBlockPartType.CORE) return pos;

		MultiBlockPartType type = state.getValue(PART);
		Direction facing = state.getValue(FACING);
		MultiBlockPart[] parts = getMultiBlockParts(facing);

		for (MultiBlockPart part : parts)
		{
			if (part.type() == type)
			{
				BlockPos potentialCorePos = reverseOffset(pos, part.offsetToCore());
				if (level.getBlockState(potentialCorePos).getValueOrElse(PART, MultiBlockPartType.PROXY) == MultiBlockPartType.CORE) return potentialCorePos;
			}
		}

		return null;
	}

	/**
	 * Gets a stable index for a block in the multi-block structure independent of it's facing. Meaning,
	 * for the purpose of the order of indexes the
	 * <pre>
	 *     {@code
	 *                       NORTH
	 *   TOP (y = 1)		   ↑        BOTTOM (y = 0)
	 *   ┌──────┬──────┬──────┐	┌──────┬──────┬──────┐
	 *   │  9   │ 10   │ 11   │	│  0   │  1   │  2   │
	 *   ├──────┼──────┼──────┤	├──────┼──────┼──────┤
	 *   │ 12   │ 13   │ 14   │	│  3   │  4   │  5   │
	 *   ├──────┼──────┼──────┤	├──────┼──────┼──────┤
	 *   │ 15   │ 16   │ 17   │	│  6   │  7   │  8   │
	 *   └──────┴──────┴──────┘	└──────┴──────┴──────┘
	 *   WEST ←                ↓                → EAST
	 *                       SOUTH
	 *     }
	 * </pre>
	 *
	 * @param corePos
	 * The position of the core block.
	 * @param pos
	 * The position of the block in the structure for which to calculate the index.
	 * @param facing
	 * The direction the multi-block structure is facing.
	 * @return
	 * A value between {@code 0} and {@code 17}.
	 */
	private static int getMultiBlockPartIndex(BlockPos corePos, BlockPos pos, Direction facing)
	{
		int deltaX = pos.getX() - corePos.getX();
		int deltaY = pos.getY() - corePos.getY();
		int deltaZ = pos.getZ() - corePos.getZ();

		switch (facing)
		{
			case EAST ->
			{
				int oldDeltaX = deltaX;
				deltaX = deltaZ;
				deltaZ = -oldDeltaX;
			}

			case SOUTH ->
			{
				deltaX = -deltaX;
				deltaZ = -deltaZ;
			}

			case WEST ->
			{
				int oldDeltaX = deltaX;
				deltaX = -deltaZ;
				deltaZ = oldDeltaX;
			}

			case NORTH -> {}

			default -> throw new IllegalArgumentException("Facing must be horizontal");
		}

		// deltaY: 0..1, deltaZ: -1..1, deltaX: -1..1.
		// Add 1 to deltaZ to move its range to 0..2. Which corresponds to the row index. Multiply by 3 to get the index in the first column of the corresponding row.
		// Add 1 to deltaX to move its range to 0..2. Which corresponds to the column index. Add on top of the index in the first column.
		// On the bottom layer the deltaY * 9 term is always 0, on the top layer it is always 9. Add the result to the previously calculated index to get the top layer index.
		return deltaY * 9 + (deltaZ + 1) * 3 + (deltaX + 1);
	}

	protected static MultiBlockPart[] getMultiBlockParts(Direction facing)
	{
		return PARTS[facing.get2DDataValue()];
	}

	public static BlockPos reverseOffset(BlockPos pos, Vec3i offset)
	{
		return pos.offset(-offset.getX(), -offset.getY(), -offset.getZ());
	}

	public record MultiBlockPart(Vec3i offsetToCore, MultiBlockPartType type)
	{
		public MultiBlockPart rotateOffset(Direction facing)
		{
			return switch (facing)
			{
				case NORTH -> this;
				case EAST  -> new MultiBlockPart(new Vec3i(-offsetToCore.getZ(), offsetToCore.getY(),  offsetToCore.getX()), type);
				case SOUTH -> new MultiBlockPart(new Vec3i(-offsetToCore.getX(), offsetToCore.getY(), -offsetToCore.getZ()), type);
				case WEST  -> new MultiBlockPart(new Vec3i( offsetToCore.getZ(), offsetToCore.getY(), -offsetToCore.getX()), type);
				default    -> throw new IllegalArgumentException(String.format("Can't rotate %s. Facing must be horizontal", facing));
			};
		}
	}
}