package net.zarathul.simpleautomations.common;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

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

	public static DistillationLevel fromSerializedName(String name)
	{
		return DistillationLevel.valueOf(name.toUpperCase(Locale.ROOT));
	}

	@Override
	public String getSerializedName()
	{
		return name;
	}
}