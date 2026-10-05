package net.zarathul.simplemodslib.api.fluid;

import net.minecraft.world.level.material.FluidState;

public interface IModFluidBlock
{
	/**
	 * Get the fluid represented by the block as a {@code FluidStack}.<br>
	 * Makes it possible to transfer {@code FluidState} property to
	 * {@code FluidStack} component data.
	 *
	 * @return
	 * A {@code FluidStack} with the fluids component values.
	 */
	FluidStack getFluidStackFromState(FluidState state);

	/**
	 * Constructs a {@code FluidState} from a {@code FluidStack}.<br>
	 * Makes it possible to transfer {@code FluidStack} component to
	 * {@code FluidState} property data.
	 *
	 * @param stack
	 * The {@code FluidStack} which may have component data.
	 * @return
	 * The constructed {@code FluidState} with applicable properties set.
	 */
	FluidState getStateFromFluidStack(FluidStack stack);
}