package fi.ugim.conflagration.model.bombtypes;

import fi.ugim.conflagration.Conflagration;
import fi.ugim.conflagration.model.CustomBomb;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.InstrumentTypes;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.TrigMath;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class Smoke extends CustomBomb {

    private final Conflagration plugin;

    public Smoke(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 18;
        this.damage = 0;
        this.radius = 10;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#fa8bff:#2bd2ff>Savu TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.F2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 5f, 1f), position);

        AtomicInteger i = new AtomicInteger(0);
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(this.plugin.container())
                .interval(Ticks.of(1))
                .execute(task -> {
                    if (i.incrementAndGet() > 1000) {
                        task.cancel();
                        return;
                    }

                    for (Entity entity : serverWorld.nearbyEntities(position, this.radius)) {
                        entity.offer(Keys.POTION_EFFECTS, List.of(
                                PotionEffect.builder()
                                        .potionType(PotionEffectTypes.BLINDNESS)
                                        .amplifier(3)
                                        .duration(Ticks.of(20))
                                        .ambient(false)
                                        .showParticles(false)
                                        .showIcon(false)
                                        .build(),
                                PotionEffect.builder()
                                        .potionType(PotionEffectTypes.DARKNESS)
                                        .amplifier(3)
                                        .duration(Ticks.of(20))
                                        .ambient(false)
                                        .showParticles(false)
                                        .showIcon(false)
                                        .build())
                        );
                    }

                    final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                    AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                        for (int j = 0; j < 300; j++) {
                            final Vector3d direction = createRandomDirection(RANDOM);
                            final Vector3d smokePosition = position.add(direction.mul(Math.sqrt(Math.random()) * this.radius));
                            for (ServerPlayer updatedServerPlayer : updatedServerPlayers) {
                                updatedServerPlayer.spawnParticles(ParticleEffect.builder()
                                        .type(ParticleTypes.LARGE_SMOKE)
                                        .build(), smokePosition);
                            }
                        }
                    });
                }).build());
    }

    public static Vector3d createRandomDirection(final Random random) {
        return createDirectionRad(random.nextDouble() * TrigMath.TWO_PI,
                Math.acos(2 * random.nextDouble() - 1));
    }

    public static Vector3d createDirectionRad(final double theta, final double phi) {
        final double f = TrigMath.sin(phi);
        return new Vector3d(f * TrigMath.cos(theta), f * TrigMath.sin(theta), TrigMath.cos(phi));
    }
}
