package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import moremekasuitmodules.common.MoreMekaModulesReopen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public record ModuleHyperArmorUnit(boolean solidHitbox) implements ICustomModule<ModuleHyperArmorUnit> {
    public static final ResourceLocation SOLID_HITBOX = MoreMekaModulesReopen.rl("hyper_armor_solid_hitbox");

    public ModuleHyperArmorUnit(IModule<ModuleHyperArmorUnit> module) {
        this(module.<Boolean>getConfigOrThrow(SOLID_HITBOX).get());
    }

    @Override
    public void tickServer(IModule<ModuleHyperArmorUnit> module, IModuleContainer container, ItemStack stack, Player player) {
        if (module.isEnabled() && player.getItemBySlot(EquipmentSlot.CHEST) == stack) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 2, true, false, false));
        }
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleHyperArmorUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleHyperArmorUnit> module, Player player, IModuleContainer container, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(container, stack, player, net.minecraft.network.chat.Component.translatable("module.moremekasuitmodules.hyper_armor_unit"));
    }
}
