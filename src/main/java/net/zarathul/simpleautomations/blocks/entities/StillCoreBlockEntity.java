package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simpleautomations.blocks.StillBlock;
import net.zarathul.simpleautomations.recipes.ModRecipes;
import net.zarathul.simpleautomations.recipes.StillRecipe;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class StillCoreBlockEntity extends BlockEntity
{
	private static final String PROGRESS  = "progress";
	private static final String BURN_TIME = "burn_time";

	public static final int PROCESSING_TIME = 200;

	private int progress;
	private int burnTime;

	public StillCoreBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		super(ModBlocks.STILL_CORE, worldPosition, blockState);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, StillCoreBlockEntity blockEntity)
	{
		if (!(level instanceof ServerLevel serverLevel)) return;

		blockEntity.serverTick(serverLevel);
	}

	public boolean isFueled() { return burnTime > 0; }

	private void serverTick(ServerLevel level)
	{
		var fuelInput = getFuelInput();
		if (fuelInput == null)
		{
			resetProgress();
			return;
		}

		boolean poweredOn = getBlockState().getValue(StillBlock.POWERED_ON);
		int oldBurnTime = burnTime;

		// Burn time decreases even if the machine is turned off, but no new fuel is consumed.
		if (burnTime > 0) burnTime--;
		else if (poweredOn) burnTime = consumeFuel(fuelInput);

		if (burnTime != oldBurnTime) setChanged();

		if (!poweredOn || (burnTime <= 0))
		{
			resetProgress();
			return;
		}

		var fluidInput  = getFluidInput();
		var fluidOutput = getFluidOutput();
		var itemInput   = getItemInput();

		if (fluidInput == null || fluidOutput == null || itemInput == null)
		{
			resetProgress();
			return;
		}

		StillRecipe.StillRecipeInput input = new StillRecipe.StillRecipeInput(fluidInput.getFluid().copy(), itemInput.getAllItems());
		Optional<RecipeHolder<StillRecipe>> recipe = level.recipeAccess().getRecipeFor(ModRecipes.STILL, input, level);

		// No fitting recipe found, which can either mean the recipe(json) is wrong or the needed items/fluids are not present.
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
		fluidInput.getFluid().changeAmount(-recipe.getFluidInput().getAmount());

		consumeItems(itemInput, recipe);

		FluidStack output = fluidOutput.getFluid();
		// canOutput() already checked capacity and change amount at this time, so no checks required here.
		if (output.isEmpty()) fluidOutput.setFluid(recipe.getResult().copy());
		else output.changeAmount(recipe.getResult().getAmount());
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

				if (stack.isEmpty()) inventory.setItem(slot, ItemStack.EMPTY);
			}
		}

		inventory.setChanged();
	}

	private int consumeFuel(MultiBlockInventory inventory)
	{
		for (int slot = 0; slot < inventory.getContainerSize(); slot++)
		{
			ItemStack stack = inventory.getItem(slot);

			if (stack.isEmpty() || !level.fuelValues().isFuel(stack)) continue;

			int burnDuration = level.fuelValues().burnDuration(stack);
			stack.shrink(1);
			if (stack.isEmpty()) inventory.setItem(slot, ItemStack.EMPTY);

			inventory.setChanged();

			return burnDuration;
		}

		return 0;
	}

	private boolean canOutput(MultiBlockFluidInventory output, FluidStack result)
	{
		FluidStack existing = output.getFluid();

		return (existing.isEmpty()) ? (result.getAmount() <= output.getCapacity()) : (existing.isSameFluidSameComponents(result) && existing.getAmount() + result.getAmount() <= output.getCapacity());
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
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);

		progress = input.getIntOr(PROGRESS, 0);
		burnTime = input.getIntOr(BURN_TIME, 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		output.putInt(PROGRESS, progress);
		output.putInt(BURN_TIME, burnTime);
	}

	private MultiBlockFluidInventory getFluidInput()
	{
		return getInventory(StillBlock.FLUID_INPUT_INDEX, ModBlocks.MULTI_BLOCK_FLUID_INVENTORY);
	}

	private MultiBlockFluidInventory getFluidOutput()
	{
		return getInventory(StillBlock.FLUID_OUTPUT_INDEX, ModBlocks.MULTI_BLOCK_FLUID_INVENTORY);
	}

	private MultiBlockInventory getItemInput()
	{
		return getInventory(StillBlock.ITEMS_INPUT_INDEX, ModBlocks.MULTI_BLOCK_INVENTORY);
	}

	private MultiBlockInventory getFuelInput()
	{
		return getInventory(StillBlock.FUEL_INPUT_INDEX, ModBlocks.MULTI_BLOCK_INVENTORY);
	}

	private <T extends BlockEntity> T getInventory(int partIndex, BlockEntityType<T> type)
	{
		Direction facing = getBlockState().getValue(StillBlock.FACING);
		BlockPos partPos = StillBlock.getPartPos(worldPosition, facing, partIndex);

		return level.getBlockEntity(partPos, type).orElse(null);
	}
}
