package net.zarathul.simpleautomations.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.zarathul.simpleautomations.components.AlcoholDistillationLevel;
import net.zarathul.simpleautomations.components.ModComponents;
import org.jspecify.annotations.Nullable;

public class AlcoholDistillationLevelProperty implements SelectItemModelProperty<AlcoholDistillationLevel>
{
	public static final SelectItemModelProperty.Type<AlcoholDistillationLevelProperty, AlcoholDistillationLevel> TYPE =
		SelectItemModelProperty.Type.create(MapCodec.unit(new AlcoholDistillationLevelProperty()), AlcoholDistillationLevel.CODEC);

	@Override
	public @Nullable AlcoholDistillationLevel get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext)
	{
		var fluidContainerComponent = itemStack.get(net.zarathul.simplemodslib.ModComponents.FLUID_CONTAINER_COMPONENT);
		if (fluidContainerComponent != null)
		{
			var levelComponent = fluidContainerComponent.fluid().get(ModComponents.ALCOHOL_DISTILLATION_LEVEL);
			return levelComponent;
		}

		return null;
	}

	@Override
	public Codec<AlcoholDistillationLevel> valueCodec()
	{
		return AlcoholDistillationLevel.CODEC;
	}

	@Override
	public Type<? extends SelectItemModelProperty<AlcoholDistillationLevel>, AlcoholDistillationLevel> type()
	{
		return null;
	}
}
