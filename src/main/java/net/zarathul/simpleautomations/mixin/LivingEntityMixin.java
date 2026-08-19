package net.zarathul.simpleautomations.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simpleautomations.mobs.IGaggableMob;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(LivingEntity.class)
public class LivingEntityMixin implements IGaggableMob
{
	@Unique
	private boolean simpleautomations_isGagged = false;

	@Override
	public boolean simpleautomations_isGagged() { return simpleautomations_isGagged; }

	@Override
	public void simpleautomations_setGagged(boolean value) { simpleautomations_isGagged = value; }

	@Inject(method = "makeSound", at = @At("HEAD"), cancellable = true)
	public void simpleautomations_makeSoundInject(CallbackInfo info, @Local final @Nullable SoundEvent sound)
	{
		// TODO: Remove debug output
//		System.out.println("original:" + ((sound != null) ? sound.toString() : "") + " gagged: " + simpleautomations_isGagged);
		if (simpleautomations_isGagged) info.cancel();
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	public void simpleautomations_addAdditionalSaveDataInject(CallbackInfo info, @Local final ValueOutput tag)
	{
		tag.putBoolean("simpleautomations_is_gagged", simpleautomations_isGagged);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void simpleautomations_readAdditionalSaveDataInject(CallbackInfo info, @Local final ValueInput tag)
	{
		simpleautomations_isGagged = tag.getBooleanOr("simpleautomations_is_gagged", false);
	}
}