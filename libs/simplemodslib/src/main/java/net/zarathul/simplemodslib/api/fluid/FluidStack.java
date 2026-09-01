package net.zarathul.simplemodslib.api.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simplemodslib.ModComponents;

import java.util.Objects;

public class FluidStack implements DataComponentHolder
{
	public static final int BUCKET_VOLUME = 1000; // in mB (milli-Buckets)

	private static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);
	private static final Identifier EMPTY_FLUID_ID = BuiltInRegistries.FLUID.getDefaultKey();
	private static final String FLUID_ID = "fluid_id";
	private static final String FLUID_AMOUNT = "fluid_amount";
	private static final String COMPONENTS = "fluid_components";

	private Fluid fluid;
	private int amount;
	private Identifier fluidId;
	private PatchedDataComponentMap components;

	public FluidStack(Fluid fluid, int amount)
	{
		this(fluid, amount, BuiltInRegistries.FLUID.getKey(fluid), DataComponentPatch.EMPTY);
	}

	private FluidStack(Fluid fluid, int amount, Identifier fluidId)
	{
		this(fluid, amount, fluidId, DataComponentPatch.EMPTY);
	}

	private FluidStack(Fluid fluid, int amount, Identifier fluidId, DataComponentPatch componentPatch)
	{
		this.fluid = fluid;
		this.amount = amount;
		this.fluidId = fluidId;

		this.components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, componentPatch);
	}

	private FluidStack(Fluid fluid, int amount, Identifier fluidId, PatchedDataComponentMap components)
	{
		this.fluid = fluid;
		this.amount = amount;
		this.fluidId = fluidId;
		this.components = components;
	}

	private FluidStack()
	{
		this.fluid = Fluids.EMPTY;
		this.amount = 0;
		this.fluidId = EMPTY_FLUID_ID;
		this.components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, DataComponentPatch.EMPTY);
	}

	public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC
				.fieldOf("fluid")
				.forGetter(FluidStack::getRegistryKey),

			Codec.INT
				.fieldOf("amount")
				.forGetter(FluidStack::getAmount),

			DataComponentPatch.CODEC
				.optionalFieldOf(
					"components",
					DataComponentPatch.EMPTY
				)
				.forGetter(FluidStack::getComponentsPatch)
		).apply(instance, FluidStack::from)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC,
		FluidStack::getRegistryKey,

		ByteBufCodecs.INT,
		FluidStack::getAmount,

		DataComponentPatch.STREAM_CODEC,
		FluidStack::getComponentsPatch,

		FluidStack::from
	);

	public static FluidStack from(Identifier fluidId, int amount)
	{
		return from(fluidId, amount, DataComponentPatch.EMPTY);
	}

	public static FluidStack from(Identifier fluidId, int amount, DataComponentPatch componentPatch)
	{
		if (amount <= 0) return empty();

		var registryGetResult = BuiltInRegistries.FLUID.get(fluidId);

		return (registryGetResult.isPresent()) ? new FluidStack(registryGetResult.get().value(), amount, fluidId, componentPatch) : empty();
	}

	public FluidStack copy()
	{
		return new FluidStack(fluid, amount, fluidId, components.copy());
	}

	public Fluid getFluid()
	{
		return fluid;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
		if (this.amount <= 0) makeEmpty();
	}

	public void changeAmount(int delta)
	{
		this.amount += delta;
		if (this.amount <= 0) makeEmpty();
	}

	public Identifier getRegistryKey()
	{
		return fluidId;
	}

	public boolean isEmpty()
	{
		return ((this == EMPTY) || (amount <= 0));
	}

	@Override
	public DataComponentMap getComponents()
	{
		return components;
	}

	public DataComponentPatch getComponentsPatch()
	{
		return components.asPatch();
	}

	public boolean isComponentsPatchEmpty()
	{
		return components.asPatch().isEmpty();
	}

	@Override
	public <T> T get(DataComponentType<? extends T> type)
	{
		return components.get(type);
	}

	@Override
	public <T> T getOrDefault(DataComponentType<? extends T> type, T defaultValue)
	{
		return components.getOrDefault(type, defaultValue);
	}

	@Override
	public boolean has(DataComponentType<?> type)
	{
		return components.has(type);
	}

	public <T> T set(DataComponentType<T> type, T value)
	{
		return components.set(type, value);
	}

	public <T> FluidStack with(DataComponentType<T> type, T value)
	{
		set(type, value);
		return this;
	}

	public <T> T remove(DataComponentType<T> type)
	{
		return components.remove(type);
	}

	public void applyComponents(DataComponentPatch patch)
	{
		components.applyPatch(patch);
	}

	public boolean isSameFluid(FluidStack other)
	{
		return other != null && other.fluid.isSame(this.fluid);
	}

	public boolean isSameFluidSameComponents(FluidStack other)
	{
		return other != null && other.fluid.isSame(this.fluid) && components.equals(other.components);
	}

	public static FluidStack empty()
	{
		return EMPTY.copy();
	}

	private void makeEmpty()
	{
		amount = 0;
		fluidId = EMPTY_FLUID_ID;
		fluid = Fluids.EMPTY;
		components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, DataComponentPatch.EMPTY);
	}

	public void save(ValueOutput output)
	{
		output.putInt(FLUID_AMOUNT, amount);
		output.putString(FLUID_ID, fluidId.toString());

		if (!isComponentsPatchEmpty())
		{
			output.store(COMPONENTS, DataComponentPatch.CODEC, components.asPatch());
		}
	}

	public void load(ValueInput input)
	{
		amount = input.getInt(FLUID_AMOUNT).get();

		if (amount <= 0)
		{
			makeEmpty();
			return;
		}

		fluidId = Identifier.parse(input.getString(FLUID_ID).get());

		var registryGetResult = BuiltInRegistries.FLUID.get(fluidId);
		if (registryGetResult.isPresent())
		{
			fluid = registryGetResult.get().value();
		}
		else
		{
			// In case the fluid is no longer available,
			// e.g. a mod got removed.
			makeEmpty();
			return;
		}

		DataComponentPatch patch = input.read(COMPONENTS, DataComponentPatch.CODEC).orElse(DataComponentPatch.EMPTY);

		components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch);
	}

	public static FluidStack from(FluidContainerComponent input)
	{
		return input.fluid().copy();
	}

	public static FluidStack getFluid(ItemStack stack)
	{
		var component = stack.get(ModComponents.FLUID_CONTAINER_COMPONENT);
		if (component == null) return FluidStack.empty();

		return FluidStack.from(component);
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof FluidStack otherStack)) return false;

		return otherStack.fluid.isSame(fluid) &&
			otherStack.amount == amount &&
			components.equals(otherStack.components);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(fluid, amount, components);
	}
}