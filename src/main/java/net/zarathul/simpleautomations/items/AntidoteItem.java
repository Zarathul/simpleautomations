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
import net.zarathul.simpleautomations.mobs.IGaggableMob;

import java.util.List;

public class AntidoteItem extends Item
{
	public AntidoteItem(Item.Properties properties)
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

				Direction dispenserFacing = dispenser.getValue(DispenserBlock.FACING);
				BlockPos searchStartPos = source.pos().relative(dispenserFacing);
				List<Entity> entities = world.getEntities(null, AABB.ofSize(Vec3.atBottomCenterOf(searchStartPos), 1, 1, 1));

				if (!entities.isEmpty())
				{
					if (entities.getFirst() instanceof IGaggableMob gaggableMob && !(gaggableMob instanceof Player))
					{
						gaggableMob.simpleautomations_setGagged(false);
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
		if (!(target instanceof Player))
		{
			IGaggableMob gaggableMob = (IGaggableMob) target;
			gaggableMob.simpleautomations_setGagged(false);
			itemStack.consume(1, player);
		}

		return (player.level().isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}
}