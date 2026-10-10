package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.config.ModuleBooleanConfig;
import moremekasuitmodules.common.MoreMekaSuitModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@ParametersAreNotNullByDefault
public record ModuleHyperArmorUnit(boolean solidHitbox) implements ICustomModule<ModuleHyperArmorUnit> {
    public static final ResourceLocation SOLID_HITBOX = MoreMekaSuitModules.rl("hyper_armor_solid_hitbox");
    private static final ResourceLocation ARMOR = MoreMekaSuitModules.rl("hyper_armor.armor");
    private static final ResourceLocation TOUGHNESS = MoreMekaSuitModules.rl("hyper_armor.toughness");
    private static final ResourceLocation KNOCKBACK = MoreMekaSuitModules.rl("hyper_armor.knockback");

    public ModuleHyperArmorUnit(IModule<ModuleHyperArmorUnit> module) {
        this(module.<Boolean>getConfigOrThrow(SOLID_HITBOX).get());
    }

    @Override
    public void adjustAttributes(IModule<ModuleHyperArmorUnit> module, ItemAttributeModifierEvent event) {
        if (!(event.getItemStack().getItem() instanceof ArmorItem armor) || armor.getType() != ArmorItem.Type.CHESTPLATE) {
            return;
        }
        event.addModifier(Attributes.ARMOR, new AttributeModifier(ARMOR, 15.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
        event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(TOUGHNESS, 5.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
        event.addModifier(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(KNOCKBACK, 0.5D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
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
        module.toggleEnabled(container, stack, player, player.getItemBySlot(EquipmentSlot.CHEST) == stack ? net.minecraft.network.chat.Component.translatable("module.moremekasuitmodules.hyper_armor_unit") : net.minecraft.network.chat.Component.translatable("module.moremekasuitmodules.hyper_armor_unit"));
    }
}
