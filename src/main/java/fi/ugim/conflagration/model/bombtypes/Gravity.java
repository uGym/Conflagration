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
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.*;

public class Gravity extends CustomBomb {

    final Conflagration plugin;

    public Gravity(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 8;
        this.damage = 0;
        this.radius = 20;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#e0c3fc:#8ec5fc>Gravitaatio TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.C2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_EVOKER_PREPARE_SUMMON, Sound.Source.BLOCK, 5f, 0f), position);

        var radius = new Vector3d(1, 0, 0);

        AtomicInteger i = new AtomicInteger(0);
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(plugin.container())
                .delay(Ticks.of(20))
                .interval(Ticks.of(1))
                .execute(task -> {
                    if (i.incrementAndGet() > 1000) {
                        task.cancel();
                        return;
                    }
                    for (Entity entity : serverWorld.nearbyEntities(position, 20)) {
                        entity.offer(Keys.POTION_EFFECTS, List.of(PotionEffect.builder()
                                .potionType(PotionEffectTypes.LEVITATION)
                                .amplifier(3)
                                .duration(Ticks.of(Math.max(1, Math.min(1000 - i.get(), 200))))
                                .ambient(false)
                                .showParticles(false)
                                .showIcon(false)
                                .build()));
                    }
                    serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WARDEN_AGITATED, Sound.Source.BLOCK, 3f, 0f), position);
                    serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_DEACTIVATE, Sound.Source.BLOCK, 4f, 0f), position);
                    for (int j = 0; j < 5; j++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double distance = Math.sqrt(Math.random()) * this.radius;
                        var xOffSet = distance * Math.cos(angle);
                        var zOffSet = distance * Math.sin(angle);
                        var pos = serverWorld.highestPositionAt(new Vector3i(xOffSet, 0, zOffSet).add(position.toInt())).sub(0, 1, 0);
                        BlockState blockState = serverWorld.block(pos);
                        Entity entity = serverWorld.createEntity(EntityTypes.BLOCK_DISPLAY, pos);
                        entity.offer(Keys.BLOCK_STATE, BlockState.builder().blockType(blockState.type()).build());
                        final Vector3d locationTransform = new Vector3d(0, RANDOM.nextInt(50, 100), 0);
                        final Vector3d rotationTransform = Vector3d.createRandomDirection(RANDOM).mul(360);
                        final int pathTime = 200;
                        serverWorld.spawnEntity(entity);
                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(1))
                                .execute(() -> {
                                    entity.offer(Keys.TRANSFORM, Transform.of(locationTransform.mul(0.8), rotationTransform.mul(0.8)));
                                    entity.offer(Keys.INTERPOLATION_DURATION, Ticks.of((long) (pathTime * 0.8)));
                                    entity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(1));
                                    serverWorld.setBlock(pos, BlockState.builder().blockType(BlockTypes.AIR).build());
                                    serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_STONE_BREAK, Sound.Source.BLOCK, 1f, 1f), pos.toDouble());
                                    Sponge.server().scheduler().submit(Task.builder()
                                            .plugin(plugin.container())
                                            .delay(Ticks.of((long) (pathTime * 0.8)))
                                            .execute(() -> {
                                                entity.offer(Keys.TRANSFORM, Transform.of(locationTransform, rotationTransform, Vector3d.ZERO));
                                                entity.offer(Keys.INTERPOLATION_DURATION, Ticks.of((long) (pathTime * 0.2)));
                                                entity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(1));
                                                Sponge.server().scheduler().submit(Task.builder()
                                                        .plugin(plugin.container())
                                                        .delay(Ticks.of((long) (pathTime * 0.2)))
                                                        .execute(() -> entity.remove()).build());
                                            }).build());
                                }).build());
                    }


                }).build());

        for (int j = 0; j < 100; j++) {
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of(j * 10 + 40))
                    .execute(() -> {
                        final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                        for (double theta = 0; theta < Math.PI * 2; theta += Math.PI * 0.01) {
                            final Vector3d direction = rotateY(radius.mul(this.radius), theta);
                            AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                                for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                    serverPlayer.spawnParticles(ParticleEffect.builder()
                                            .type(ParticleTypes.GLOW_SQUID_INK)
                                            .velocity(Vector3d.UP.mul(3))
                                            .build(), position.add(direction));
                                }
                            });
                        }
                    }).build());
        }

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int j = 0; j < 20; j++) {
                var angle_x = Math.PI * 2 * Math.random();
                var angle_z = Math.PI * 2 * Math.random();
                for (double theta = 0; theta < Math.PI * 2; theta += Math.PI * 0.003) {
                    var direction = rotateX(rotateZ(rotateY(radius, theta), angle_z), angle_x);
                    for (ServerPlayer serverPlayer : serverPlayers) {
                        serverPlayer.spawnParticles(ParticleEffect.builder()
                                .type(ParticleTypes.GLOW_SQUID_INK)
                                .velocity(direction.mul(this.radius / 8))
                                .build(), position);
                    }
                }
            }
        });

    }
}
