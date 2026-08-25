package net.zarathul.simpleautomations.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.mobs.IGaggableMob;
import net.zarathul.simpleautomations.mobs.IParalyzable;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IGaggableMob, IParalyzable
{
	@Unique
	private boolean simpleautomations_isGagged = false;

	@Override
	public boolean simpleautomations_isGagged() { return simpleautomations_isGagged; }

	@Override
	public void simpleautomations_setGagged(boolean value) { simpleautomations_isGagged = value; }

	@Unique
	private boolean simpleautomations_isParalyzed = false;

	@Override
	public boolean simpleautomations_isParalyzed() { return simpleautomations_isParalyzed; }

	@Override
	public void simpleautomations_setParalyzed(boolean value) { simpleautomations_isParalyzed = value; }

	@Inject(method = "makeSound", at = @At("HEAD"), cancellable = true)
	public void simpleautomations_makeSoundInject(CallbackInfo info, @Local final @Nullable SoundEvent sound)
	{
		// isGagged should never be true on players, because the SilenceTonic checks if it is used on a player.
		// Also, Player and derived classes don't seem to use makeSound(). Still check for extra safety.
		boolean isPlayer = ((Object)this) instanceof Player;
		if (!isPlayer && simpleautomations_isGagged) info.cancel();
	}

	@ModifyReturnValue(method="isImmobile", at = @At("RETURN"))
	public boolean simpleautomations_isImmobileInject(boolean original)
	{
		return original || simpleautomations_isParalyzed;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	public void simpleautomations_addAdditionalSaveDataInject(CallbackInfo info, @Local final ValueOutput tag)
	{
		tag.putBoolean("simpleautomations_is_gagged"  , simpleautomations_isGagged);
		tag.putBoolean("simpleautomations_isParalyzed", simpleautomations_isParalyzed);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void simpleautomations_readAdditionalSaveDataInject(CallbackInfo info, @Local final ValueInput tag)
	{
		simpleautomations_isGagged = tag.getBooleanOr("simpleautomations_is_gagged"  , false);
		simpleautomations_isParalyzed = tag.getBooleanOr("simpleautomations_isParalyzed", false);
	}
}