package moriyashiine.extraorigins.mixin;

import moriyashiine.extraorigins.client.network.packet.StopRidingPacket;
import moriyashiine.extraorigins.common.registry.EOPowers;
import moriyashiine.extraorigins.common.registry.EOScaleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	private EntityDimensions dimensions;
	
	@Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
	private void slowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci) {
		if (EOPowers.NIMBLE.isActive((Entity) (Object) this)) {
			ci.cancel();
		}
	}
	
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "getMountedHeightOffset", at = @At("HEAD"), cancellable = true)
	private void getMountedHeightOffset(CallbackInfoReturnable<Double> cir) {
		if ((Object) this instanceof PlayerEntity player) {
			float scale = EOScaleTypes.MODIFY_SIZE_TYPE.getScaleData(player).getScale();
			double height = dimensions.height / scale;
			height += 0;
			height *= scale;
			cir.setReturnValue(height);
		}
	}
	
	@Inject(method = "dismountVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;removePassenger(Lnet/minecraft/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void dismountVehicle(CallbackInfo ci, Entity entity) {
		if (entity instanceof ServerPlayerEntity player) {
			StopRidingPacket.send(player, (Entity) (Object) this);
		}
	}
}
