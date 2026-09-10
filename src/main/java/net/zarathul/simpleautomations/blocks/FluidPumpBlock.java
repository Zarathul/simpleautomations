package net.zarathul.simpleautomations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zarathul.simpleautomations.blocks.entities.FluidPumpBlockEntity;
import net.zarathul.simplemodslib.api.block.WrenchableEntityBlock;
import org.jspecify.annotations.Nullable;

public class FluidPumpBlock extends WrenchableEntityBlock
{
	public static final int MIN_SPEED = 0;
	public static final int SPEED_STEPS = 4;
	public static final int MAX_SPEED = MIN_SPEED + SPEED_STEPS - 1;

	public static final MapCodec<FluidPumpBlock> CODEC = simpleCodec(FluidPumpBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED_ON = BooleanProperty.create("powered_on");
	public static final IntegerProperty SPEED = IntegerProperty.create("speed", MIN_SPEED, MAX_SPEED);

	private static final VoxelShape[] SHAPES = new VoxelShape[]
	{
		Block.boxZ(16.0d, 16.0d, 3.0d, 14.0d),							// SOUTH
		Block.box(2.0d, 0.0d, 0.0d, 13.0d, 16.0d, 16.0d),	// WEST
		Block.boxZ(16.0d, 16.0d, 2.0d, 13.0d),							// NORTH
		Block.box(3.0d, 0.0d, 0.0d, 14.0d, 16.0d, 16.0d)		// EAST
	};
	public static final FluidPumpBlockEntity.PumpSpeed BASE_PUMP_SPEED = new FluidPumpBlockEntity.PumpSpeed(100, 20);
	public static final int[] MODIFIED_PUMP_SPEEDS =
	{
		BASE_PUMP_SPEED.amount(),
		Math.round(BASE_PUMP_SPEED.amount() * 2.5f),
		Math.round(BASE_PUMP_SPEED.amount() * 5f),
		BASE_PUMP_SPEED.amount() * 10
	};

	public FluidPumpBlock(Properties properties)
	{
		super(properties);

		registerDefaultState(getStateDefinition().any()
			.setValue(FACING, Direction.NORTH)
			.setValue(POWERED_ON, false)
			.setValue(SPEED, 0)
		);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		return new FluidPumpBlockEntity(worldPosition, blockState);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type)
	{
		return createTickerHelper(type, ModBlocks.FLUID_PUMP_ENTITY, FluidPumpBlockEntity::tick);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
		builder.add(POWERED_ON);
		builder.add(SPEED);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		Direction facing = context.getHorizontalDirection().getOpposite();

		return defaultBlockState().setValue(FACING, facing);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		Direction facing = state.getValue(FACING);
		return SHAPES[facing.get2DDataValue()];
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
	{
		Direction facing = state.getValue(FACING);

		if (player.getItemInHand(player.getUsedItemHand()).isEmpty() && hitResult.getDirection() == facing)
		{
			boolean hitIsOnRightSide = (facing == Direction.NORTH) ? hitResult.getLocation().x() < pos.getX() + 0.5d :
									   (facing == Direction.SOUTH) ? hitResult.getLocation().x() > pos.getX() + 0.5d :
									   (facing == Direction.EAST) ? hitResult.getLocation().z() < pos.getZ() + 0.5d :
									   hitResult.getLocation().z() > pos.getZ() + 0.5d;

			BlockState newState;

			if (hitIsOnRightSide)
			{
				int speedDelta;
				float selectorPitch;

				if (player.isCrouching())
				{
					speedDelta = -1;
					selectorPitch = 1.8f;
				}
				else
				{
					speedDelta = 1;
					selectorPitch = 1.65f;
				}

				int speed = state.getValue(SPEED);
				int newSpeed = Math.floorMod(speed + ((player.isCrouching()) ? -1 : 1), SPEED_STEPS);
				newState = state.setValue(SPEED, newSpeed);

				level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, selectorPitch);
			}
			else
			{
				boolean poweredOn = state.getValue(POWERED_ON);
				newState = state.setValue(POWERED_ON, !poweredOn);

				level.playSound(player, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, (poweredOn) ? 0.6f : 0.1f);
			}

			level.setBlockAndUpdate(pos, newState);
		}

		return (level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public void handleToolWrenchClick(BlockState state, Level level, BlockPos pos, Player player, ItemStack equippedItemStack)
	{
		Direction facing = state.getValue(FACING);
		Direction newFacing = (player.isCrouching()) ? facing.getCounterClockWise() : facing.getClockWise();

		level.setBlockAndUpdate(pos, state.setValue(FACING, newFacing));

		FluidPumpBlockEntity pumpEntity = level.getBlockEntity(pos, ModBlocks.FLUID_PUMP_ENTITY).get();
		pumpEntity.updateConnections();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack)
	{
		if (!level.isClientSide())
		{
			var blockEntity = level.getBlockEntity(pos, ModBlocks.FLUID_PUMP_ENTITY);

			if (blockEntity.isPresent())
			{
				blockEntity.get().updateConnections();
			}
		}

		super.setPlacedBy(level, pos, state, by, itemStack);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston)
	{
		FluidPumpBlockEntity pumpEntity = level.getBlockEntity(pos, ModBlocks.FLUID_PUMP_ENTITY).get();
		pumpEntity.updateConnections();
	}

	public boolean hasConnectorOnSide(BlockState state, Direction direction)
	{
		Direction facing = state.getValue(FACING);

		return switch (facing)
		{
			case NORTH, SOUTH -> (direction.getAxis() == Direction.Axis.X);
			case WEST, EAST   -> (direction.getAxis() == Direction.Axis.Z);
			default 		  -> false;
		};
	}
}