package net.zarathul.simpleautomations.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simplemodslib.api.recipes.RecipeRegistrar;

public final class ModRecipes
{
	private static final RecipeRegistrar REGISTRAR = new RecipeRegistrar(SimpleAutomations.MOD_ID);

	public static final RecipeType<StillRecipe> STILL = REGISTRAR.register("still", new RecipeType<StillRecipe>() {});
	public static final RecipeSerializer<StillRecipe> STILL_SERIALIZER = REGISTRAR.registerSerializer("still", new RecipeSerializer<>(StillRecipe.CODEC, StillRecipe.STREAM_CODEC));

	public static void init()
	{
		SimpleAutomations.LOG.info("Registering recipes.");
	}
}