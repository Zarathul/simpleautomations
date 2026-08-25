package net.zarathul.simpleautomations.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simpleautomations.components.Tonic;
import net.zarathul.simpleautomations.mobs.IGaggableMob;
import net.zarathul.simpleautomations.mobs.IParalyzable;

import java.util.List;

public class TonicItem extends Item
{
	public TonicItem(Item.Properties properties)
	{
		super(properties);

		DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior()
		{
			private final DefaultDispenseItemBehavior ejectBehavior = new DefaultDispenseItemBehavior();

			@Override
			protected ItemStack execute(BlockSource source, ItemStack stack)
			{
				ServerLevel world = source.level();
				BlockState dispenser = world.getBlockState(source.pos());
				Tonic tonic = stack.get(ModComponents.TONIC);

				if (tonic != null)
				{
					Direction dispenserFacing = dispenser.getValue(DispenserBlock.FACING);
					BlockPos searchStartPos = source.pos().relative(dispenserFacing);
					List<Entity> entities = world.getEntities(null, AABB.ofSize(Vec3.atBottomCenterOf(searchStartPos), 1, 1, 1));

					if (!entities.isEmpty())
					{
						applyEffect(entities.getFirst(), tonic.type());
						stack.consume(1, null);
					}
				}

				return ejectBehavior.dispense(source, stack);
			}
		});
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand type)
	{
		Tonic tonic = itemStack.get(ModComponents.TONIC);

		if (tonic != null)
		{
			applyEffect(target, tonic.type());
			itemStack.consume(1, player);
		}

		return (player.level().isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	private void applyEffect(Entity entity, Tonic.Type type)
	{
		if (entity instanceof Player) return;

		boolean doApplyEffect = true;

		switch (type)
		{
			case ANTIDOTE:
				doApplyEffect = false;

			case SILENCE:
				if (entity instanceof IGaggableMob gaggableMob)
				{
					gaggableMob.simpleautomations_setGagged(doApplyEffect);
				}
				if (type == Tonic.Type.SILENCE) break;

			case BINDING:
				if (entity instanceof IParalyzable paralyzableMob)
				{
					paralyzableMob.simpleautomations_setParalyzed(doApplyEffect);
				}
				if (type == Tonic.Type.BINDING) break;
		}
	}
}
