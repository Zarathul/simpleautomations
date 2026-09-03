package net.zarathul.simpleautomations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simpleautomations.blocks.entities.MultiBlockFluidInventory;
import net.zarathul.simpleautomations.blocks.entities.MultiBlockInventory;
import net.zarathul.simpleautomations.blocks.entities.StillCoreBlockEntity;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.fluid.FluidHelper;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.zarathul.simplemodslib.Utils.getCenter;
import static net.zarathul.simplemodslib.Utils.reverseOffset;

public class StillBlock extends BaseEntityBlock
{
	public static final int PRESSURE_RELEASE_INDEX = 0;
	public static final int FUEL_INPUT_INDEX = 1;
	public static final int POWER_LEVER_INDEX = 2;
	public static final int FLUID_OUTPUT_INDEX = 5;
	public static final int FLUID_INPUT_INDEX = 7;
	public static final int ITEMS_INPUT_INDEX = 12;

	public static final int MIN_PRESSURE = 0;
	public static final int MAX_PRESSURE = 7;

	public static final MapCodec<StillBlock> CODEC = simpleCodec(StillBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<MultiBlockPartType> PART = EnumProperty.create("part", MultiBlockPartType.class);
	public static final BooleanProperty POWERED_ON = BooleanProperty.create("powered_on");
	public static final BooleanProperty FUEL_HATCH_OPEN = BooleanProperty.create("fuel_hatch_open");
	public static final EnumProperty<StillFuelState> FUEL = EnumProperty.create("fuel", StillFuelState.class);
	public static final BooleanProperty PRESSURE_RELEASE_PULLED = BooleanProperty.create("pressure_release_pulled");
	public static final IntegerProperty PRESSURE = IntegerProperty.create("pressure", MIN_PRESSURE, MAX_PRESSURE);

	private static final MultiBlockPart[][] PARTS;

	static
	{
		int northIndex = Direction.NORTH.get2DDataValue();
		PARTS = new MultiBlockPart[4][];
		PARTS[northIndex] = new MultiBlockPart[]
		{
			new MultiBlockPart(new Vec3i(-1, 0, -1), MultiBlockPartType.INTERACTABLE),	//  0 North, West: Power Gauge + Pressure Release
			new MultiBlockPart(new Vec3i( 0, 0, -1), MultiBlockPartType.FUEL_INPUT),	//  1 North: Fuel Hatch
			new MultiBlockPart(new Vec3i( 1, 0, -1), MultiBlockPartType.INTERACTABLE),	//  2 North, East: Power Lever

			new MultiBlockPart(new Vec3i(-1, 0, 0), MultiBlockPartType.PROXY),			//  3 West: Proxy
			new MultiBlockPart(new Vec3i( 0, 0, 0), MultiBlockPartType.CORE),			//  4 Core
			new MultiBlockPart(new Vec3i( 1, 0, 0), MultiBlockPartType.FLUID_OUTPUT),	//  5 East: Fluid Output

			new MultiBlockPart(new Vec3i(-1, 0, 1), MultiBlockPartType.PROXY),			//  6 South, West: Proxy
			new MultiBlockPart(new Vec3i( 0, 0, 1), MultiBlockPartType.FLUID_INPUT),	//  7 South: Fluid Input
			new MultiBlockPart(new Vec3i( 1, 0, 1), MultiBlockPartType.PROXY),			//  8 South, East: Proxy

			new MultiBlockPart(new Vec3i(-1, 1, -1), MultiBlockPartType.PROXY),		//  9 North, Up, West: Proxy
			new MultiBlockPart(new Vec3i( 0, 1, -1), MultiBlockPartType.PROXY),		// 10 North, Up: Proxy
			new MultiBlockPart(new Vec3i( 1, 1, -1), MultiBlockPartType.PROXY),		// 11 North, Up, East: Proxy

			new MultiBlockPart(new Vec3i(-1, 1, 0), MultiBlockPartType.ITEMS_INPUT),	// 12 Up, West: Item Input
			new MultiBlockPart(new Vec3i( 0, 1, 0), MultiBlockPartType.PROXY),			// 13 Up: Proxy
			new MultiBlockPart(new Vec3i( 1, 1, 0), MultiBlockPartType.PROXY),			// 14 Up, East: Proxy

			new MultiBlockPart(new Vec3i(-1, 1, 1), MultiBlockPartType.PROXY),			// 15 South, Up, West: Proxy
			new MultiBlockPart(new Vec3i( 0, 1, 1), MultiBlockPartType.PROXY),			// 16 South, Up: Proxy
			new MultiBlockPart(new Vec3i( 1, 1, 1), MultiBlockPartType.PROXY)			// 17 South, Up, East: Proxy
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
			.setValue(FUEL, StillFuelState.EMPTY)
			.setValue(PRESSURE_RELEASE_PULLED, false)
			.setValue(PRESSURE, 0)
		);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		MultiBlockPartType partType = blockState.getValue(PART);

		return switch (partType)
		{
			case CORE 		 			   -> new StillCoreBlockEntity(worldPosition, blockState);
			case FLUID_INPUT, FLUID_OUTPUT -> new MultiBlockFluidInventory(worldPosition, blockState, 32 * FluidStack.BUCKET_VOLUME);
			case ITEMS_INPUT  			   -> new MultiBlockInventory(worldPosition, blockState, 4, 64);
			case FUEL_INPUT   			   -> new MultiBlockInventory(worldPosition, blockState, 1, 64);
			default 		  			   -> null;
		};
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state)
	{
		return false;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type)
	{
		return createTickerHelper(type, ModBlocks.STILL_CORE, StillCoreBlockEntity::tick);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
		builder.add(PART);
		builder.add(POWERED_ON);
		builder.add(FUEL_HATCH_OPEN);
		builder.add(FUEL);
		builder.add(PRESSURE_RELEASE_PULLED);
		builder.add(PRESSURE);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockPos corePos = context.getClickedPos();
		Level level = context.getLevel();
		Direction facing = context.getHorizontalDirection().getOpposite();
		MultiBlockPart[] parts = PARTS[facing.get2DDataValue()];

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
			.setValue(FUEL, StillFuelState.EMPTY)
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
		MultiBlockPart[] parts = PARTS[facing.get2DDataValue()];
		BlockState defaultState = defaultBlockState();

		for (MultiBlockPart part : parts)
		{
			if (part.type() == MultiBlockPartType.CORE) continue;

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
			SimpleAutomations.LOG.error("Core block not found for multiblock part at {}.", Utils.getReadableBlockPos(pos));
			return (level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
		}

		BlockState coreState = level.getBlockState(corePos);

		switch (state.getValue(PART))
		{
			case FUEL_INPUT ->
			{
				boolean hatchOpen = coreState.getValue(FUEL_HATCH_OPEN);
				if (hatchOpen && player.isCrouching())	// Take out fuel
				{
					MultiBlockInventory fuelInventory = level.getBlockEntity(pos, ModBlocks.MULTI_BLOCK_INVENTORY).get();
					fuelInventory.takeItem(player);
				}
				else
				{
					level.setBlockAndUpdate(corePos, coreState.setValue(FUEL_HATCH_OPEN, !hatchOpen));
				}
			}
			case ITEMS_INPUT ->
			{
				if (player.isCrouching())
				{
					MultiBlockInventory itemsInventory = level.getBlockEntity(pos, ModBlocks.MULTI_BLOCK_INVENTORY).get();
					itemsInventory.takeItem(player);
				}
			}
			case INTERACTABLE ->
			{
				int partIndex = getMultiBlockPartIndex(corePos, pos, state.getValue(FACING));

				if (partIndex == PRESSURE_RELEASE_INDEX)
				{
					// The handle will release automatically after releasing MAX_PRESSURE or reaching pressure 0.
					boolean pressureReleaseEngaged = coreState.getValue(PRESSURE_RELEASE_PULLED);
					if (!pressureReleaseEngaged)
					{
						level.setBlockAndUpdate(corePos, coreState.setValue(PRESSURE_RELEASE_PULLED, true));
						level.playSound(player, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.6f, 0.6f);
					}
				}
				else if (partIndex == POWER_LEVER_INDEX)
				{
					boolean poweredOn = coreState.getValue(POWERED_ON);
					level.setBlockAndUpdate(corePos, coreState.setValue(POWERED_ON, !poweredOn));
					level.playSound(player, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, (poweredOn) ? 0.6f : 0.1f);
				}
			}
			default -> {}
		}

		return (level.isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if (itemStack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;

		BlockPos corePos = getCorePos(level, pos, state);

		if (corePos != null)
		{
			BlockState coreState = level.getBlockState(corePos);

			InteractionResult result = switch (state.getValue(PART))
			{
				case FUEL_INPUT -> handleFuelItemInput(itemStack, level, pos, coreState);
				case ITEMS_INPUT -> handleItemsInput(itemStack, level, pos, coreState);
				case FLUID_INPUT,FLUID_OUTPUT -> handleFluidInputOutput(player, hand, itemStack, level, pos, coreState);

				default -> InteractionResult.PASS;
			};

			if (result != InteractionResult.PASS) return result;
		}
		else
		{
			SimpleAutomations.LOG.error("Core block not found for multiblock part at {}.", Utils.getReadableBlockPos(pos));
		}

		return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
	}

	private InteractionResult handleFuelItemInput(ItemStack itemStack, Level level, BlockPos pos, BlockState coreState)
	{
		if (coreState.getValue(FUEL_HATCH_OPEN) && level.fuelValues().isFuel(itemStack))
		{
			if (!level.isClientSide())
			{
				MultiBlockInventory fuelInventory = level.getBlockEntity(pos, ModBlocks.MULTI_BLOCK_INVENTORY).get();
				fuelInventory.putItem(itemStack);
			}

			return (level.isClientSide()) ?  InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}

	private InteractionResult handleItemsInput(ItemStack itemStack, Level level, BlockPos pos, BlockState coreState)
	{
		if (!level.isClientSide())
		{
			MultiBlockInventory itemsInventory = level.getBlockEntity(pos, ModBlocks.MULTI_BLOCK_INVENTORY).get();
			itemsInventory.putItem(itemStack);
		}

		return (level.isClientSide()) ?  InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	private InteractionResult handleFluidInputOutput(Player player, InteractionHand hand, ItemStack itemStack, Level level, BlockPos pos, BlockState coreState)
	{
		if (!level.isClientSide())
		{
			var fluidInventory = level.getBlockEntity(pos, ModBlocks.MULTI_BLOCK_FLUID_INVENTORY);

			if (fluidInventory.isPresent())
			{
				if (FluidHelper.InteractWithFluidHandler((ServerPlayer)player, hand, fluidInventory.get()).success()) return InteractionResult.SUCCESS_SERVER;
			}
			else
			{
				SimpleAutomations.LOG.error("Missing MultiBlockFluidInventory at {}", pos.toShortString());
			}
		}

		if (FluidHelper.isFluidContainerItem(player.getItemInHand(hand))) return InteractionResult.SUCCESS;
		else return InteractionResult.PASS;
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state)
	{
		if (!level.isClientSide())
		{
			BlockPos corePos = getCorePos(level, pos, state);

			if (corePos != null)
			{
				Direction facing = state.getValue(FACING);
				destroyParts(level, facing, corePos);

				level.setBlock(corePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
	}

//	private void dropItemsFromInventories(Level level, Direction facing, BlockPos corePos)
//	{
//		BlockPos[] inventoryPos = new BlockPos[]
//		{
//			getPartPos(corePos, facing, FUEL_INPUT_INDEX),
//			getPartPos(corePos, facing, ITEMS_INPUT_INDEX)
//		};
//
//		for (BlockPos pos : inventoryPos)
//		{
//			var inventory = level.getBlockEntity(pos, ModBlocks.MULTI_BLOCK_INVENTORY);
//			if (inventory.isEmpty()) SimpleAutomations.LOG.error("Missing MultiBlockInventory at {}", pos.toShortString());
//			else
//			{
//				var container = inventory.get();
//
//				for (int i = 0; i < container.getContainerSize(); i++)
//				{
//					ItemStack itemInSlot = container.getItem(i);
//					if (!itemInSlot.isEmpty())
//					{
//						ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5d, pos.getY() + 1.0d, pos.getZ() + 0.5d, itemInSlot);
//						level.addFreshEntity(itemEntity);
//					}
//				}
//			}
//		}
//	}

	private void destroyParts(LevelAccessor level, Direction facing, BlockPos corePos)
	{
		MultiBlockPart[] parts = PARTS[facing.get2DDataValue()];

		for (MultiBlockPart part : parts)
		{
			if (part.type() == MultiBlockPartType.CORE) continue;

			BlockPos partPos = corePos.offset(part.offsetToCore());
			level.setBlock(partPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
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
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		if (state.getValue(PART) == MultiBlockPartType.CORE)
		{
			var core = level.getBlockEntity(pos, ModBlocks.STILL_CORE);

			if (core.isEmpty())
			{
				SimpleAutomations.LOG.error("Core block entity not found for multiblock part at {}.", pos.toShortString());
			}
			else if (core.get().isFueled())
			{
				BlockPos[] posAroundChimneyOne = getPartPos(pos, state.getValue(FACING), new int[] { 10, 11, 13, 14 });
				BlockPos[] posAroundChimneyTwo = getPartPos(pos, state.getValue(FACING), new int[] { 13, 14, 16, 17 });
				Vec3i[] chimneys = new Vec3i[]
				{
					getCenter(posAroundChimneyOne),
					getCenter(posAroundChimneyTwo)
				};

				for (int i = 0; i < chimneys.length; i++)
				{
					Vec3i chimneyPos = chimneys[i];
					float particleY = chimneyPos.getY() + 0.9f;

					for (int j = 0; j < 10; j++)
					{
						float particleX = chimneyPos.getX() + random.nextIntBetweenInclusive(-1, 1) * random.nextFloat() / 5;
						float particleZ = chimneyPos.getZ() + random.nextIntBetweenInclusive(-1, 1) * random.nextFloat() / 5;
						particleY += random.nextIntBetweenInclusive(0, 1) * random.nextFloat() / 10;

						level.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
					}
				}
			}
		}
	}

	protected BlockPos getCorePos(LevelAccessor level, BlockPos pos, BlockState state)
	{
		if (state.getValue(PART) == MultiBlockPartType.CORE) return pos;

		MultiBlockPartType type = state.getValue(PART);
		Direction facing = state.getValue(FACING);
		MultiBlockPart[] parts = PARTS[facing.get2DDataValue()];

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

	public static BlockPos getPartPos(BlockPos corePos, Direction facing, int index)
	{
		return corePos.offset(PARTS[facing.get2DDataValue()][index].offsetToCore());
	}

	public static BlockPos[] getPartPos(BlockPos corePos, Direction facing, int[] indexes)
	{
		if (indexes == null || indexes.length == 0) return null;

		BlockPos[] result = new BlockPos[indexes.length];

		for (int i = 0; i < indexes.length; i++)
		{
			result[i] = getPartPos(corePos, facing, indexes[i]);
		}

		return result;
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