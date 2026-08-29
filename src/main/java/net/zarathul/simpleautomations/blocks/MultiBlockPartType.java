package net.zarathul.simpleautomations.blocks;

import net.minecraft.util.StringRepresentable;

public enum MultiBlockPartType implements StringRepresentable
{
	CORE("core"),
	PROXY("proxy"),
	FLUID_INPUT("fluid_input"),
	FLUID_OUTPUT("fluid_output"),
	ITEMS_INPUT("items_input"),
	FUEL_INPUT("fuel_input"),
	INTERACTABLE("interactable");

	private final String name;

	MultiBlockPartType(String name)
	{
		this.name = name;
	}

	@Override
	public String getSerializedName()
	{
		return name;
	}
}
