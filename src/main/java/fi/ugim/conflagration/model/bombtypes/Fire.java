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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class Fire extends CustomBomb {

    public Fire() {
        this.customModelData = 7;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#fddb92:#d1fdff>Tuli TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.C1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 5f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_LAVA_POP, Sound.Source.MASTER, 5f, 1.4f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BLASTFURNACE_FIRE_CRACKLE, Sound.Source.MASTER, 5f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_FIRE_AMBIENT, Sound.Source.MASTER, 5f, 0f), position);

        serverWorld.triggerExplosion(Explosion.builder()
                .shouldBreakBlocks(true)
                .knockback(2.0)
                .radius(12)
                .canCauseFire(true)
                .shouldDamageEntities(true)
                .location(serverWorld.location(serverWorld.highestPositionAt(position.toInt())))
                .build());

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int i = 0; i < 500; i++) {
                final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random()).mul(2);
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.FLAME)
                            .velocity(direction)
                            .build(), position);
                }
            }
            for (ServerPlayer serverPlayer : serverPlayers) {
                serverPlayer.spawnParticles(ParticleEffect.builder()
                        .type(ParticleTypes.LAVA)
                        .quantity(200)
                        .offset(Vector3d.ONE.mul(3))
                        .build(), position);
            }
        });

        final BlockState block = BlockState.builder().blockType(BlockTypes.FIRE).build();
        for (int i = 0; i < 600; i++) {
            final Vector3d velocity = Vector3d.createRandomDirection(RANDOM).add(0, 0.5, 0).mul(2 * Math.random() + 0.5);
            final Entity entity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, position);
            entity.offer(Keys.BLOCK_STATE, block);
            entity.offer(Keys.VELOCITY, velocity);
            serverWorld.spawnEntity(entity);
        }
    }
}
