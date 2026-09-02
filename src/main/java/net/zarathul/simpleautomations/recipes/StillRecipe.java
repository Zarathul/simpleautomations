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

		StillItemInput.STREAM_CODEC.apply(ByteBufCodecs.list()),
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
			input.fluid().isSameFluidSameComponents(fluidInput) &&
			input.fluid().getAmount() >= fluidInput.getAmount() &&
			matchItems(input.items());
	}

	private boolean matchItems(List<ItemStack> items)
	{
		if (itemInputs.isEmpty() != items.isEmpty()) return false;

		// Check if all supplied items are actually needed by the recipe. If not, fail the match.
		for (int i = 0; i < items.size(); i++)
		{
			boolean isRequiredIngredient = false;

			for (var requiredInput : itemInputs)
			{
				if (requiredInput.ingredient().test(items.get(i)))
				{
					isRequiredIngredient = true;
					break;
				}
			}

			if (!isRequiredIngredient) return false;
		}

		// Check if enough items are supplied to satisfy the requirements of the recipe.
		// For each requirement all supplied item stack counts are processed. If there
		// are not enough items to fulfill a requirement, the match fails.
		for (var requiredInput : itemInputs)
		{
			int requiredRemaining = requiredInput.count();

			for (int i = 0; i < items.size(); i++)
			{
				ItemStack stack = items.get(i);
				if (!requiredInput.ingredient().test(stack)) continue;

				requiredRemaining -= stack.getCount();

				if (requiredRemaining <= 0) break;
			}

			if (requiredRemaining > 0) return false;
		}

		return true;
	}

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

			ByteBufCodecs.INT,
			StillItemInput::count,

			StillItemInput::new
		);
	}
}