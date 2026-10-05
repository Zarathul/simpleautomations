package net.zarathul.simpleautomations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zarathul.simpleautomations.blocks.entities.FluidGrateBlockEntity;
import net.zarathul.simplemodslib.api.block.WrenchableEntityBlock;
import org.jspecify.annotations.Nullable;

public class FluidGrateBlock extends WrenchableEntityBlock
{
	public static final MapCodec<FluidGrateBlock> CODEC = simpleCodec(FluidGrateBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.VERTICAL_DIRECTION;

	public FluidGrateBlock(Properties properties)
	{
		super(properties);

		registerDefaultState(getStateDefinition().any()
			.setValue(FACING, Direction.UP)
		);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		return new FluidGrateBlockEntity(worldPosition, blockState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public void handleToolWrenchClick(BlockState state, Level level, BlockPos pos, Player player, ItemStack equippedItemStack)
	{
		Direction facing = state.getValue(FACING);
		Direction newFacing = (facing == Direction.UP) ? Direction.DOWN : Direction.UP;

		level.setBlockAndUpdate(pos, state.setValue(FACING, newFacing));
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state)
	{
		return false;
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state)
	{
		return Shapes.empty();
	}
}