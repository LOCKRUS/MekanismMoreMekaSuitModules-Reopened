package moremekasuitmodules.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import moremekasuitmodules.common.config.MoreModulesConfig;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleLootingAmplificationUnit;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class MixinEnchantedCountIncreaseFunction {
    @ModifyExpressionValue(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I")
    )
    private int moreMekaSuitModules$amplifyLooting(int original, @Local LivingEntity livingEntity) {
        ItemStack stack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
        IModule<ModuleLootingAmplificationUnit> module = IModuleHelper.INSTANCE.getModule(stack, MekaSuitMoreModules.LOOTING_AMPLIFICATION_UNIT);
        if (module == null) {
            stack = livingEntity.getItemInHand(InteractionHand.OFF_HAND);
            module = IModuleHelper.INSTANCE.getModule(stack, MekaSuitMoreModules.LOOTING_AMPLIFICATION_UNIT);
        }
        if (module == null || !module.isEnabled()) {
            return original;
        }
        int bonus = module.getCustomInstance().getEffectiveLevel(module.getInstalledCount());
        if (bonus <= 0) {
            return original;
        }
        long usage = MoreModulesConfig.config.mekaToolEnergyUsageLootingAmplification.get() * bonus;
        boolean free = livingEntity instanceof Player player && player.isCreative();
        if (!free && !module.hasEnoughEnergy(stack, usage)) {
            return original;
        }
        if (!free) {
            module.useEnergy(livingEntity, stack, usage);
        }
        return original + bonus;
    }
}
