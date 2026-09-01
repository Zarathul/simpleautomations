package net.zarathul.simpleautomations.common;

import net.minecraft.util.StringRepresentable;

public enum DistillationLevel implements StringRepresentable
{
	NORMAL("normal", 3),
	CONCENTRATED("concentrated", 2),
	PURE("pure", 1);

	private final String name;
	private final int mixMultiplier;

	public int getMixMultiplier()
	{
		return mixMultiplier;
	}

	public String getName()
	{
		return name;
	}

	DistillationLevel(String name, int mixMultiplier)
	{
		this.name = name;
		this.mixMultiplier = mixMultiplier;
	}

	@Override
	public String getSerializedName()
	{
		return name;
	}
}