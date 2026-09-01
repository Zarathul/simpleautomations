package net.zarathul.simpleautomations.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.zarathul.simpleautomations.SimpleAutomations;
import net.zarathul.simpleautomations.blocks.ModBlocks;
import net.zarathul.simpleautomations.common.DistillationLevel;
import net.zarathul.simpleautomations.components.ModComponents;
import net.zarathul.simpleautomations.items.ModItems;
import net.zarathul.simpleautomations.particles.ModParticles;
import net.zarathul.simplemodslib.api.fluid.FluidContainerComponent;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.fluid.IBucketProvider;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.zarathul.simpleautomations.common.DistillationLevel.*;

public abstract class AlcoholFluid extends FlowingFluid implements IBucketProvider
{
	public static final EnumProperty<DistillationLevel> DISTILLATION_LEVEL = ModFluids.DISTILLATION_LEVEL;

	private static final int MAX_FILL_LEVEL = 8;

	@Override
	public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random)
	{
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (!state.isSource() && !(Boolean) state.getValue(FALLING))
		{
			if (random.nextInt(64) == 0)
			{
				level.playLocalSound(
					x + 0.5,
					y + 0.5,
					z + 0.5,
					SoundEvents.WATER_AMBIENT, // Bubbling poison/swamp sound
					SoundSource.AMBIENT,
					random.nextFloat() * 0.25F + 0.75F,
					random.nextFloat() + 0.5F,
					false);
			}
		}
		else if (random.nextInt(10) == 0)
		{
			level.addParticle(
				ParticleTypes.UNDERWATER,
				x + random.nextDouble(),
				y + random.nextDouble(),
				z + random.nextDouble(),
				0.0, 0.0, 0.0);
		}

		int fluidLevel = state.getValueOrElse(LEVEL, MAX_FILL_LEVEL);
		float heightOffset = 1.0f - ((float)fluidLevel / MAX_FILL_LEVEL); // Make particles spawn directly on the fluids surface.

		float particleX = x + random.nextFloat();
		float particleY = y + 1.0f - heightOffset;
		float particleZ = z + random.nextFloat();
		level.addParticle(ModParticles.ALCOHOL_EVAPORATION, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
	}

	@Override
	public ParticleOptions getDripParticle()
	{
		return ParticleTypes.DRIPPING_WATER;
	}

	@Override
	protected boolean canConvertToSource(ServerLevel world)
	{
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropResources(state, world, pos, blockEntity);
	}

	@Override
	protected void entityInside(Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler)
	{
		if (entity.isOnFire()) handler.apply(InsideBlockEffectType.FIRE_IGNITE);

		if (!(world instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity livingEntity)) return;

		if (world.getGameTime() % 20 == 0)
		{
			DistillationLevel distillationLevel = world.getFluidState(pos).getValue(DISTILLATION_LEVEL);

			switch (distillationLevel)
			{
				case PURE:
					livingEntity.hurtServer(serverLevel, world.damageSources().magic(), 2.0F); // 1 heart/sec
				case CONCENTRATED:
					livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, -3));
				case NORMAL:
					livingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 300, -3));
			}
		}
	}

	@Override
	protected int getSlopeFindDistance(LevelReader world)
	{
		return 4;
	}

	@Override
	public int getDropOff(LevelReader world)
	{
		return 1;
	}

	@Override
	public int getTickDelay(LevelReader world)
	{
		return 5;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction)
	{
		return direction == Direction.DOWN && !(fluid.isSame(ModFluids.ALCOHOL_STILL) || fluid.isSame(ModFluids.ALCOHOL_FLOWING));
	}

	@Override
	protected float getExplosionResistance()
	{
		return 1.0F;
	}

	@Override
	public Optional<SoundEvent> getPickupSound()
	{
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	@Override
	public Fluid getFlowing()
	{
		return ModFluids.ALCOHOL_FLOWING;
	}

	@Override
	public Fluid getSource()
	{
		return ModFluids.ALCOHOL_STILL;
	}

	@Override
	public boolean isSame(Fluid fluid)
	{
		return fluid == ModFluids.ALCOHOL_STILL || fluid == ModFluids.ALCOHOL_FLOWING;
	}

	@Override
	public Item getBucket()
	{
		return ModItems.ALCOHOL_BUCKET;
	}

	@Override
	public ItemStack getBucket(FluidStack fluid)
	{
		// Exceptions are possible here if fluid is null or fluid lacks the required component.
		// Both should never happen, and if either does it is an error warranting an exception.
		var component = fluid.get(ModComponents.ALCOHOL_DISTILLATION_LEVEL);

		return switch (component.level())
		{
			case NORMAL 	  -> ModItems.ALCOHOL_BUCKET.getDefaultInstance();
			case CONCENTRATED -> ModItems.CONCENTRATED_ALCOHOL_BUCKET.getDefaultInstance();
			case PURE 		  -> ModItems.PURE_ALCOHOL_BUCKET.getDefaultInstance();
		};
	}

	@Override
	public boolean isFilledBucket(ItemStack item)
	{
		return ItemStack.isSameItemSameComponents(item, ModItems.ALCOHOL_BUCKET.getDefaultInstance()) ||
			   ItemStack.isSameItemSameComponents(item, ModItems.CONCENTRATED_ALCOHOL_BUCKET.getDefaultInstance()) ||
			   ItemStack.isSameItemSameComponents(item, ModItems.PURE_ALCOHOL_BUCKET.getDefaultInstance());
	}

	@Override
	protected FluidState getNewLiquid(ServerLevel level, BlockPos pos, BlockState state)
	{
		FluidState newFluidState = super.getNewLiquid(level, pos, state);
		if (newFluidState.isEmpty()) return newFluidState;

		// To determine the DistillationLevel of the new fluid, look at all the alcohol fluids around (except up) and add up the
		// fluid levels multiplied by the respective DistillationLevels mix multiplier.
		HashMap<DistillationLevel, Integer> totalDistillationLevelAmounts = HashMap.newHashMap(DistillationLevel.values().length);
		for (Direction direction : Direction.values())
		{
			// Ignore potential alcohol fluid below. Otherwise, DistillationLevels with higher mix multiplier can propagate upwards.
			if (direction == Direction.DOWN) continue;

			FluidState neighborFluidState = level.getFluidState(pos.relative(direction));
			if (neighborFluidState.getType().isSame(this))
			{
				DistillationLevel neighborDistillationLevel = neighborFluidState.getValue(DISTILLATION_LEVEL);
				int totalAmount = totalDistillationLevelAmounts.getOrDefault(neighborDistillationLevel, 0);
				totalDistillationLevelAmounts.put(neighborDistillationLevel, totalAmount + (neighborDistillationLevel.getMixMultiplier() * neighborFluidState.getAmount()));
			}
		}

		// The DistillationLevel with the highest amount in the end is the winner. Just in case there is a tie,
		// pick the DistillationLevels with the highest mix multiplier.
		var highestDistillationLevelEntry = totalDistillationLevelAmounts.entrySet().stream().max(
			Comparator.<Map.Entry<DistillationLevel, Integer>>comparingInt(Map.Entry::getValue)
				.thenComparingInt(entry -> entry.getKey().getMixMultiplier())
		);

		if (highestDistillationLevelEntry.isEmpty()) return newFluidState;

		return newFluidState.setValue(DISTILLATION_LEVEL, highestDistillationLevelEntry.get().getKey());
	}

	@Override
	protected BlockState createLegacyBlock(FluidState fluidState)
	{
		return ModBlocks.ALCOHOL.defaultBlockState()
			.setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState))
			.setValue(DISTILLATION_LEVEL, fluidState.getValue(DISTILLATION_LEVEL));
	}

	public static class Flowing extends AlcoholFluid
	{
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
		{
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
			builder.add(DISTILLATION_LEVEL);
		}

		@Override
		public int getAmount(FluidState state)
		{
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state)
		{
			return false;
		}
	}

	public static class Source extends AlcoholFluid
	{
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
		{
			super.createFluidStateDefinition(builder);
			builder.add(DISTILLATION_LEVEL);
		}

		@Override
		public int getAmount(FluidState state)
		{
			return MAX_FILL_LEVEL;
		}

		@Override
		public boolean isSource(FluidState state)
		{
			return true;
		}

		@Override
		protected boolean isRandomlyTicking()
		{
			return true;
		}

		@Override
		protected void randomTick(ServerLevel level, BlockPos pos, FluidState fluidState, RandomSource random)
		{
			super.randomTick(level, pos, fluidState, random);

			if (!fluidState.isSource()) return;

			// Randomly evaporate alcohol sources. The purer the alcohol the faster the evaporation.

			DistillationLevel distillationLevel = fluidState.getValue(DISTILLATION_LEVEL);

			float chance = switch (distillationLevel)
			{
				case NORMAL 	  -> 0.30f;
				case CONCENTRATED -> 0.50f;
				case PURE 	      -> 0.75f;
			};

			boolean doEvaporate = (random.nextFloat() < chance);

			if (doEvaporate)
			{
				int newFluidLevel = fluidState.getAmount() - getDropOff(level);

				if (newFluidLevel <= 0) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
				else
				{
					FluidState newFluidState = ModFluids.ALCOHOL_FLOWING
						.defaultFluidState()
						.setValue(FlowingFluid.LEVEL, newFluidLevel)
						.setValue(ModFluids.DISTILLATION_LEVEL, distillationLevel);

					level.setBlock(pos, newFluidState.createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
				}
			}
		}
	}
}