package net.zarathul.simpleautomations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos)
	{
		return Shapes.box(-1.0d, 0.0d, -1.0d, 2.0d, 2.0d, 2.0d);
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

	private BlockPos getCorePos(LevelAccessor level, BlockPos pos, BlockState state)
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