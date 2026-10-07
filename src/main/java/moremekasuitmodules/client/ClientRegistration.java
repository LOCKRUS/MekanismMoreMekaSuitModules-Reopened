package moremekasuitmodules.client;

import moremekasuitmodules.client.render.hud.MoreMekaSuitModulesHUD;
import moremekasuitmodules.client.render.entity.AntimatterExplosiveOrbModel;
import moremekasuitmodules.client.render.entity.AntimatterExplosiveOrbRenderer;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import moremekasuitmodules.common.registries.MoreMekaSuitModulesEntities;
import mekanism.common.item.gear.ItemMekaTool;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.network.chat.Component;


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
    public static void addMekaToolLavaTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof ItemMekaTool)) {
            return;
        }
        IFluidHandlerItem tank = MekaToolLavaHandler.create(event.getItemStack());
        if (tank != null && tank.getTanks() > 0) {
            event.getToolTip().add(Component.translatable("tooltip.moremekasuitmodules.lava_storage",
                    tank.getFluidInTank(0).getAmount(), tank.getTankCapacity(0)));
        }
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
