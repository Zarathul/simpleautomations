package net.zarathul.simplemodslib.api.fluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IBucketProvider
{
	Item getBucket(FluidStack fluid);
	boolean isFilledBucket(ItemStack item);
}