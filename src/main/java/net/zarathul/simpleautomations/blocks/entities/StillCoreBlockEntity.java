package net.zarathul.simpleautomations.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
	private static final String PRESSURE_INCREASE_TIME  = "pressure_increase_time";
	private static final String PRESSURE_DECREASE_TIME  = "pressure_decrease_time";
	private static final String PRESSURE_TO_RELEASE  = "pressure_to_release";

	// These values are in ticks. Default Minecraft server is 20tps, so 200 = 10sec roughly.
	public static final int BASE_PROCESSING_TIME = 200;
	public static final int[] PROCESSING_TIMES = new int[StillBlock.MAX_PRESSURE + 1];
	public static final int PRESSURE_CHANGE_TIME = 200;

	private int progress;
	private int burnTime;
	private int pressureIncreaseTime;
	private int pressureDecreaseTime;
	private int pressureToRelease;

	static
	{
		for (int i = 0; i <= StillBlock.MAX_PRESSURE; i++)
		{
			PROCESSING_TIMES[i] = switch (i)
			{
				case 0,1,2,3 -> BASE_PROCESSING_TIME;
				case 4,5	 -> BASE_PROCESSING_TIME * 2;
				case 6,7	 -> BASE_PROCESSING_TIME * 6;
				default 	 -> Integer.MAX_VALUE;
			};
		}
	}


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

	protected void serverTick(ServerLevel level)
	{
		var fuelInput = getFuelInput();
		if (fuelInput == null)
		{
			resetProgress();
			return;
		}

		int oldPressure = getPressure();

		// Set up pressure release is handle is pulled.
		if (getBlockState().getValue(StillBlock.PRESSURE_RELEASE_PULLED))
		{
			// Release the handle if there is no pressure.
			if (oldPressure <= 0)
			{
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(StillBlock.PRESSURE_RELEASE_PULLED, false));
				level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.6f, 0.5f);
				pressureToRelease = 0;	// Void remaining pressure release if pressure 0 was reached.
			}
			else if (pressureToRelease <= 0)
			{
				pressureToRelease = StillBlock.MAX_PRESSURE;
				pressureIncreaseTime = 0;
			}
		}

		boolean poweredOn = getBlockState().getValue(StillBlock.POWERED_ON);
		int oldBurnTime = burnTime;
		int oldPressureIncreaseTime = pressureIncreaseTime;
		int oldPressureDecreaseTime = pressureDecreaseTime;
		int oldPressureToRelease = pressureToRelease;

		// Burn time decreases even if the machine is turned off, but no new fuel is consumed.
		if (burnTime > 0)	// Burning, no matter if machine is on or off.
		{
			burnTime--;

			// Only decrease pressure while burning if the release handle was pulled.
			if (pressureToRelease <= 0) increasePressure();
			else decreasePressure();
		}
		else	// No longer burning
		{
			// Decrease pressure if no fuel was consumed.
			if (poweredOn) burnTime = consumeFuel(fuelInput);
			if (burnTime <= 0) decreasePressure();
		}

		int pressure = getPressure();

		if (burnTime != oldBurnTime ||
			pressure != oldPressure ||
			pressureIncreaseTime != oldPressureIncreaseTime ||
			pressureDecreaseTime != oldPressureDecreaseTime ||
			pressureToRelease != oldPressureToRelease)
		{
			setChanged();
		}

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

		if (progress >= PROCESSING_TIMES[pressure])
		{
			process(stillRecipe, fluidInput, fluidOutput, itemInput);
			progress = 0;
		}

		setChanged();
	}

	private void increasePressure()
	{
		pressureIncreaseTime++;

		if (pressureIncreaseTime >= PRESSURE_CHANGE_TIME)
		{
			changePressure(1);
			pressureIncreaseTime = 0;
		}

		if (pressureDecreaseTime > 0) pressureDecreaseTime--;
	}

	protected void decreasePressure()
	{
		if (getPressure() > 0)
		{
			pressureDecreaseTime++;

			if (pressureDecreaseTime >= PRESSURE_CHANGE_TIME)
			{
				changePressure(-1);
				pressureDecreaseTime = 0;
				if (pressureToRelease > 0) pressureToRelease--;
			}
		}

		if (pressureIncreaseTime > 0) pressureIncreaseTime--;
	}

	protected int getPressure()
	{
		return getBlockState().getValue(StillBlock.PRESSURE);
	}

	protected void changePressure(int amount)
	{
		BlockState oldState = getBlockState();
		int oldPressure = oldState.getValue(StillBlock.PRESSURE).intValue();
		int newPressure = oldPressure + amount;

		if (newPressure >= StillBlock.MIN_PRESSURE && newPressure <= StillBlock.MAX_PRESSURE)
		{
			level.setBlockAndUpdate(worldPosition, oldState.setValue(StillBlock.PRESSURE, newPressure));
		}
	}

	protected void process(StillRecipe recipe, MultiBlockFluidInventory fluidInput, MultiBlockFluidInventory fluidOutput, MultiBlockInventory itemInput)
	{
		fluidInput.getFluid().changeAmount(-recipe.getFluidInput().getAmount());
		fluidInput.setChanged();

		consumeItems(itemInput, recipe);

		FluidStack output = fluidOutput.getFluid();
		// canOutput() already checked capacity and change amount at this time, so no checks required here.
		if (output.isEmpty()) fluidOutput.setFluid(recipe.getResult().copy());
		else output.changeAmount(recipe.getResult().getAmount());

		fluidOutput.setChanged();
	}

	protected void consumeItems(MultiBlockInventory inventory, StillRecipe recipe)
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

	protected int consumeFuel(MultiBlockInventory inventory)
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

	protected boolean canOutput(MultiBlockFluidInventory output, FluidStack result)
	{
		FluidStack existing = output.getFluid();

		return (existing.isEmpty()) ? (result.getAmount() <= output.getCapacity()) : (existing.isSameFluidSameComponents(result) && existing.getAmount() + result.getAmount() <= output.getCapacity());
	}

	protected void resetProgress()
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
		pressureIncreaseTime = input.getIntOr(PRESSURE_INCREASE_TIME, 0);
		pressureDecreaseTime = input.getIntOr(PRESSURE_DECREASE_TIME, 0);
		pressureToRelease = input.getIntOr(PRESSURE_TO_RELEASE, 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		output.putInt(PROGRESS, progress);
		output.putInt(BURN_TIME, burnTime);
		output.putInt(PRESSURE_INCREASE_TIME, pressureIncreaseTime);
		output.putInt(PRESSURE_DECREASE_TIME, pressureDecreaseTime);
		output.putInt(PRESSURE_TO_RELEASE, pressureToRelease);
	}

	protected MultiBlockFluidInventory getFluidInput()
	{
		return getInventory(StillBlock.FLUID_INPUT_INDEX, ModBlocks.MULTI_BLOCK_FLUID_INVENTORY);
	}

	protected MultiBlockFluidInventory getFluidOutput()
	{
		return getInventory(StillBlock.FLUID_OUTPUT_INDEX, ModBlocks.MULTI_BLOCK_FLUID_INVENTORY);
	}

	protected MultiBlockInventory getItemInput()
	{
		return getInventory(StillBlock.ITEMS_INPUT_INDEX, ModBlocks.MULTI_BLOCK_INVENTORY);
	}

	protected MultiBlockInventory getFuelInput()
	{
		return getInventory(StillBlock.FUEL_INPUT_INDEX, ModBlocks.MULTI_BLOCK_INVENTORY);
	}

	protected <T extends BlockEntity> T getInventory(int partIndex, BlockEntityType<T> type)
	{
		Direction facing = getBlockState().getValue(StillBlock.FACING);
		BlockPos partPos = StillBlock.getPartPos(worldPosition, facing, partIndex);

		return level.getBlockEntity(partPos, type).orElse(null);
	}
}
