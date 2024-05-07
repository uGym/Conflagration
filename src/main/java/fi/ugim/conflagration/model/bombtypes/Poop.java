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
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;
import static fi.ugim.conflagration.utils.MathUtil.sphere;

public class Poop extends CustomBomb {

    private final Conflagration plugin;

    public Poop(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 15;
        this.damage = 0;
        this.radius = 14;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#84fab0:#8fd3f4>Kakka TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.E1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
        final Set<Vector3i> blockPositions = sphere(position.toInt(), (int) this.radius).stream().filter(blockPosition -> blockPosition.distance(position.toInt()) * Math.random() < this.radius / 2).collect(Collectors.toSet());

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_DRAGON_FIREBALL_EXPLODE, Sound.Source.MASTER, 5f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_POLAR_BEAR_DEATH, Sound.Source.MASTER, 5f, 2f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_POLAR_BEAR_HURT, Sound.Source.MASTER, 5f, 2f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ENDERMAN_HURT, Sound.Source.MASTER, 5f, 0f), position);

        for (Vector3i blockPosition : blockPositions) {
            final BlockState blockState = serverWorld.block(blockPosition);
            if (blockState.get(Keys.IS_REPLACEABLE).orElse(false) && !(blockState.type() == BlockTypes.WATER.get() || blockState.type() == BlockTypes.LAVA.get())) {
                serverWorld.setBlock(blockPosition, BlockTypes.AIR.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
            } else if (blockState.get(Keys.IS_SOLID).orElse(false)) {
                serverWorld.setBlock(blockPosition, BlockTypes.PODZOL.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
            }
        }

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (ServerPlayer serverPlayer : serverPlayers) {
                for (int i = 0; i < 2000; i++) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.SNEEZE)
                            .velocity(Vector3d.createRandomDirection(RANDOM).mul(2))
                            .build(), position);
                }
                serverPlayer.spawnParticles(ParticleEffect.builder()
                        .type(ParticleTypes.BLOCK)
                        .quantity(1000)
                        .offset(Vector3d.ONE.mul(3))
                        .option(ParticleOptions.BLOCK_STATE, BlockState.builder()
                                .blockType(BlockTypes.COCOA)
                                .add(Keys.GROWTH_STAGE, 2)
                                .build())
                        .build(), position);
            }
        });

        for (int i = 0; i < 500; i++) {
            final Entity entity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, position);
            entity.offer(Keys.VELOCITY, Vector3d.createRandomDirection(RANDOM).mul(2));
            entity.offer(Keys.BLOCK_STATE, BlockTypes.DIRT.get().defaultState());
            entity.offer(Keys.DAMAGE_PER_BLOCK, 100.0);
            entity.offer(Keys.CAN_HURT_ENTITIES, true);
            entity.offer(Keys.MAX_FALL_DAMAGE, 2000.0);
            entity.offer(Keys.CAN_DROP_AS_ITEM, false);
            serverWorld.spawnEntity(entity);
        }
        for (int i = 0; i < 100; i++) {
            final Entity entity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, position);
            entity.offer(Keys.VELOCITY, Vector3d.createRandomDirection(RANDOM).mul(1.4));
            entity.offer(Keys.BLOCK_STATE, BlockTypes.SOUL_SAND.get().defaultState());
            entity.offer(Keys.DAMAGE_PER_BLOCK, 100.0);
            entity.offer(Keys.CAN_HURT_ENTITIES, true);
            entity.offer(Keys.MAX_FALL_DAMAGE, 2000.0);
            entity.offer(Keys.CAN_DROP_AS_ITEM, false);
            serverWorld.spawnEntity(entity);
        }

        for (int i = 0; i < 60; i++) {
            final Entity entity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, position);
            entity.offer(Keys.VELOCITY, Vector3d.createRandomDirection(RANDOM).mul(1.2));
            entity.offer(Keys.BLOCK_STATE, BlockTypes.ROOTED_DIRT.get().defaultState());
            entity.offer(Keys.DAMAGE_PER_BLOCK, 100.0);
            entity.offer(Keys.CAN_HURT_ENTITIES, true);
            entity.offer(Keys.MAX_FALL_DAMAGE, 2000.0);
            entity.offer(Keys.CAN_DROP_AS_ITEM, false);
            serverWorld.spawnEntity(entity);
        }
        for (int i = 0; i < 30; i++) {
            final Entity entity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, position);
            entity.offer(Keys.VELOCITY, Vector3d.createRandomDirection(RANDOM).mul(1));
            entity.offer(Keys.BLOCK_STATE, BlockTypes.COARSE_DIRT.get().defaultState());
            entity.offer(Keys.DAMAGE_PER_BLOCK, 100.0);
            entity.offer(Keys.CAN_HURT_ENTITIES, true);
            entity.offer(Keys.MAX_FALL_DAMAGE, 2000.0);
            entity.offer(Keys.CAN_DROP_AS_ITEM, false);
            serverWorld.spawnEntity(entity);
        }

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
                        entity.damage(1, DamageSources.MAGIC);
                    }
                    for (int j = 0; j < 20; j++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double distance = Math.sqrt(Math.random()) * this.radius;
                        var xOffSet = distance * Math.cos(angle);
                        var zOffSet = distance * Math.sin(angle);
                        var pos = serverWorld.highestPositionAt(new Vector3i(xOffSet, 0, zOffSet).add(position.toInt())).sub(0, 1, 0);
                        final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                            for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                serverPlayer.spawnParticles(ParticleEffect.builder()
                                        .type(ParticleTypes.SNEEZE)
                                        .velocity(Vector3d.UP.mul(0.2))
                                        .build(), pos.toDouble().add(0.5, 0, 0.5));
                            }
                        });
                    }
                }).build());
    }
}
