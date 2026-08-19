package net.zarathul.simpleautomations.items;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zarathul.simpleautomations.mobs.IGaggableMob;

public class AntidoteItem extends Item
{
	public AntidoteItem(ResourceKey<Item> id)
	{
		super(new Item.Properties()
			.setId(id)
			.stacksTo(1)
		);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand type)
	{
		IGaggableMob gaggableMob = (IGaggableMob) target;
		gaggableMob.simpleautomations_setGagged(false);
		itemStack.consume(1, player);

		return (player.level().isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}
}