package fi.ugim.conflagration.model.bombtypes;

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
import org.spongepowered.api.entity.explosive.fused.FusedExplosive;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Ticks;
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
import static fi.ugim.conflagration.utils.MathUtil.sphere;

public class Explosion extends CustomBomb {

    public Explosion() {
        this.customModelData = 6;
        this.damage = 0;
        this.radius = 10;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#89f7fe:#66a6ff>Paine TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.B2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
        final Set<Vector3i> blockPositions = sphere(position.toInt(), (int) this.radius).stream().filter(blockPosition -> blockPosition.distance(position.toInt()) * Math.random() < this.radius / 2).collect(Collectors.toSet());
        final List<? extends Entity> nearbyEntities = serverWorld.nearbyEntities(position, this.radius).stream().filter(entity -> !(entity instanceof ServerPlayer || entity instanceof FusedExplosive)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 5f, 0f), position);

        for (Vector3i blockPosition : blockPositions) {

            final Vector3d blockEntityPosition = blockPosition.toDouble().add(0.5, 0, 0.5);
            final Entity blockEntity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, blockEntityPosition);
            final Vector3d velocity = blockEntityPosition.sub(position).normalize().mul(1.5).add(0, 0.6, 0).add(Vector3d.createRandomDirection(RANDOM).mul(0.05));

            blockEntity.offer(Keys.BLOCK_STATE, serverWorld.block(blockPosition));
            blockEntity.offer(Keys.VELOCITY, velocity);
            serverWorld.setBlock(blockPosition, BlockTypes.AIR.get().defaultState());

            serverWorld.spawnEntity(blockEntity);

        }

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int i = 0; i < 500; i++) {
                final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random());
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.POOF)
                            .velocity(direction)
                            .build(), position);
                }
            }
            for (ServerPlayer serverPlayer : serverPlayers) {
                serverPlayer.spawnParticles(ParticleEffect.builder()
                        .type(ParticleTypes.EXPLOSION_EMITTER)
                        .build(), position);
            }
        });

        for (Entity victim : nearbyEntities) {
            final Vector3d velocity = victim.position().sub(position).normalize().mul(1.5);
            victim.offer(Keys.VELOCITY, velocity);
        }
    }
}
