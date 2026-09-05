package net.zarathul.simpleautomations.blocks;

import net.minecraft.util.StringRepresentable;

public enum StillFuelState implements StringRepresentable
{
	EMPTY("empty"),
	UNLIT("unlit"),
	LIT("lit"),
	LIT_LAST("lit_last");

	private final String name;

	StillFuelState(String name)
	{
		this.name = name;
	}

	@Override
	public String getSerializedName()
	{
		return name;
	}
}
