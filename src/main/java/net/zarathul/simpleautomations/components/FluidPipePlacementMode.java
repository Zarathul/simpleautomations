package net.zarathul.simpleautomations.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FluidPipePlacementMode(Mode mode)
{
	public static final Codec<FluidPipePlacementMode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("mode").forGetter(fluidPipePlacementMode -> fluidPipePlacementMode.mode.name())
	).apply(instance, name -> new FluidPipePlacementMode(Mode.valueOf(name))));

	public static final StreamCodec<FriendlyByteBuf, FluidPipePlacementMode> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, fluidPipePlacementMode -> fluidPipePlacementMode.mode.name(),
		name -> new FluidPipePlacementMode(FluidPipePlacementMode.Mode.valueOf(name))
	);

	public enum Mode
	{
		NORMAL,
		SMART
	}
}
