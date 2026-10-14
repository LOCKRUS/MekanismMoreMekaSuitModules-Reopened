package moremekasuitmodules.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * A positional, looping Fusion Reactor bed used while the antimatter strike charges.
 * The sound is deliberately driven by the charge envelope instead of being restarted
 * every tick, so interruption and completion both fade naturally.
 */
public final class AntimatterChargeSoundInstance extends AbstractTickableSoundInstance {
    private static final ResourceLocation FUSION_REACTOR_SOUND =
            ResourceLocation.fromNamespaceAndPath("mekanismgenerators", "tile.machine.fusion_reactor");
    private static final float MAX_VOLUME = 1.1F;
    private static final float FADE_SPEED = 0.16F;

    private float targetVolume;
    private float targetPitch = 0.75F;
    private boolean fadingOut;

    public AntimatterChargeSoundInstance(Vec3 position) {
        super(SoundEvent.createVariableRangeEvent(FUSION_REACTOR_SOUND), SoundSource.MASTER, RandomSource.create());
        this.looping = true;
        this.volume = 0.025F;
        this.pitch = targetPitch;
        this.attenuation = Attenuation.NONE;
        this.relative = true;
        setPosition(position);
    }

    public void update(Vec3 position, float chargeProgress) {
        setPosition(position);
        float envelope = Mth.sin(Mth.PI * Mth.clamp(chargeProgress, 0.0F, 1.0F));
        // Keep a tiny audible bed at the first tick, then let the envelope rise.
        targetVolume = 0.025F + MAX_VOLUME * envelope;
        targetPitch = 0.68F + 0.28F * Mth.clamp(chargeProgress, 0.0F, 1.0F);
        fadingOut = false;
    }

    public void fadeOut(Vec3 position) {
        setPosition(position);
        targetVolume = 0.0F;
        fadingOut = true;
    }

    @Override
    public void tick() {
        volume = approach(volume, targetVolume, FADE_SPEED);
        pitch = approach(pitch, targetPitch, 0.035F);
        if (fadingOut && volume <= 0.008F) {
            stop();
        }
    }

    private void setPosition(Vec3 position) {
        x = position.x;
        y = position.y;
        z = position.z;
    }

    private static float approach(float current, float target, float step) {
        return current + Mth.clamp(target - current, -step, step);
    }
}
