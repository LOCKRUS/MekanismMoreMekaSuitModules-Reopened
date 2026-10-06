package moremekasuitmodules.common.registries;

import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.gear.config.ModuleBooleanConfig;
import mekanism.api.gear.config.ModuleColorConfig;
import mekanism.api.gear.config.ModuleEnumConfig;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.content.gear.*;
import moremekasuitmodules.common.content.gear.ModuleAutomaticAttackUnit.Range;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleLootingAmplificationUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleMekaToolPerformanceAmplificationUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleMekaToolBlasterUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleMekaToolAntimatterStrikeUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleMekaToolLavaTankUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleCounterattackUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleWallClingUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleImpactWaveUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleFlightUnit;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleEntityDisplayBoxUnit;

public class MekaSuitMoreModules {


    public MekaSuitMoreModules() {
    }

    //注册所属ID
    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(MoreMekaSuitModules.MODID);


    //开始注册模块
    //磁吸单元（在安装物品上能够确保该物品一直吸附在身上）（缺少接口，暂时不实现）
    //生命恢复单元
    public static final ModuleRegistryObject<ModuleHealthRegenerationUnit> HEALTH_REGENERATION_UNIT = MODULES.registerInstanced("health_regeneration_unit", ModuleHealthRegenerationUnit::new, () -> MekaSuitMoreModulesItem.MODULE_HEALTH_REGENERATION, builder -> builder.maxStackSize(10));
    //紧急救援（消耗该单元来进行复活玩家）
    public static final ModuleRegistryObject<?> EMERGENCY_RESCUE_UNIT = MODULES.registerMarker("emergency_rescue_unit", () -> MekaSuitMoreModulesItem.MODULE_EMERGENCY_RESCUE, builder -> builder.maxStackSize(10));
    //先进救援（不消耗该单元来进行复活玩家，且如果安装并启用了紧急救援，则不消耗紧急救援）
    public static final ModuleRegistryObject<?> ADVANCED_INTERCEPTION_SYSTEM_UNIT = MODULES.registerMarker("advanced_interception_system_unit", () -> MekaSuitMoreModulesItem.MODULE_ADVANCED_INTERCEPTION_SYSTEM);
    //密封单元 //GC或者AR （允许在太空中呼吸））//和mod重复，不添加 https://www.curseforge.com/minecraft/mc-mods/mekanism-x-create-northstar
    //热防护单元 //GC （允许在更热的星球不会导致过热）//和mod重复，不添加 https://www.curseforge.com/minecraft/mc-mods/mekanism-x-create-northstar
    //绝缘单元 IE 和 GTCEU（将电流导入大地，防止导致受伤）
    public static final ModuleRegistryObject<?> INSULATED_UNIT = MODULES.registerMarker("insulated_unit", () -> MekaSuitMoreModulesItem.MODULE_INSULATED);
    //自动灭火单元（着火时消耗MekaSuit能量自动灭火）
    public static final ModuleRegistryObject<ModuleAutomaticExtinguishUnit> AUTOMATIC_EXTINGUISH_UNIT = MODULES.registerInstanced("automatic_extinguish_unit", ModuleAutomaticExtinguishUnit::new, () -> MekaSuitMoreModulesItem.MODULE_AUTOMATIC_EXTINGUISH, builder -> builder.maxStackSize(1));
    // Контратака: отражает часть входящего урона атакующему.
    public static final ModuleRegistryObject<ModuleCounterattackUnit> COUNTERATTACK_UNIT = MODULES.register(
            "counterattack_unit",
            ModuleCounterattackUnit::new,
            () -> MekaSuitMoreModulesItem.MODULE_COUNTERATTACK,
            builder -> builder.maxStackSize(ModuleCounterattackUnit.MAX_MODULES_PER_ARMOR).disabledByDefault()
                    .addInstalledCountConfig(
                            installed -> ModuleEnumConfig.createBounded(ModuleCounterattackUnit.COUNTER_LEVEL, ModuleCounterattackUnit.CounterLevel.LOW, Math.max(1, Math.min(ModuleCounterattackUnit.CounterLevel.values().length, (installed + 1) / 2))),
                            installed -> ModuleEnumConfig.codec(ModuleCounterattackUnit.CounterLevel.CODEC, ModuleCounterattackUnit.CounterLevel.class, Math.max(1, Math.min(ModuleCounterattackUnit.CounterLevel.values().length, (installed + 1) / 2))),
                            installed -> ModuleEnumConfig.streamCodec(ModuleCounterattackUnit.CounterLevel.STREAM_CODEC, ModuleCounterattackUnit.CounterLevel.class, Math.max(1, Math.min(ModuleCounterattackUnit.CounterLevel.values().length, (installed + 1) / 2)))
                    ));
    // Wall climbing and impact-wave modules are installed on MekaSuit boots.
    public static final ModuleRegistryObject<ModuleWallClingUnit> WALL_CLING_UNIT = MODULES.register(
            "wall_cling_unit", ModuleWallClingUnit::new, () -> MekaSuitMoreModulesItem.MODULE_WALL_CLING,
            builder -> builder.maxStackSize(ModuleWallClingUnit.MAX_MODULES).disabledByDefault()
                    .addInstalledCountConfig(
                            installed -> ModuleEnumConfig.createBounded(ModuleWallClingUnit.CLIMB_SPEED, ModuleWallClingUnit.ClimbSpeed.LOW, Math.min(ModuleWallClingUnit.ClimbSpeed.values().length, installed)),
                            installed -> ModuleEnumConfig.codec(ModuleWallClingUnit.ClimbSpeed.CODEC, ModuleWallClingUnit.ClimbSpeed.class, Math.min(ModuleWallClingUnit.ClimbSpeed.values().length, installed)),
                            installed -> ModuleEnumConfig.streamCodec(ModuleWallClingUnit.ClimbSpeed.STREAM_CODEC, ModuleWallClingUnit.ClimbSpeed.class, Math.min(ModuleWallClingUnit.ClimbSpeed.values().length, installed))));
    public static final ModuleRegistryObject<ModuleImpactWaveUnit> IMPACT_WAVE_UNIT = MODULES.register(
            "impact_wave_unit", ModuleImpactWaveUnit::new, () -> MekaSuitMoreModulesItem.MODULE_IMPACT_WAVE,
            builder -> builder.maxStackSize(ModuleImpactWaveUnit.MAX_MODULES).disabledByDefault()
                    .addInstalledCountConfig(
                            installed -> ModuleEnumConfig.createBounded(ModuleImpactWaveUnit.TRIGGER_HEIGHT, ModuleImpactWaveUnit.TriggerHeight.LOW, Math.min(ModuleImpactWaveUnit.TriggerHeight.values().length, installed)),
                            installed -> ModuleEnumConfig.codec(ModuleImpactWaveUnit.TriggerHeight.CODEC, ModuleImpactWaveUnit.TriggerHeight.class, Math.min(ModuleImpactWaveUnit.TriggerHeight.values().length, installed)),
                            installed -> ModuleEnumConfig.streamCodec(ModuleImpactWaveUnit.TriggerHeight.STREAM_CODEC, ModuleImpactWaveUnit.TriggerHeight.class, Math.min(ModuleImpactWaveUnit.TriggerHeight.values().length, installed)))
                    .addConfig(ModuleEnumConfig.create(ModuleImpactWaveUnit.RADIUS, ModuleImpactWaveUnit.ImpactRadius.LOW), ModuleEnumConfig.codec(ModuleImpactWaveUnit.ImpactRadius.CODEC), ModuleEnumConfig.streamCodec(ModuleImpactWaveUnit.ImpactRadius.STREAM_CODEC))
                    .addConfig(ModuleEnumConfig.create(ModuleImpactWaveUnit.DAMAGE, ModuleImpactWaveUnit.DamageScale.LOW), ModuleEnumConfig.codec(ModuleImpactWaveUnit.DamageScale.CODEC), ModuleEnumConfig.streamCodec(ModuleImpactWaveUnit.DamageScale.STREAM_CODEC)));
    // Энергетический полёт: до четырёх модулей, каждый уровень повышает скорость до x2.
    public static final ModuleRegistryObject<ModuleFlightUnit> FLIGHT_UNIT = MODULES.register(
            "flight_unit", ModuleFlightUnit::new, () -> MekaSuitMoreModulesItem.MODULE_FLIGHT,
            builder -> builder.maxStackSize(ModuleFlightUnit.MAX_MODULES).disabledByDefault().exclusive(ExclusiveFlag.OVERRIDE_JUMP)
                    .addInstalledCountConfig(
                            installed -> ModuleEnumConfig.createBounded(ModuleFlightUnit.FLIGHT_LEVEL, ModuleFlightUnit.FlightLevel.ONE, Math.min(ModuleFlightUnit.FlightLevel.values().length, installed)),
                            installed -> ModuleEnumConfig.codec(ModuleFlightUnit.FlightLevel.CODEC, ModuleFlightUnit.FlightLevel.class, Math.min(ModuleFlightUnit.FlightLevel.values().length, installed)),
                            installed -> ModuleEnumConfig.streamCodec(ModuleFlightUnit.FlightLevel.STREAM_CODEC, ModuleFlightUnit.FlightLevel.class, Math.min(ModuleFlightUnit.FlightLevel.values().length, installed))));
    // Показывает рамки, имена, дистанцию и здоровье живых существ на экране.
    public static final ModuleRegistryObject<ModuleEntityDisplayBoxUnit> ENTITY_DISPLAY_BOX_UNIT = MODULES.register(
            "entity_display_box_unit", ModuleEntityDisplayBoxUnit::new, () -> MekaSuitMoreModulesItem.MODULE_ENTITY_DISPLAY_BOX,
            builder -> builder.maxStackSize(1).disabledByDefault()
                    .addConfig(ModuleEnumConfig.create(ModuleEntityDisplayBoxUnit.RANGE, ModuleEntityDisplayBoxUnit.Range.MEDIUM),
                            ModuleEnumConfig.codec(ModuleEntityDisplayBoxUnit.Range.CODEC), ModuleEnumConfig.streamCodec(ModuleEntityDisplayBoxUnit.Range.STREAM_CODEC))
                    .addConfig(ModuleEnumConfig.create(ModuleEntityDisplayBoxUnit.MAX_BOXES, ModuleEntityDisplayBoxUnit.MaxBoxes.MEDIUM),
                            ModuleEnumConfig.codec(ModuleEntityDisplayBoxUnit.MaxBoxes.CODEC), ModuleEnumConfig.streamCodec(ModuleEntityDisplayBoxUnit.MaxBoxes.STREAM_CODEC))
                    .addConfig(ModuleEnumConfig.create(ModuleEntityDisplayBoxUnit.HEALTH_DISPLAY, ModuleEntityDisplayBoxUnit.HealthDisplay.OFF),
                            ModuleEnumConfig.codec(ModuleEntityDisplayBoxUnit.HealthDisplay.CODEC), ModuleEnumConfig.streamCodec(ModuleEntityDisplayBoxUnit.HealthDisplay.STREAM_CODEC))
                    .addConfig(ModuleColorConfig.argb(ModuleEntityDisplayBoxUnit.BOX_COLOR, ModuleEntityDisplayBoxUnit.DEFAULT_BOX_COLOR),
                            ModuleColorConfig.ARGB_CODEC, ModuleColorConfig.ARGB_STREAM_CODEC)
                    .addConfig(ModuleColorConfig.argb(ModuleEntityDisplayBoxUnit.NAME_COLOR, ModuleEntityDisplayBoxUnit.DEFAULT_NAME_COLOR),
                            ModuleColorConfig.ARGB_CODEC, ModuleColorConfig.ARGB_STREAM_CODEC)
                    .addConfig(ModuleColorConfig.argb(ModuleEntityDisplayBoxUnit.DISTANCE_COLOR, ModuleEntityDisplayBoxUnit.DEFAULT_DISTANCE_COLOR),
                            ModuleColorConfig.ARGB_CODEC, ModuleColorConfig.ARGB_STREAM_CODEC));
    //防蜂单元 （散发特殊的信息素，让蜜蜂对你视而不见） //TODO

    //扭曲清除基础单元 神秘（通过特殊的方法移除身上的临时扭曲值）//神秘未到1.20.1
    //扭曲清除高级单元 神秘（通过特殊的方法移除身上的普通扭曲值）//神秘未到1.20.1
    //扭曲清除终极单元 神秘（通过特殊的方法移除身上的永久扭曲值）【创造物品】//神秘未到1.20.1
    //魔力优化单元 神秘（使用特殊的方法，减少魔力Vis的使用）[最大25个]//神秘未到1.20.1
    //揭示护目单元 神秘 （同揭示之护目镜）(需要mixin)//神秘未到1.20.1
    //智能温度调节单元 意志坚定(稳定身体的温度，一直保持最佳温度)//和mod重复，不添加 https://modrinth.com/mod/mekanismmoremodules
    //自动供液单元 意志坚定(如果口渴了，自动喝水)//和mod重复，不添加 https://modrinth.com/mod/mekanismmoremodules
    //微重力调节单元 //和mod重复，不添加 https://www.curseforge.com/minecraft/mc-mods/gravitational-modulating-additional-unit
    //能量护盾单元 龙研[改用MEK实现]（给meka套提供能量护盾）[最大10个](需要mixin)[因为DR3的盾无法实现，所以是按照DR2的盾来实现]
    public static final ModuleRegistryObject<ModuleEnergyShieldUnit> ENERGY_SHIELD_UNIT = MODULES.registerInstanced("energy_shield_unit", ModuleEnergyShieldUnit::new, () -> MekaSuitMoreModulesItem.MODULE_ENERGY_SHIELD, builder -> builder.maxStackSize(10));
    //能量护盾控制器单元 龙研
    public static final ModuleRegistryObject<ModuleEnergyShieldControllerUnit> ENERGY_SHIELD_CONTROLLER_UNIT = MODULES.register("energy_shield_controller_unit", ModuleEnergyShieldControllerUnit::new, () -> MekaSuitMoreModulesItem.MODULE_ENERGY_CONTROLLER_SHIELD, builder -> builder.addConfig(ModuleBooleanConfig.create(ModuleEnergyShieldControllerUnit.SHIELD_ENABLE, true)));
    ///混沌抗性单元 龙研（给meka套提供阻挡混沌伤害的抵抗效果）[最大25个] //TODO

    //混沌旋涡稳定器 龙研（当挖掘混沌晶体时，如果玩家附近会产生混沌旋涡，则移除本单元来平息该爆炸）//1.20.1无该实体，取消
    //智能屏蔽单元 冰与火（通过芯片分析，自动屏蔽对方的目光）【需要mixin】
    /**
     * {@link iceAndFireModules#SMART_SHIELDING_UNIT}
     */
    //无限能量供能单元 （让MekaSuit始终充满能量）【创造物品】
    public static final ModuleRegistryObject<ModuleInfiniteEnergySupplyUnit> INFINITE_ENERGY_SUPPLY_UNIT = MODULES.registerInstanced("infinite_energy_supply_unit", ModuleInfiniteEnergySupplyUnit::new, () -> MekaSuitMoreModulesItem.MODULE_INFINITE_ENERGY_SUPPLY, builder -> builder);
    //无限拦截救援系统单元 不再受伤【创造物品】
    public static final ModuleRegistryObject<ModuleInfiniteInterceptionAndRescueSystemUnit> INFINITE_INTERCEPTION_AND_RESCUE_SYSTEM_UNIT = MODULES.register("infinite_interception_and_rescue_system_unit", ModuleInfiniteInterceptionAndRescueSystemUnit::new, () -> MekaSuitMoreModulesItem.MODULE_INFINITE_INTERCEPTION_AND_RESCUE_SYSTEM,
            builder -> builder.noDisable()
                    .addConfig(ModuleBooleanConfig.create(ModuleInfiniteInterceptionAndRescueSystemUnit.DAMAGE_SOURCE, false))
                    .addConfig(ModuleBooleanConfig.create(ModuleInfiniteInterceptionAndRescueSystemUnit.DAMAGE_SOURCE_INDIRECT, false))
                    .addConfig(ModuleBooleanConfig.create(ModuleInfiniteInterceptionAndRescueSystemUnit.CHUNK_REMOVE, true)));
    //光环单元 植物魔法
    /**
     * {@link botaniaModules#BAND_OF_AURA_UNIT}
     */
    //基础光环单元 植物魔法
    /**
     * {@link botaniaModules#BASIC_BAND_OF_AURA_UNIT}
     */
    //高级光环单元 植物魔法
    /**
     * {@link botaniaModules#ADVANCED_BAND_OF_AURA_UNIT}
     */
    //精英光环单元 植物魔法
    /**
     * {@link botaniaModules#ELITE_BAND_OF_AURA_UNIT}
     */
    //终极光环单元 植物魔法
    /**
     * {@link botaniaModules#ULTIMATE_BAND_OF_AURA_UNIT}
     */
    //创造光环单元【创造物品】 植物魔法
    /**
     * {@link botaniaModules#CREATIVE_BAND_OF_AURA_UNIT}
     */
    //AE智能无线单元 AE
    //无限供能单元 （让MekaSuit始终充气体/流体）【创造物品】
    public static final ModuleRegistryObject<ModuleInfiniteChemicalAndFluidSupplyUnit> INFINITE_CHEMICAL_AND_FLUID_SUPPLY_UNIT = MODULES.register("infinite_chemical_and_fluid_supply_unit", ModuleInfiniteChemicalAndFluidSupplyUnit::new, () -> MekaSuitMoreModulesItem.MODULE_INFINITE_CHEMICAL_AND_FLUID_SUPPLY, builder -> builder
            .addConfig(ModuleBooleanConfig.create(ModuleInfiniteChemicalAndFluidSupplyUnit.SUPPLY_ARMOR, true))
            .addConfig(ModuleBooleanConfig.create(ModuleInfiniteChemicalAndFluidSupplyUnit.SUPPLY_INVENTORY, false))
            .addConfig(ModuleBooleanConfig.create(ModuleInfiniteChemicalAndFluidSupplyUnit.SUPPLY_CURIOS, false)));

    //智能范围攻击单元 (WC 挂)
    public static final ModuleRegistryObject<ModuleAutomaticAttackUnit> AUTOMATIC_ATTACK_UNIT = MODULES.register("automatic_attack_unit", ModuleAutomaticAttackUnit::new, () -> MekaSuitMoreModulesItem.MODULE_AUTOMATIC_ATTACK, builder -> builder.maxStackSize(4).disabledByDefault()
            .addConfig(ModuleBooleanConfig.create(ModuleAutomaticAttackUnit.ATTACK_PLAYER, false))
            .addConfig(ModuleBooleanConfig.create(ModuleAutomaticAttackUnit.ATTACK_HOSTILE, true))
            .addConfig(ModuleBooleanConfig.create(ModuleAutomaticAttackUnit.ATTACK_NEUTRAL, false))
            .addConfig(ModuleBooleanConfig.create(ModuleAutomaticAttackUnit.ATTACK_OTHER, false))
            .addInstalledCountConfig(
                    installed -> ModuleEnumConfig.createBounded(ModuleAutomaticAttackUnit.RANGE, Range.LOW, installed + 1),
                    installed -> ModuleEnumConfig.codec(Range.CODEC, Range.class, installed + 1),
                    installed -> ModuleEnumConfig.streamCodec(Range.STREAM_CODEC, Range.class, installed + 1)
            ));
    //动力增强单元（增加伤害和攻击速度）
    public static final ModuleRegistryObject<ModulePowerEnhancementUnit> POWER_ENHANCEMENT_UNIT = MODULES.registerInstanced("power_enhancement_unit", ModulePowerEnhancementUnit::new, () -> MekaSuitMoreModulesItem.MODULE_POWER_ENHANCEMENT, builder -> builder.maxStackSize(64));
    //加速冷却单元(加速物品的冷却)
    public static final ModuleRegistryObject<ModuleHighSpeedCoolingUnit> HIGH_SPEED_COOLING_UNIT = MODULES.registerInstanced("high_speed_cooling_unit", ModuleHighSpeedCoolingUnit::new, () -> MekaSuitMoreModulesItem.MODULE_HIGH_SPEED_COOLING, builder -> builder.maxStackSize(10));
    //量子重建单元
    public static final ModuleRegistryObject<ModuleQuantumReconstructionUnit> QUANTUM_RECONSTRUCTION_UNIT = MODULES.registerInstanced("quantum_reconstruction_unit", ModuleQuantumReconstructionUnit::new, () -> MekaSuitMoreModulesItem.MODULE_QUANTUM_RECONSTRUCTION, builder -> builder.handlesModeChange().modeChangeDisabledByDefault().disabledByDefault());
    //生命提升单元
    public static final ModuleRegistryObject<ModuleHPBootsUnit> HP_BOOTS_UNIT = MODULES.registerInstanced("hp_boots_unit", ModuleHPBootsUnit::new, () -> MekaSuitMoreModulesItem.MODULE_HP_BOOTS, builder -> builder.maxStackSize(64).noDisable());
    //抢夺强化单元（提高MekaTool的抢夺效果）
    public static final ModuleRegistryObject<ModuleLootingAmplificationUnit> LOOTING_AMPLIFICATION_UNIT = MODULES.register("looting_amplification_unit", ModuleLootingAmplificationUnit::new, () -> MekaSuitMoreModulesItem.MODULE_LOOTING_AMPLIFICATION, builder -> builder.maxStackSize(ModuleLootingAmplificationUnit.MAX_MODULES).disabledByDefault()
            .addInstalledCountConfig(
                    installed -> ModuleEnumConfig.createBounded(ModuleLootingAmplificationUnit.LOOTING_LEVEL, ModuleLootingAmplificationUnit.LootingLevel.LOW, Math.min(ModuleLootingAmplificationUnit.LootingLevel.values().length, installed + 1)),
                    installed -> ModuleEnumConfig.codec(ModuleLootingAmplificationUnit.LootingLevel.CODEC, ModuleLootingAmplificationUnit.LootingLevel.class, Math.min(ModuleLootingAmplificationUnit.LootingLevel.values().length, installed + 1)),
                    installed -> ModuleEnumConfig.streamCodec(ModuleLootingAmplificationUnit.LootingLevel.STREAM_CODEC, ModuleLootingAmplificationUnit.LootingLevel.class, Math.min(ModuleLootingAmplificationUnit.LootingLevel.values().length, installed + 1))
            ));
    // MekaTool生产力强化：放大原版挖掘效率和攻击伤害
    public static final ModuleRegistryObject<ModuleMekaToolPerformanceAmplificationUnit> MEKA_TOOL_PERFORMANCE_AMPLIFICATION_UNIT = MODULES.registerInstanced(
            "meka_tool_performance_amplification_unit",
            ModuleMekaToolPerformanceAmplificationUnit::new,
            () -> MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_PERFORMANCE_AMPLIFICATION,
            builder -> builder.maxStackSize(ModuleMekaToolPerformanceAmplificationUnit.MAX_MODULES).disabledByDefault());
    // MekaTool爆炸球发射器：可在重型、标准、速射三种模式间选择
    public static final ModuleRegistryObject<ModuleMekaToolBlasterUnit> MEKA_TOOL_BLASTER_UNIT = MODULES.register(
            "meka_tool_blaster_unit",
            ModuleMekaToolBlasterUnit::new,
            () -> MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_BLASTER,
            builder -> builder.maxStackSize(1).disabledByDefault().handlesModeChange().exclusive(1)
                    .addConfig(
                            ModuleEnumConfig.create(ModuleMekaToolBlasterUnit.FIRE_MODE, ModuleMekaToolBlasterUnit.FireMode.STANDARD),
                            ModuleEnumConfig.codec(ModuleMekaToolBlasterUnit.FireMode.CODEC),
                            ModuleEnumConfig.streamCodec(ModuleMekaToolBlasterUnit.FireMode.STREAM_CODEC)));
    // MekaTool antimatter orb launcher: one fixed, nuclear-scale strike mode.
    public static final ModuleRegistryObject<ModuleMekaToolAntimatterStrikeUnit> MEKA_TOOL_ANTIMATTER_STRIKE_UNIT = MODULES.registerInstanced(
            "meka_tool_antimatter_strike_unit",
            ModuleMekaToolAntimatterStrikeUnit::new,
            () -> MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_ANTIMATTER_STRIKE,
            builder -> builder.maxStackSize(1).disabledByDefault().exclusive(1));
    // MekaTool lava tank expansion: each installed module adds 200,000 mB, up to five modules.
    public static final ModuleRegistryObject<ModuleMekaToolLavaTankUnit> MEKA_TOOL_LAVA_TANK_UNIT = MODULES.registerInstanced(
            "meka_tool_lava_tank_unit",
            ModuleMekaToolLavaTankUnit::new,
            () -> MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_LAVA_TANK,
            builder -> builder.maxStackSize(5).disabledByDefault());


}
