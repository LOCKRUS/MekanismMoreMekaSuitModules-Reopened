package moremekasuitmodules.common;

import mekanism.api.MekanismIMC;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.lib.Version;
import moremekasuitmodules.common.config.MoreModulesConfig;
import moremekasuitmodules.common.integration.MoreMekaSuitModulesHooks;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import moremekasuitmodules.common.registries.MekaSuitMoreModulesCreativeTabs;
import moremekasuitmodules.common.registries.MekaSuitMoreModulesItem;
import moremekasuitmodules.common.registries.MoreMekaSuitModulesEntities;
import moremekasuitmodules.common.registries.MoreMekaSuitModulesDataComponents;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolBlasterHandler;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.CounterattackHandler;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ImpactWaveHandler;
import moremekasuitmodules.common.network.AntimatterShockwavePayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(MoreMekaSuitModules.MODID)
public class MoreMekaSuitModules implements IModModule {

    public static final String MODID = "moremekasuitmodules";

    public static MoreMekaSuitModules instance;

    public final Version versionNumber;

    public static MoreMekaSuitModulesHooks hooks;

    public MoreMekaSuitModules(ModContainer modContainer, IEventBus modEventBus) {
        Mekanism.addModule(instance = this);
        versionNumber = new Version(modContainer);
        MoreModulesConfig.registerConfigs(modContainer);
        hooks = new MoreMekaSuitModulesHooks();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(MekaToolLavaHandler::register);
        modEventBus.addListener(MoreModulesConfig::onConfigLoad);
        modEventBus.addListener(this::imcQueue);
        MoreMekaSuitModulesDataComponents.DATA_COMPONENTS.register(modEventBus);
        MekaSuitMoreModulesItem.ITEMS.register(modEventBus);
        MekaSuitMoreModules.MODULES.register(modEventBus);
        if (hooks.DraconicEvolutionLoaded.isLoaded()) {

        }
        if (hooks.IceAndFireLoaded.isLoaded()) {
            //   modEventBus.addListener(iceandfireImcQueue::imcQueue);
            //   iceAndFireModulesItem.ITEMS.register(modEventBus);
            //   iceAndFireModules.MODULES.register(modEventBus);
        }
        if (hooks.BotaniaLoaded.isLoaded()) {
            //    modEventBus.addListener(botaniaImcQueue::imcQueue);
            //    botaniaModulesItem.ITEMS.register(modEventBus);
            //    botaniaModules.MODULES.register(modEventBus);
        }
        MekaSuitMoreModulesCreativeTabs.CREATIVE_TABS.register(modEventBus);
        MoreMekaSuitModulesEntities.ENTITY_TYPES.register(modEventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MoreMekaSuitModules.MODID, path);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                AntimatterShockwavePayload.TYPE,
                AntimatterShockwavePayload.STREAM_CODEC,
                AntimatterShockwavePayload::handle);
    }

    private void imcQueue(InterModEnqueueEvent event) {
        MekanismIMC.addModulesToAll(
                MekaSuitMoreModules.INFINITE_ENERGY_SUPPLY_UNIT
        );
        //meka套全部
        MekanismIMC.addMekaSuitModules(
                MekaSuitMoreModules.INSULATED_UNIT,
                MekaSuitMoreModules.AUTOMATIC_EXTINGUISH_UNIT,
                MekaSuitMoreModules.COUNTERATTACK_UNIT,
                MekaSuitMoreModules.ENERGY_SHIELD_UNIT,
                MekaSuitMoreModules.POWER_ENHANCEMENT_UNIT,
                MekaSuitMoreModules.HP_BOOTS_UNIT
        );
        MekanismIMC.addMekaSuitBootsModules(
                MekaSuitMoreModules.WALL_CLING_UNIT,
                MekaSuitMoreModules.IMPACT_WAVE_UNIT);
        //meka头盔
        MekanismIMC.addMekaSuitHelmetModules(
                MekaSuitMoreModules.EMERGENCY_RESCUE_UNIT,
                MekaSuitMoreModules.ADVANCED_INTERCEPTION_SYSTEM_UNIT,
                MekaSuitMoreModules.INFINITE_INTERCEPTION_AND_RESCUE_SYSTEM_UNIT,
                MekaSuitMoreModules.ENERGY_SHIELD_CONTROLLER_UNIT,
                MekaSuitMoreModules.AUTOMATIC_ATTACK_UNIT);


        //meka护甲
        MekanismIMC.addMekaSuitBodyarmorModules(
                MekaSuitMoreModules.HEALTH_REGENERATION_UNIT,
                MekaSuitMoreModules.INFINITE_CHEMICAL_AND_FLUID_SUPPLY_UNIT,
                MekaSuitMoreModules.HIGH_SPEED_COOLING_UNIT,
                MekaSuitMoreModules.QUANTUM_RECONSTRUCTION_UNIT,
                MekaSuitMoreModules.FLIGHT_UNIT);
        MekanismIMC.addMekaToolModules(
                MekaSuitMoreModules.LOOTING_AMPLIFICATION_UNIT,
                MekaSuitMoreModules.MEKA_TOOL_PERFORMANCE_AMPLIFICATION_UNIT,
                MekaSuitMoreModules.MEKA_TOOL_BLASTER_UNIT,
                MekaSuitMoreModules.MEKA_TOOL_ANTIMATTER_STRIKE_UNIT,
                MekaSuitMoreModules.MEKA_TOOL_LAVA_TANK_UNIT);
        //meka护腿

        //meka靴子
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new CommonPlayerTickHandler());
        NeoForge.EVENT_BUS.register(new ShieldProviderHandler());
        NeoForge.EVENT_BUS.register(new MekaToolBlasterHandler());
        NeoForge.EVENT_BUS.register(new CounterattackHandler());
        NeoForge.EVENT_BUS.register(new ImpactWaveHandler());
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "MoreMekaSuitModules";
    }

    @Override
    public void resetClient() {

    }
}
