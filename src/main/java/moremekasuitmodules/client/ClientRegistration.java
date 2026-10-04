package moremekasuitmodules.client;

import moremekasuitmodules.client.render.hud.MoreMekaSuitModulesHUD;
import moremekasuitmodules.client.render.item.MekaToolLavaBarDecorator;
import moremekasuitmodules.client.render.entity.AntimatterExplosiveOrbModel;
import moremekasuitmodules.client.render.entity.AntimatterExplosiveOrbRenderer;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.registries.MoreMekaSuitModulesEntities;
import mekanism.common.registries.MekanismItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;


@EventBusSubscriber(modid = MoreMekaSuitModules.MODID, value = Dist.CLIENT)
public class ClientRegistration {

    private ClientRegistration() {
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientTickHandler());
    }


    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, MoreMekaSuitModules.rl("shield_hud"), MoreMekaSuitModulesHUD.INSTANCE);
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(MekanismItems.MEKA_TOOL, MekaToolLavaBarDecorator.INSTANCE);
    }

    @SubscribeEvent
    public static void registerOrbLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AntimatterExplosiveOrbModel.LAYER_LOCATION,
                AntimatterExplosiveOrbModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerOrbRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MoreMekaSuitModulesEntities.ANTIMATTER_EXPLOSIVE_ORB.get(),
                AntimatterExplosiveOrbRenderer::new);
    }
}
