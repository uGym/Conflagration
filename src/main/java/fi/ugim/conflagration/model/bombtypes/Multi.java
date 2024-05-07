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
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class Multi extends CustomBomb {

    private final Conflagration plugin;

    public Multi(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 13;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#d299c2:#fef9d7>Monistus TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.D_SHARP1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();

        for (int i = 0; i < 100; i++) {
            final Vector3d velocity = Vector3d.createRandomDirection(RANDOM).add(0, 1, 0).mul(1);
            final Entity tntEntity = serverWorld.createEntity(EntityTypes.TNT, position);
            tntEntity.offer(Keys.VELOCITY, velocity);

            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.plugin.container())
                    .delay(Ticks.of(i))
                    .execute(() -> {
                        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ITEM_PICKUP, Sound.Source.MASTER, 3f, 1f), position);
                        serverWorld.spawnEntity(tntEntity);
                        for (int j = 0; j < 40; j++) {
                            Sponge.server().scheduler().submit(Task.builder()
                                    .plugin(plugin.container())
                                    .delay(Ticks.of(j))
                                    .execute(() -> {
                                        final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                                        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                                            for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                                serverPlayer.spawnParticles(ParticleEffect.builder()
                                                        .type(ParticleTypes.LARGE_SMOKE)
                                                        .build(), tntEntity.position());
                                                serverPlayer.spawnParticles(ParticleEffect.builder()
                                                        .type(ParticleTypes.LAVA)
                                                        .build(), tntEntity.position());
                                            }
                                        });
                                    }).build());
                        }
                    }).build());
        }
    }
}
