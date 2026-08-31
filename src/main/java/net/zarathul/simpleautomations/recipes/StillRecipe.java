package net.zarathul.simpleautomations.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simplemodslib.api.fluid.FluidStack;

import java.util.List;

public class StillRecipe implements Recipe<StillRecipe.StillRecipeInput>
{
	private final FluidStack fluidInput;
	private final List<StillItemInput> itemInputs;
	private final FluidStack result;

	public StillRecipe(FluidStack fluidInput, List<StillItemInput> itemInputs, FluidStack result)
	{
		this.fluidInput = fluidInput.copy();
		this.itemInputs = List.copyOf(itemInputs);
		this.result = result.copy();
	}

	public static final MapCodec<StillRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			FluidStack.CODEC
				.fieldOf("fluid")
				.forGetter(StillRecipe::getFluidInput),

			StillItemInput.CODEC.listOf()
				.fieldOf("items")
				.forGetter(StillRecipe::getItemInputs),

			FluidStack.CODEC
				.fieldOf("result")
				.forGetter(StillRecipe::getResult)
		).apply(instance, StillRecipe::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, StillRecipe> STREAM_CODEC = StreamCodec.composite(
		FluidStack.STREAM_CODEC,
		StillRecipe::getFluidInput,

		StillItemInput.STREAM_CODEC.apply(
			net.minecraft.network.codec.ByteBufCodecs.list()
		),
		StillRecipe::getItemInputs,

		FluidStack.STREAM_CODEC,
		StillRecipe::getResult,

		StillRecipe::new
	);


	public FluidStack getFluidInput()
	{
		return fluidInput;
	}

	public List<StillItemInput> getItemInputs()
	{
		return itemInputs;
	}

	public FluidStack getResult()
	{
		return result;
	}

	@Override
	public boolean matches(StillRecipeInput input, Level level)
	{
		return !input.fluid().isEmpty() &&
			input.fluid().isSameFluid(fluidInput) &&
			(input.fluid().getAmount() >= fluidInput.getAmount()) &&
			(findMatch(input) != null);
	}


	/**
	 * Finds a valid assignment of inventory items to recipe ingredients.
	 *
	 * <p>This must be used when actually processing the recipe rather than
	 * calling {@link #matches(StillRecipeInput, Level)} and then trying to
	 * determine the consumed items independently. The returned match contains
	 * the exact slots and amounts that should be consumed.</p>
	 *
	 * @return a match, or null if the recipe does not match
	 */
	public StillRecipeMatch findMatch(StillRecipeInput input)
	{
		if (input.items().isEmpty() && !itemInputs.isEmpty()) return null;

		int[] remaining = new int[input.items().size()];

		for (int slot = 0; slot < input.items().size(); slot++)
		{
			remaining[slot] = input.items().get(slot).getCount();
		}

		int[][] consumed = new int[itemInputs.size()][input.items().size()];

		return (matchIngredient(0, remaining, input.items(), consumed)) ? new StillRecipeMatch(consumed) : null;
	}

	/**
	 * Matches one recipe ingredient.
	 *
	 * <p>Each ingredient starts searching at slot 0 because a later recipe
	 * ingredient is allowed to use an earlier inventory slot.</p>
	 */
	private boolean matchIngredient(int ingredientIndex, int[] remaining, List<ItemStack> available, int[][] consumed)
	{
		// All recipe ingredients have been successfully matched.
		if (ingredientIndex >= itemInputs.size()) return true;

		StillItemInput required = itemInputs.get(ingredientIndex);

		return matchAmount(ingredientIndex, required, required.count(), 0, remaining, available, consumed);
	}

	/**
	 * Attempts to satisfy one ingredient using the available inventory slots.
	 *
	 * <p>Backtracking is required because Ingredient/tag definitions can
	 * overlap. For example, a slot containing wheat can match both
	 * c:grains and minecraft:wheat.</p>
	 */
	private boolean matchAmount(int ingredientIndex, StillItemInput required, int amountRemaining, int slot, int[] remaining, List<ItemStack> available, int[][] consumed)
	{
		// This ingredient has been completely satisfied. Continue with the next recipe ingredient.
		if (amountRemaining <= 0) return matchIngredient(ingredientIndex + 1, remaining, available, consumed);

		// There are no more inventory slots to consider.
		if (slot >= available.size()) return false;

		ItemStack stack = available.get(slot);

		// First try using this slot. Only consume as much as this ingredient needs, or as much as remains in the slot.
		if (!stack.isEmpty() && remaining[slot] > 0 && required.ingredient().test(stack))
		{
			int used = Math.min(amountRemaining, remaining[slot]);

			remaining[slot] -= used;
			consumed[ingredientIndex][slot] += used;

			if (matchAmount(ingredientIndex, required, amountRemaining - used, slot + 1, remaining, available, consumed)) return true;

			// That choice led to a dead end. Undo it and try another assignment.
			remaining[slot] += used;
			consumed[ingredientIndex][slot] -= used;
		}

		// Try not using this slot for the current ingredient.
		return matchAmount(ingredientIndex, required, amountRemaining, slot + 1, remaining, available, consumed);
	}

	public record StillRecipeMatch(int[][] consumed) {}

	@Override
	public ItemStack assemble(StillRecipeInput input)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<? extends Recipe<StillRecipeInput>> getSerializer()
	{
		return ModRecipes.STILL_SERIALIZER;
	}

	@Override
	public RecipeType<? extends Recipe<StillRecipeInput>> getType()
	{
		return ModRecipes.STILL;
	}

	@Override
	public PlacementInfo placementInfo()
	{
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public boolean showNotification()
	{
		return false;
	}

	@Override
	public String group()
	{
		return SimpleAutomations.MOD_ID + ".still";
	}

	@Override
	public RecipeBookCategory recipeBookCategory()
	{
		return RecipeBookCategories.CRAFTING_MISC;
	}

	public record StillRecipeInput(FluidStack fluid, List<ItemStack> items) implements RecipeInput
	{
		@Override
		public ItemStack getItem(int index)
		{
			return (index < 0 || index >= items.size()) ? ItemStack.EMPTY : items.get(index);
		}

		@Override
		public int size()
		{
			return items.size();
		}
	}

	public record StillItemInput(Ingredient ingredient, int count)
	{
		public StillItemInput
		{
			if (count <= 0) throw new IllegalArgumentException("Still item input count must be greater than zero.");
		}

		public static final Codec<StillItemInput> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				Ingredient.CODEC
					.fieldOf("ingredient")
					.forGetter(StillItemInput::ingredient),

				Codec.intRange(1, 64)
					.optionalFieldOf("count", 1)
					.forGetter(StillItemInput::count)
			).apply(instance, StillItemInput::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, StillItemInput> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC,
			StillItemInput::ingredient,

			ByteBufCodecs.VAR_INT,
			StillItemInput::count,

			StillItemInput::new
		);
	}
}