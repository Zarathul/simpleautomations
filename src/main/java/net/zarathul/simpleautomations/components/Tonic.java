package net.zarathul.simpleautomations.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Tonic(Type type)
{
	public static final Codec<Tonic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("type").forGetter(tonic -> tonic.type.name())
	).apply(instance, name -> new Tonic(Type.valueOf(name))));

	public static final StreamCodec<FriendlyByteBuf, Tonic> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, tonic -> tonic.type.name(),
		name -> new Tonic(Type.valueOf(name))
	);

	public enum Type
	{
		EMPTY,
		ANTIDOTE,
		SILENCE,
		BINDING
	}
}