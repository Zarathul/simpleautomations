package net.zarathul.simplemodslib.api.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeRegistrar
{
	private String modId;

	public RecipeRegistrar(String modId)
	{
		this.modId = modId;
	}

	public <T extends Recipe<? extends RecipeInput>> RecipeType<T> register(String name, RecipeType<T> type)
	{
		return Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(modId, name), type);
	}

	public <T extends Recipe<? extends RecipeInput>> RecipeSerializer<T> registerSerializer(String name, RecipeSerializer<T> serializer)
	{
		return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(modId, name), serializer);
	}
}
