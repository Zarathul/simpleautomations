package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simpleautomations.blocks.StillBlock;
import net.zarathul.simpleautomations.recipes.ModRecipes;
import net.zarathul.simpleautomations.recipes.StillRecipe;
import net.zarathul.simplemodslib.api.fluid.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StillCoreBlockEntity extends BlockEntity
{
	private static final String PROGRESS = "progress";

	public static final int PROCESSING_TIME = 200;

	private int progress;

	public StillCoreBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		super(ModBlocks.STILL_CORE, worldPosition, blockState);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, StillCoreBlockEntity blockEntity)
	{
		if (!(level instanceof ServerLevel serverLevel)) return;

		blockEntity.serverTick(serverLevel);
	}

	private void serverTick(ServerLevel level)
	{
		if (!getBlockState().getValue(StillBlock.POWERED_ON))
		{
			resetProgress();
			return;
		}

		var fluidInput = getFluidInput();
		var fluidOutput = getFluidOutput();
		var itemInput = getItemInput();

		if (fluidInput == null || fluidOutput == null || itemInput == null)
		{
			resetProgress();
			return;
		}

		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < itemInput.getContainerSize(); i++)
		{
			items.add(itemInput.getItem(i).copy());
		}

		StillRecipe.StillRecipeInput input = new StillRecipe.StillRecipeInput(fluidInput.getFluid().copy(), items);

		Optional<RecipeHolder<StillRecipe>> recipe = level.recipeAccess().getRecipeFor(ModRecipes.STILL, input, level);

		if (recipe.isEmpty())
		{
			resetProgress();
			return;
		}

		StillRecipe stillRecipe = recipe.get().value();

		if (!canOutput(fluidOutput, stillRecipe.getResult()))
		{
			resetProgress();
			return;
		}

		progress++;

		if (progress >= PROCESSING_TIME)
		{
			process(stillRecipe, fluidInput, fluidOutput, itemInput);
			progress = 0;
		}

		setChanged();
	}

	private void process(StillRecipe recipe, MultiBlockFluidInventory fluidInput, MultiBlockFluidInventory fluidOutput, MultiBlockInventory itemInput)
	{
		FluidStack inputFluid = fluidInput.getFluid().copy();
		inputFluid.changeAmount(-recipe.getFluidInput().getAmount());
		fluidInput.setFluid(inputFluid);

		consumeItems(itemInput, recipe);

		FluidStack output = fluidOutput.getFluid().copy();

		if (output.isEmpty())
		{
			fluidOutput.setFluid(recipe.getResult().copy());
		}
		else
		{
			output.changeAmount(recipe.getResult().getAmount());
			fluidOutput.setFluid(output);
		}
	}

	private void consumeItems(MultiBlockInventory inventory, StillRecipe recipe)
	{
		for (var required : recipe.getItemInputs())
		{
			int remaining = required.count();

			for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++)
			{
				ItemStack stack = inventory.getItem(slot);

				if (stack.isEmpty() || !required.ingredient().test(stack)) continue;

				int consumed = Math.min(remaining, stack.getCount());

				stack.shrink(consumed);
				remaining -= consumed;

				if (stack.isEmpty())
				{
					inventory.setItem(slot, ItemStack.EMPTY);
				}
			}
		}

		inventory.setChanged();
	}

	private boolean canOutput(MultiBlockFluidInventory output, FluidStack result)
	{
		FluidStack existing = output.getFluid();

		return (existing.isEmpty()) ? (result.getAmount() <= output.getCapacity()) : (existing.isSameFluid(result) && existing.getAmount() + result.getAmount() <= output.getCapacity());
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
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);

		progress = input.getIntOr(PROGRESS, 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		output.putInt(PROGRESS, progress);
	}

	private MultiBlockFluidInventory getFluidInput()
	{
		return getFluidInventory(StillBlock.FLUID_INPUT_INDEX);
	}

	private MultiBlockFluidInventory getFluidOutput()
	{
		return getFluidInventory(StillBlock.FLUID_OUTPUT_INDEX);
	}
	// TODO: Make generic version to reduce code
	private MultiBlockFluidInventory getFluidInventory(int index)
	{
		Direction facing = getBlockState().getValue(StillBlock.FACING);
		BlockPos partPos = StillBlock.getPartPos(worldPosition, facing, index);

		return level.getBlockEntity(partPos, ModBlocks.MULTI_BLOCK_FLUID_INVENTORY).orElse(null);
	}

	private MultiBlockInventory getItemInput()
	{
		Direction facing = getBlockState().getValue(StillBlock.FACING);
		BlockPos partPos = StillBlock.getPartPos(worldPosition, facing, StillBlock.ITEMS_INPUT_INDEX);

		return level.getBlockEntity(partPos, ModBlocks.MULTI_BLOCK_INVENTORY).orElse(null);
	}
}
