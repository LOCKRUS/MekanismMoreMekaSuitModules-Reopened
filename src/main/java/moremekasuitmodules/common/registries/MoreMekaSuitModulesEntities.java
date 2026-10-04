package moremekasuitmodules.common.registries;

import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.AntimatterExplosiveOrbEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class MoreMekaSuitModulesEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MoreMekaSuitModules.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<AntimatterExplosiveOrbEntity>> ANTIMATTER_EXPLOSIVE_ORB =
            ENTITY_TYPES.register("antimatter_explosive_orb", () -> EntityType.Builder
                    .of((EntityType<AntimatterExplosiveOrbEntity> type, Level level) ->
                            new AntimatterExplosiveOrbEntity(type, level), MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build(MoreMekaSuitModules.rl("antimatter_explosive_orb").toString()));

    private MoreMekaSuitModulesEntities() {
    }
}
