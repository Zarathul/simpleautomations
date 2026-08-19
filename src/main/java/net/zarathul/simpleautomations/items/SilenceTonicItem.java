package net.zarathul.simpleautomations.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zarathul.simpleautomations.Simpleautomations;
import net.zarathul.simpleautomations.mobs.IGaggableMob;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SilenceTonicItem extends Item
{
	private static final String TOOLTIP_KEY = "item." + Simpleautomations.MOD_ID + "." + Items.SILENCE_TONIC_NAME + ".tooltip";
	private static final String TOOLTIP_DETAILS_KEY = "item." + Simpleautomations.MOD_ID + "." + Items.SILENCE_TONIC_NAME + ".tooltip_details";

	public SilenceTonicItem(ResourceKey<Item> id)
	{
		super(new Item.Properties()
			.setId(id)
			.stacksTo(1)
		);

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
						gaggableMob.simpleautomations_setGagged(true);
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
			gaggableMob.simpleautomations_setGagged(true);
			itemStack.consume(1, player);
		}

		return (player.level().isClientSide()) ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Environment(EnvType.CLIENT)
	public void addTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines)
	{
		long windowHandle = Minecraft.getInstance().getWindow().handle();
		int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
		int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		if (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS)
		{
			lines.addAll(Simpleautomations.multiLineTranslate(TOOLTIP_DETAILS_KEY));
		}
		else
		{
			lines.add(Component.translatable(TOOLTIP_KEY));
		}
	}
}
