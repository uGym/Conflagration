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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;
import static fi.ugim.conflagration.utils.MathUtil.layeredSphere;

public class Sculk extends CustomBomb {

    private final Conflagration plugin;

    public Sculk(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 17;
        this.damage = 0;
        this.radius = 50;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#d4fc79:#96e6a1>Skulkki TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.F1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
        final List<Set<Vector3i>> blockPositions = layeredSphere(position.toInt(), (int) this.radius).stream().map(shell -> shell.stream().filter(blockPosition -> blockPosition.distance(position.toInt()) * Math.random() < this.radius / 4).collect(Collectors.toSet())).toList();

        for (int i = 0; i < blockPositions.size(); i++) {
            int finalI = i;
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.plugin.container())
                    .delay(Ticks.of(finalI))
                    .execute(() -> {
                        for (Vector3i blockPosition : blockPositions.get(finalI)) {
                            final BlockState blockState = serverWorld.block(blockPosition);
                            if (blockState.get(Keys.IS_REPLACEABLE).orElse(false) && !(blockState.type() == BlockTypes.WATER.get() || blockState.type() == BlockTypes.LAVA.get())) {
                                serverWorld.setBlock(blockPosition, BlockTypes.AIR.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
                            } else if (blockState.get(Keys.IS_SOLID).orElse(false)) {
                                if (Math.random() < 0.999) {
                                    serverWorld.setBlock(blockPosition, BlockTypes.SCULK.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
                                } else {
                                    serverWorld.setBlock(blockPosition, BlockTypes.SCULK_CATALYST.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
                                }
                                if (Math.random() < 0.03 && serverWorld.block(blockPosition.add(0, 1, 0)).type() == BlockTypes.AIR.get()) {
                                    if (Math.random() < 0.1) {
                                        serverWorld.setBlock(blockPosition.add(0, 1, 0), BlockTypes.SCULK_SHRIEKER.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
                                    } else {
                                        serverWorld.setBlock(blockPosition.add(0, 1, 0), BlockTypes.SCULK_SENSOR.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
                                    }
                                }
                            }
                        }
                    }).build());
        }

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int i = 0; i < 500; i++) {
                final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random());
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.SCULK_SOUL)
                            .velocity(direction.mul(2))
                            .build(), position);
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.GLOW_SQUID_INK)
                            .velocity(direction.mul(4))
                            .build(), position);
                }
            }
        });

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 5f, 1f), position);

    }
}
