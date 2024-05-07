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
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class WanderingTrader extends CustomBomb {

    private final Conflagration plugin;

    public WanderingTrader(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 22;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#b6fbff:#83a4d4>Valdemar TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.G0.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 8f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WANDERING_TRADER_YES, Sound.Source.MASTER, 8f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WANDERING_TRADER_NO, Sound.Source.MASTER, 8f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WANDERING_TRADER_TRADE, Sound.Source.MASTER, 8f, 0f), position);

        for (int i = 0; i < 150; i++) {
            final Vector3d velocity = Vector3d.createRandomDirection(RANDOM).mul(3);
            final Entity trader = serverWorld.createEntity(EntityTypes.WANDERING_TRADER, position);
            trader.offer(Keys.VELOCITY, velocity);
            final AtomicInteger c = new AtomicInteger(0);
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .interval(Ticks.of(1))
                    .execute(task -> {
                        if (c.getAndIncrement() >= 200) {
                            task.cancel();
                            return;
                        }
                        if (trader.get(Keys.LEASH_HOLDER).isEmpty()) {
                            final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                            updatedServerPlayers.stream()
                                .filter(serverPlayer -> serverPlayer.position().distance(trader.position()) < 8)
                                .findFirst()
                                .ifPresent(serverPlayer -> trader.offer(Keys.LEASH_HOLDER, serverPlayer));
                        }
                    }).build());
            serverWorld.spawnEntity(trader);
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of(200))
                    .execute(() -> {
                        if (!trader.isRemoved()) {
                            final Vector3d traderPos = trader.position();
                            trader.remove();
                            serverWorld.triggerExplosion(Explosion.builder()
                                    .shouldBreakBlocks(true)
                                    .knockback(2.0)
                                    .radius(2)
                                    .shouldPlaySmoke(false)
                                    .shouldDamageEntities(true)
                                    .location(serverWorld.location(traderPos.toInt()))
                                    .build());
                            final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                            AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                                for (int j = 0; j < 30; j++) {
                                    final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random());
                                    for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                        serverPlayer.spawnParticles(ParticleEffect.builder()
                                                .type(ParticleTypes.POOF)
                                                .velocity(direction)
                                                .build(), traderPos);
                                    }
                                }
                                for (int j = 0; j < 30; j++) {
                                    final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random());
                                    for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                        serverPlayer.spawnParticles(ParticleEffect.builder()
                                                .type(ParticleTypes.DRAGON_BREATH)
                                                .velocity(direction)
                                                .build(), traderPos);
                                    }
                                }
                            });
                        }
                    }).build());
        }


    }
}
