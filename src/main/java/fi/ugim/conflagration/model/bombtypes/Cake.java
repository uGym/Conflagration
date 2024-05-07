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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class Cake extends CustomBomb {

    private final Conflagration plugin;

    public Cake(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 2;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#ff9a9e:#fad0c4>Kakku TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.A2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 5f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WARDEN_DIG, Sound.Source.MASTER, 5f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_DEACTIVATE, Sound.Source.MASTER, 5f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_RESPAWN_ANCHOR_DEPLETE, Sound.Source.MASTER, 5f, 0f), position);

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int i = 0; i < 500; i++) {
                final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random()).mul(8);
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.POOF)
                            .velocity(direction)
                            .build(), position);
                }
            }
            for (int i = 0; i < 6000; i++) {
                final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random()).mul(5);
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.END_ROD)
                            .velocity(direction)
                            .build(), position);
                }
            }
        });

        Set<Vector3i> cakes = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of(4 * i))
                    .execute(() -> {
                        int dynamicRange = calculateDynamicRange(cakes);
                        spawnCakesAround(position.toInt(), serverWorld, dynamicRange, cakes);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_STONE_PLACE, Sound.Source.MASTER, 5f, 1f), position);
                        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, Sound.Source.MASTER, 5f, 2f), position);
                        final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                            for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                serverPlayer.spawnParticles(ParticleEffect.builder()
                                        .type(ParticleTypes.END_ROD)
                                        .quantity(3000)
                                        .offset(new Vector3d(30, 30, 30))
                                        .build(), position);
                            }
                        });
                    }).build());
        }

    }

    private int calculateDynamicRange(Set<Vector3i> cakes) {
        int base = 10;
        int multiplier = 5;
        return base + (int)(Math.log(cakes.size() + 1) / Math.log(2) * multiplier);
    }

    private void spawnCakesAround(Vector3i position, ServerWorld world, int range, Set<Vector3i> cakes) {
        if (cakes.isEmpty()) {
            cakes.add(position);
            world.setBlock(searchForEmptySpot(position, world, range, cakes), BlockTypes.CAKE.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
        } else {
            Set<Vector3i> newCakes = cakes.stream()
                .map(pos -> searchForEmptySpot(pos, world, range, cakes))
                .filter(Objects::nonNull)
                .peek(newPos -> world.setBlock(newPos, BlockTypes.CAKE.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS))
                .collect(Collectors.toSet());
            cakes.addAll(newCakes);
        }
    }

    private Vector3i searchForEmptySpot(Vector3i position, ServerWorld world, int range, Set<Vector3i> cakes) {
        for (int i = 0; i < 100; i++) {
            double angle = 2 * Math.PI * RANDOM.nextDouble();
            double radius = range * RANDOM.nextDouble();
            Vector3i candidateBase = position.add((int) (radius * Math.cos(angle)), 0, (int) (radius * Math.sin(angle)));
            Vector3i highestPosition = world.highestPositionAt(candidateBase);
            if (!cakes.contains(highestPosition) && world.block(highestPosition).type().equals(BlockTypes.AIR.get())) {
                return highestPosition;
            }
        }
        return null;
    }
}
