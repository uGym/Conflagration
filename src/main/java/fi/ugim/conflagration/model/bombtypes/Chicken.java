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
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class Chicken extends CustomBomb {

    public Chicken() {
        this.customModelData = 3;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#a1c4fd:#c2e9fb>Kana TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.A_SHARP1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 5f, 1f), position);

        for (int i = 0; i < 300; i++) {

            final Entity entity = serverWorld.createEntity(EntityTypes.CHICKEN, position);
            final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(2);
            entity.offer(Keys.VELOCITY, direction);
            serverWorld.spawnEntity(entity);

            AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.LARGE_SMOKE)
                            .velocity(direction)
                            .build(), position);
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.POOF)
                            .velocity(direction)
                            .build(), position);
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.FLAME)
                            .velocity(direction.mul(0.5))
                            .build(), position);
                }
            });

        }
    }
}
