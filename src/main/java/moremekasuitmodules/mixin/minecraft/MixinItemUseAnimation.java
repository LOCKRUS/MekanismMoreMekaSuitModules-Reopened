package moremekasuitmodules.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mekanism.api.gear.IModuleHelper;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class MixinItemUseAnimation {
    @ModifyReturnValue(method = "getUseDuration", at = @At("RETURN"))
    private int moreMekaSuitModules$antimatterUseDuration(int original, ItemStack stack, LivingEntity entity) {
        return IModuleHelper.INSTANCE.isEnabled(stack, MekaSuitMoreModules.MEKA_TOOL_ANTIMATTER_STRIKE_UNIT) ? 72_000 : original;
    }

    @ModifyReturnValue(method = "getUseAnimation", at = @At("RETURN"))
    private UseAnim moreMekaSuitModules$antimatterUseAnimation(UseAnim original, ItemStack stack) {
        return IModuleHelper.INSTANCE.isEnabled(stack, MekaSuitMoreModules.MEKA_TOOL_ANTIMATTER_STRIKE_UNIT) ? UseAnim.BOW : original;
    }
}
