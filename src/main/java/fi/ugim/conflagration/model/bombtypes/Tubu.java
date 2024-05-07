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
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;
import static fi.ugim.conflagration.utils.MathUtil.layeredSphere;

public class Tubu extends CustomBomb {

    public Conflagration plugin;

    public Tubu(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 20;
        this.damage = 0;
        this.radius = 50;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#ffecd2:#ef629f>Tubu TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.F_SHARP1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
        final List<Set<Vector3i>> blockPositions = layeredSphere(position.toInt(), (int) this.radius).stream()
                .map(shell -> shell.stream()
                        .filter(blockPosition -> blockPosition.distance(position.toInt()) * Math.random() < this.radius / 2
                        && serverWorld.block(blockPosition).type() != BlockTypes.AIR.get())
                        .collect(Collectors.toSet()))
                .toList();

        for (int i = 0; i < blockPositions.size(); i++) {
            int finalI = i;
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.plugin.container())
                    .delay(Ticks.of(2L * finalI))
                    .execute(() -> {
                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(40))
                                .execute(() -> {
                                    final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                                    AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                                        for (int j = 0; j < 60; j++) {
                                            final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random()).mul(0.5);
                                            for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                                serverPlayer.spawnParticles(ParticleEffect.builder()
                                                        .type(ParticleTypes.SCULK_SOUL)
                                                        .velocity(direction)
                                                        .build(), position);
                                            }
                                        }
                                    });
                                }).build());

                        for (Vector3i blockPosition : blockPositions.get(finalI)) {
                            final BlockState blockState = serverWorld.block(blockPosition);

                            final Vector3d locationTransform = position.sub(blockPosition.toDouble());
                            serverWorld.setBlock(blockPosition, BlockTypes.AIR.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);

                            if (!blockState.get(Keys.IS_REPLACEABLE).orElse(false)) {
                                if (Math.random() < 0.005 * (blockPositions.size() - finalI)) {
                                    final Entity blockEntity = serverWorld.createEntity(EntityTypes.BLOCK_DISPLAY, blockPosition);
                                    blockEntity.offer(Keys.BLOCK_STATE, blockState);
                                    serverWorld.spawnEntity(blockEntity);
                                    Sponge.server().scheduler().submit(Task.builder()
                                            .plugin(plugin.container())
                                            .delay(Ticks.of(2))
                                            .execute(() -> {
                                                final int duration = RANDOM.nextInt(10, 40);
                                                blockEntity.offer(Keys.TRANSFORM, Transform.of(locationTransform, Vector3d.ONE.mul((Math.random() - 0.5) * 360), Vector3d.ONE.mul(0.2)));
                                                blockEntity.offer(Keys.INTERPOLATION_DURATION, Ticks.of(duration));
                                                blockEntity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(0));
                                                Sponge.server().scheduler().submit(Task.builder()
                                                        .plugin(plugin.container())
                                                        .delay(Ticks.of(duration))
                                                        .execute(() -> blockEntity.remove()).build());
                                            }).build());
                                }
                            }

                        }
                    }).build());
        }
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_POWER_SELECT, Sound.Source.MASTER, 5f, 0f), position);

        Sponge.server().scheduler().submit(Task.builder()
                .plugin(plugin.container())
                .delay(Ticks.of((long) (blockPositions.size() * 1.5)))
                .execute(() -> {
                    for (int j = 0; j < 30; j++) {
                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(2 * j))
                                .execute(() -> {
                                    final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                                    AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                                        for (int i = 0; i < 40; i++) {
                                            final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random());
                                            for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                                serverPlayer.spawnParticles(ParticleEffect.builder()
                                                        .type(ParticleTypes.GLOW_SQUID_INK)
                                                        .velocity(direction.mul(-2))
                                                        .build(), position.add(direction.mul(14)));
                                            }
                                        }
                                    });
                                }).build());
                    }
                    serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WARDEN_SONIC_CHARGE, Sound.Source.MASTER, 5f, 0f), position);
                }).build());

        Sponge.server().scheduler().submit(Task.builder()
                .plugin(plugin.container())
                .delay(Ticks.of(blockPositions.size() * 3L))
                .execute(() -> {

                        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_EVOKER_CAST_SPELL, Sound.Source.MASTER, 7f, 2f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ILLUSIONER_CAST_SPELL, Sound.Source.MASTER, 7f, 2f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.Source.MASTER, 7f, 2f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 7f, 7f), position);

                        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_EVOKER_CAST_SPELL, Sound.Source.MASTER, 7f, 1f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ILLUSIONER_CAST_SPELL, Sound.Source.MASTER, 7f, 1f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.Source.MASTER, 7f, 1f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 7f, 1f), position);

                        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_EVOKER_CAST_SPELL, Sound.Source.MASTER, 8f, 0.5f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ILLUSIONER_CAST_SPELL, Sound.Source.MASTER, 8f, 0.5f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.Source.MASTER, 8f, 0.5f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 8f, 0.5f), position);

                        for (int i = 0; i < 40; i++) {
                            final Vector3d direction = Vector3d.createRandomDirection(RANDOM);
                            AtomicReference<Vector3d> rayPosition = new AtomicReference<>();
                            for (double length = 0; length < this.radius * 1.5; length += 0.8) {
                                double finalLength = length;
                                Sponge.server().scheduler().submit(Task.builder()
                                        .plugin(plugin.container())
                                        .delay(Ticks.of((long) (length / 4)))
                                        .execute(() -> {
                                            rayPosition.set(position.add(direction.mul(finalLength)));
                                            final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                                            AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                                                for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                                    serverPlayer.spawnParticles(ParticleEffect.builder()
                                                            .type(ParticleTypes.SONIC_BOOM)
                                                            .build(), rayPosition.get());
                                                }
                                            });
                                            if (serverWorld.block(rayPosition.get().toInt()).type() != BlockTypes.AIR.get()) {
                                                Vector3d finalRayPosition = rayPosition.get();
                                                Sponge.server().scheduler().submit(Task.builder()
                                                        .plugin(plugin.container())
                                                        .delay(Ticks.of(10))
                                                        .execute(() -> {
                                                                final Collection<ServerPlayer> secondaryUpdatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                                                                AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                                                                    for (int j = 0; j < 60; j++) {
                                                                        final Vector3d particleDirection = Vector3d.createRandomDirection(RANDOM).mul(Math.random()).mul(3);
                                                                        for (ServerPlayer serverPlayer : secondaryUpdatedServerPlayers) {
                                                                            serverPlayer.spawnParticles(ParticleEffect.builder()
                                                                                    .type(ParticleTypes.GLOW_SQUID_INK)
                                                                                    .velocity(particleDirection)
                                                                                    .build(), finalRayPosition);
                                                                        }
                                                                    }
                                                                });
                                                                serverWorld.triggerExplosion(Explosion.builder()
                                                                        .shouldBreakBlocks(true)
                                                                        .knockback(5.0)
                                                                        .radius(8)
                                                                        .canCauseFire(false)
                                                                        .shouldDamageEntities(true)
                                                                        .location(serverWorld.location(finalRayPosition))
                                                                        .build());
                                                        }).build());
                                            }
                                        }).build());

                            }
                        }
                }).build());
    }
}
