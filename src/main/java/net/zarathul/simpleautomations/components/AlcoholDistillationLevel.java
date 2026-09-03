package net.zarathul.simpleautomations.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.zarathul.simpleautomations.common.DistillationLevel;

public record AlcoholDistillationLevel(DistillationLevel level)
{
	public static final Codec<AlcoholDistillationLevel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("level").forGetter(alcoholDistillationLevel -> alcoholDistillationLevel.level.getName())
	).apply(instance, name -> new AlcoholDistillationLevel(DistillationLevel.fromSerializedName(name))));

	public static final StreamCodec<FriendlyByteBuf, AlcoholDistillationLevel> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, alcoholDistillationLevel -> alcoholDistillationLevel.level.getName(),
		name -> new AlcoholDistillationLevel(DistillationLevel.fromSerializedName(name))
	);
}