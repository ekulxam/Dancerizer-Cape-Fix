package dev.kleinbox.client.mixin;

import dev.kleinbox.client.animation.Animations;
import dev.kleinbox.client.animation.HumanoidPoseManipulator;
import dev.kleinbox.client.animation.PoseModifier;
import dev.kleinbox.common.ExpressivePlayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
    public void setupAnim(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (livingEntity instanceof ExpressivePlayer player) {
            // TODO Don't animate when player is not standing still

            // Check for valid playing animation
            String anim_type = player.dancerizer$getAnimationPose();
            if (!anim_type.isBlank() && Animations.INSTANCE.getPoses().containsKey(anim_type)) {
                HumanoidPoseManipulator animation = Animations.INSTANCE.getPoses().get(anim_type);

                if (player.dancerizer$isTaunting() > 0) {
                    // Is taunting
                    if (player.dancerizer$isTaunting() > 1) {
                        animation.apply(0, 0, head, body, leftArm, rightArm, leftLeg, rightLeg);
                        ci.cancel();
                    } else if (player.dancerizer$isTaunting() == 1)
                        PoseModifier.INSTANCE.reset(head, body, leftArm, rightArm, leftLeg, rightLeg);
                } else {
                    long timestamp = player.dancerizer$getLastEmoteTimestamp();
                    long time = System.currentTimeMillis();
                    int duration = player.dancerizer$isDancePlaying();

                    // Check for dance
                    if (duration > 1 && (time - timestamp) <= (animation.getLength() * 1000)) {
                        animation.apply(timestamp, time, head, body, leftArm, rightArm, leftLeg, rightLeg);
                        ci.cancel();
                    } else if (duration == 1)
                        PoseModifier.INSTANCE.reset(head, body, leftArm, rightArm, leftLeg, rightLeg);
                }
            }
        }
    }
}
