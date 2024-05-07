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
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.weather.LightningBolt;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.*;

public class Electricity extends CustomBomb {

    private final Conflagration plugin;

    public Electricity(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 5;
        this.damage = 0;
        this.radius = 40;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#ffecd2:#fcb69f>Voltti TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.B1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
        final List<? extends Entity> nearbyEntities = serverWorld.nearbyEntities(position, this.radius).stream().filter(entity -> !(entity instanceof ServerPlayer) && entity instanceof Living).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 7f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_BEE_POLLINATE, Sound.Source.MASTER, 7f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GUARDIAN_ATTACK, Sound.Source.MASTER, 7f, 0.6f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GUARDIAN_ATTACK, Sound.Source.MASTER, 7f, 1.5f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GUARDIAN_DEATH, Sound.Source.MASTER, 7f, 0.6f), position);

        serverWorld.triggerExplosion(Explosion.builder()
                .shouldBreakBlocks(true)
                .knockback(2.0)
                .radius(48)
                .canCauseFire(false)
                .shouldDamageEntities(true)
                .location(serverWorld.location(serverWorld.highestPositionAt(position.toInt())))
                .build());

        for (Entity entity : nearbyEntities) {
            final LightningBolt lightningBolt = serverWorld.createEntity(EntityTypes.LIGHTNING_BOLT, entity.position());
            serverWorld.spawnEntity(lightningBolt);
        }

        for (int i = 0; i < 20; i++) {

            AtomicReference<Vector3d> particlePosition = new AtomicReference<>(position);
            Vector3d direction = Vector3d.createRandomDirection(RANDOM);

            for (int segment = 0; segment < 10; segment++) {

                final Vector3d vertical = rotateVector(relativeVertical(direction), direction, Math.random() * Math.PI * 2);
                direction = rotateTowards(direction, vertical, Math.random() * Math.PI * 0.5);
                Vector3d finalDirection = direction;

                Sponge.server().scheduler().submit(Task.builder()
                        .plugin(this.plugin.container())
                        .delay(Ticks.of(segment * 2))
                        .execute(() -> {
                                for (int step = 0; step < 30; step++) {
                                    particlePosition.set(particlePosition.get().add(finalDirection.mul(0.1)));
                                    for (ServerPlayer serverPlayer : serverPlayers) {
                                        serverPlayer.spawnParticles(ParticleEffect.builder()
                                                .type(ParticleTypes.END_ROD)
                                                .build(), particlePosition.get());
                                    }
                                }
                        }).build());



            }
        }
    }



}
