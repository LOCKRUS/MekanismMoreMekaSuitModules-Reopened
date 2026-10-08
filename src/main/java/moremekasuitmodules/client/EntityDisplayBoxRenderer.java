package moremekasuitmodules.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleEntityDisplayBoxUnit;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = MoreMekaSuitModules.MODID, value = Dist.CLIENT)
public final class EntityDisplayBoxRenderer {
    private static final double SCREEN_PADDING = 2.0D;
    private static final int LABEL_GAP = 4;
    private static final int HEALTH_BAR_WIDTH = 3;
    private static final int HEALTH_BAR_GAP = 3;
    private static final int HEALTH_TEXT_GAP = 2;
    private static final float HEALTH_TEXT_MAX_DISTANCE = 24.0F;
    private static final List<DisplayBox> BOXES = new ArrayList<>();
    private static int screenWidth;
    private static int screenHeight;

    private EntityDisplayBoxRenderer() {
    }

    @SubscribeEvent
    public static void collect(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        BOXES.clear();
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }
        IModule<ModuleEntityDisplayBoxUnit> module = IModuleHelper.INSTANCE.getIfEnabled(
                player.getItemBySlot(EquipmentSlot.HEAD), MekaSuitMoreModules.ENTITY_DISPLAY_BOX_UNIT);
        if (module == null) {
            return;
        }
        ModuleEntityDisplayBoxUnit unit = module.getCustomInstance();
        int radius = unit.rangeBlocks();
        if (radius <= 0) {
            return;
        }
        screenWidth = minecraft.getWindow().getGuiScaledWidth();
        screenHeight = minecraft.getWindow().getGuiScaledHeight();
        double radiusSq = radius * radius;
        AABB search = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = minecraft.level.getEntitiesOfClass(LivingEntity.class, search,
                target -> isValidTarget(player, target, radiusSq));
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        Matrix4f modelView = event.getModelViewMatrix();
        Matrix4f projection = event.getProjectionMatrix();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (LivingEntity target : targets) {
            if (BOXES.size() >= unit.maxBoxCount()) {
                break;
            }
            AABB box = paddedBox(target, partial);
            ScreenBounds bounds = project(box, modelView, projection);
            if (bounds != null && bounds.isVisible(screenWidth, screenHeight)) {
                BOXES.add(new DisplayBox(bounds.left - SCREEN_PADDING, bounds.top - SCREEN_PADDING,
                        bounds.right + SCREEN_PADDING, bounds.bottom + SCREEN_PADDING,
                        target.getName(), player.distanceTo(target), target.getHealth(), target.getMaxHealth(),
                        unit.boxColor(), unit.nameColor(), unit.distanceColor(), unit.drawHealthBar(), unit.drawHealthText()));
            }
        }
    }

    @SubscribeEvent
    public static void draw(RenderGuiEvent.Post event) {
        if (BOXES.isEmpty()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = Minecraft.getInstance().font;
        for (DisplayBox box : BOXES) {
            int left = (int) Math.floor(box.left);
            int top = (int) Math.floor(box.top);
            int right = (int) Math.ceil(box.right);
            int bottom = (int) Math.ceil(box.bottom);
            graphics.fill(left, top, right + 1, top + 1, box.boxColor);
            graphics.fill(left, bottom, right + 1, bottom + 1, box.boxColor);
            graphics.fill(left, top, left + 1, bottom + 1, box.boxColor);
            graphics.fill(right, top, right + 1, bottom + 1, box.boxColor);
            drawHealth(graphics, font, box, left, top, bottom);
            drawLabels(graphics, font, box, left, top, right, bottom);
        }
    }

    private static boolean isValidTarget(Player player, LivingEntity target, double radiusSq) {
        return target != player && target.isAlive() && !(target instanceof Player other && other.isSpectator())
                && player.distanceToSqr(target) <= radiusSq;
    }

    private static AABB paddedBox(LivingEntity entity, float partial) {
        AABB current = entity.getBoundingBox();
        double horizontal = Math.max(0.2D, entity.getBbWidth() * 0.18D);
        double vertical = Math.max(0.05D, entity.getBbHeight() * 0.08D);
        double top = Math.max(0.1D, entity.getBbHeight() * 0.12D);
        // RenderLevelStageEvent already exposes the render-time camera matrix.
        // Applying xOld/yOld/zOld here as well caused a second interpolation and
        // made the screen box visibly trail the entity.
        return new AABB(current.minX - horizontal, current.minY - vertical, current.minZ - horizontal,
                current.maxX + horizontal, current.maxY + top, current.maxZ + horizontal);
    }

    private static ScreenBounds project(AABB box, Matrix4f modelView, Matrix4f projection) {
        ScreenBounds bounds = new ScreenBounds();
        double[] xs = {box.minX, box.maxX};
        double[] ys = {box.minY, box.maxY};
        double[] zs = {box.minZ, box.maxZ};
        for (double x : xs) for (double y : ys) for (double z : zs) {
            Vector4f point = new Vector4f((float) x, (float) y, (float) z, 1.0F).mul(modelView).mul(projection);
            if (point.w <= 0.0F) continue;
            float nx = point.x / point.w;
            float ny = point.y / point.w;
            float nz = point.z / point.w;
            if (nz < -1.0F || nz > 1.0F) continue;
            double sx = (nx * 0.5D + 0.5D) * screenWidth;
            double sy = (1.0D - (ny * 0.5D + 0.5D)) * screenHeight;
            bounds.include(sx, sy);
        }
        return bounds.valid() ? bounds : null;
    }

    private static void drawLabels(GuiGraphics graphics, Font font, DisplayBox box, int left, int top, int right, int bottom) {
        String distance = String.format(java.util.Locale.ROOT, "%.1fm", box.distance);
        int nameWidth = font.width(box.name);
        int distanceWidth = font.width(distance);
        if (right - left >= nameWidth + distanceWidth + LABEL_GAP) {
            drawClamped(graphics, font, box.name, left, top - font.lineHeight - 1, box.nameColor);
            drawClamped(graphics, font, Component.literal(distance), right - distanceWidth, top - font.lineHeight - 1, box.distanceColor);
        } else {
            int center = (left + right) / 2;
            drawClamped(graphics, font, box.name, center - nameWidth / 2, top - font.lineHeight * 2 - 2, box.nameColor);
            drawClamped(graphics, font, Component.literal(distance), center - distanceWidth / 2, top - font.lineHeight - 1, box.distanceColor);
        }
    }

    private static void drawHealth(GuiGraphics graphics, Font font, DisplayBox box, int left, int top, int bottom) {
        if ((!box.healthBar && !box.healthText) || box.maxHealth <= 0.0F) return;
        float ratio = Math.max(0.0F, Math.min(1.0F, box.health / box.maxHealth));
        if (box.healthBar) {
            int barRight = Math.max(HEALTH_BAR_WIDTH + 1, left - HEALTH_BAR_GAP);
            int barLeft = barRight - HEALTH_BAR_WIDTH;
            graphics.fill(barLeft - 1, top - 1, barRight + 1, bottom + 1, 0xAA000000);
            graphics.fill(barLeft, top, barRight, bottom, 0xAA202020);
            int fillTop = bottom - Math.max(1, Math.round((bottom - top) * ratio));
            graphics.fill(barLeft, fillTop, barRight, bottom, healthColor(ratio));
        }
        if (box.healthText && box.distance <= HEALTH_TEXT_MAX_DISTANCE) {
            String text = formatHealth(box.health) + "/" + formatHealth(box.maxHealth);
            int x = Math.max(1, left - HEALTH_BAR_GAP - HEALTH_BAR_WIDTH - HEALTH_TEXT_GAP - font.width(text));
            drawClamped(graphics, font, Component.literal(text), x, top, healthColor(ratio));
        }
    }

    private static int healthColor(float ratio) { return ratio > 0.5F ? 0xFF3CFE9A : ratio > 0.25F ? 0xFFFFFF55 : 0xFFFF5555; }
    private static String formatHealth(float value) { return Math.abs(value - Math.round(value)) < 0.05F ? Integer.toString(Math.round(value)) : String.format(java.util.Locale.ROOT, "%.1f", value); }
    private static void drawClamped(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        int maxX = Math.max(1, screenWidth - font.width(text) - 1);
        graphics.drawString(font, text, Math.max(1, Math.min(x, maxX)), Math.max(1, Math.min(y, screenHeight - font.lineHeight - 1)), color, true);
    }

    private static final class ScreenBounds {
        double left = Double.POSITIVE_INFINITY, top = Double.POSITIVE_INFINITY;
        double right = Double.NEGATIVE_INFINITY, bottom = Double.NEGATIVE_INFINITY;
        void include(double x, double y) { left = Math.min(left, x); top = Math.min(top, y); right = Math.max(right, x); bottom = Math.max(bottom, y); }
        boolean valid() { return left < right && top < bottom; }
        boolean isVisible(int width, int height) { return valid() && right >= 0 && bottom >= 0 && left <= width && top <= height; }
    }

    private record DisplayBox(double left, double top, double right, double bottom, Component name,
                              float distance, float health, float maxHealth, int boxColor, int nameColor,
                              int distanceColor, boolean healthBar, boolean healthText) {}
}
