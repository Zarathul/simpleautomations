package net.zarathul.simplemodslib.api.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FluidContainerComponent(FluidStack fluid, int capacity, boolean singleBucketMode)
{
	public FluidContainerComponent(FluidStack fluid, int capacity)
	{
		this(fluid, capacity, true);
	}

	public static final Codec<FluidContainerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		FluidStack.CODEC.fieldOf("fluid").forGetter(FluidContainerComponent::fluid),
		Codec.INT.fieldOf("capacity").forGetter(FluidContainerComponent::capacity),
		Codec.BOOL.optionalFieldOf("singleBucketMode", true).forGetter(FluidContainerComponent::singleBucketMode)
	).apply(instance, FluidContainerComponent::new));
}