package net.zarathul.simpleautomations.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.zarathul.simpleautomations.SimpleAutomations;

public final class ModRecipes
{
	public static final RecipeType<StillRecipe> STILL = Registry.register(BuiltInRegistries.RECIPE_TYPE, SimpleAutomations.modId("still"), new RecipeType<StillRecipe>() {});
	public static final RecipeSerializer<StillRecipe> STILL_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, SimpleAutomations.modId("still"), new RecipeSerializer<>(StillRecipe.CODEC, StillRecipe.STREAM_CODEC));

	public static void init()
	{
		SimpleAutomations.LOG.info("Registering recipes.");
	}
}