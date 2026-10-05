package net.zarathul.simpleautomations.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.zarathul.simpleautomations.common.DistillationLevel;
import net.zarathul.simpleautomations.components.AlcoholDistillationLevel;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simpleautomations.fluids.ModFluids;
import net.zarathul.simpleautomations.items.ModItems;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.fluid.IModFluidBlock;
import org.jspecify.annotations.Nullable;

public class AlcoholBlock extends LiquidBlock implements IModFluidBlock
{
	public static final EnumProperty<DistillationLevel> DISTILLATION_LEVEL = ModFluids.DISTILLATION_LEVEL;

	public AlcoholBlock(FlowingFluid fluid, Properties properties)
	{
		super(fluid, properties);

		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0).setValue(DISTILLATION_LEVEL, DistillationLevel.NORMAL));
	}

	@Override
	public ItemStack pickupBlock(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state)
	{
		if (!super.pickupBlock(user, level, pos, state).isEmpty())
		{
			return new ItemStack(
				switch (state.getFluidState().getValue(ModFluids.DISTILLATION_LEVEL))
				{
					case NORMAL 	  -> ModItems.ALCOHOL_BUCKET;
					case CONCENTRATED -> ModItems.CONCENTRATED_ALCOHOL_BUCKET;
					case PURE	      -> ModItems.PURE_ALCOHOL_BUCKET;
				});
		}

		return ItemStack.EMPTY;
	}

	@Override
	protected FluidState getFluidState(BlockState state)
	{
		return super.getFluidState(state).setValue(DISTILLATION_LEVEL, state.getValue(DISTILLATION_LEVEL));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(DISTILLATION_LEVEL);
	}

	@Override
	public FluidStack getFluidStackFromState(FluidState state)
	{
		FluidStack stack = (fluid.isSource(state)) ?
						   new FluidStack(fluid, FluidStack.BUCKET_VOLUME).with(ModComponents.ALCOHOL_DISTILLATION_LEVEL, new AlcoholDistillationLevel(state.getValue(DISTILLATION_LEVEL))) :
						   FluidStack.empty();

		return stack;
	}

	@Override
	public FluidState getStateFromFluidStack(FluidStack stack)
	{
		AlcoholDistillationLevel stackLevel = stack.get(ModComponents.ALCOHOL_DISTILLATION_LEVEL);
		FluidState state = fluid.defaultFluidState().setValue(DISTILLATION_LEVEL, stackLevel.level());

		return state;
	}
}