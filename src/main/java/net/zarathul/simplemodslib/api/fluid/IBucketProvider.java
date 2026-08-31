package net.zarathul.simplemodslib.api.fluid;

import net.minecraft.world.item.ItemStack;

public interface IBucketProvider
{
	ItemStack getBucket(FluidStack fluid);
	boolean isFilledBucket(ItemStack item);
}