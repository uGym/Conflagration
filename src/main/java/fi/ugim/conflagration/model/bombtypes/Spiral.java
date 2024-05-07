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
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.*;

public class Spiral extends CustomBomb {

    private final Conflagration plugin;

    public Spiral(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 19;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#ff9a9e:#fecfef>Orbitaali TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.F_SHARP0.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_RESPAWN_ANCHOR_DEPLETE, Sound.Source.PLAYER, 12, 0f), position);
        final Vector3d radius = Vector3d.UNIT_X;

        int c = 0;
        for (double theta = 0; theta < Math.PI * 16; theta += Math.PI * 0.05) {

            double finalTheta = theta;
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of(c++ / 3))
                    .execute(() -> {

                        final Vector3d radialPosition = rotateY(radius.mul(2 * finalTheta), finalTheta);
                        final Vector3d impactPosition = serverWorld.highestPositionAt(position.add(radialPosition).toInt()).toDouble().add(0.5, 0.5, 0.5);
                        final Entity ufoLaser = serverWorld.createEntity(EntityTypes.ITEM_DISPLAY, impactPosition);

                        ufoLaser.offer(Keys.ITEM_STACK_SNAPSHOT, ItemStack.builder().itemType(ItemTypes.CAMPFIRE).build().createSnapshot());
                        ufoLaser.offer(Keys.BLOCK_LIGHT, 1);
                        ufoLaser.offer(Keys.VIEW_RANGE, 500.0);
                        ufoLaser.offer(Keys.TRANSFORM, Transform.of(Vector3d.UP.mul(-100), Vector3d.ZERO, Vector3d.UP.mul(1000)));
                        ufoLaser.offer(Keys.INTERPOLATION_DURATION, Ticks.of(0));
                        ufoLaser.offer(Keys.INTERPOLATION_DELAY, Ticks.of(0));
                        serverWorld.spawnEntity(ufoLaser);

                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(2))
                                .execute(() -> {
                                        ufoLaser.offer(Keys.TRANSFORM, Transform.of(Vector3d.UP.mul(-100), Vector3d.UP.mul(Math.random() * 360), new Vector3d(4, 1000, 4)));
                                        ufoLaser.offer(Keys.INTERPOLATION_DURATION, Ticks.of(30));
                                        ufoLaser.offer(Keys.INTERPOLATION_DELAY, Ticks.of(0));
                                        Sponge.server().scheduler().submit(Task.builder()
                                                .plugin(plugin.container())
                                                .delay(Ticks.of(30))
                                                .execute(() -> {
                                                    serverWorld.triggerExplosion(Explosion.builder()
                                                            .shouldBreakBlocks(true)
                                                            .knockback(2.0)
                                                            .radius(6)
                                                            .canCauseFire(false)
                                                            .shouldDamageEntities(true)
                                                            .location(serverWorld.location(serverWorld.highestPositionAt(position.add(radialPosition).toInt())))
                                                            .build());
                                                    for (Vector3i diskPosition : horizontalDisk(position.add(radialPosition).toInt(), 6)) {
                                                        if (Math.random() < 0.2) {
                                                            final Vector3i firePosition = serverWorld.highestPositionAt(diskPosition);
                                                            if (Math.random() < 0.05) {
                                                                serverWorld.setBlock(firePosition, BlockTypes.LAVA.get().defaultState());
                                                            } else {
                                                                serverWorld.setBlock(firePosition, BlockTypes.FIRE.get().defaultState());
                                                            }
                                                        }
                                                    }
                                                    ufoLaser.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, Vector3d.ZERO, Vector3d.UP.mul(600)));
                                                    ufoLaser.offer(Keys.INTERPOLATION_DURATION, Ticks.of(3));
                                                    ufoLaser.offer(Keys.INTERPOLATION_DELAY, Ticks.of(0));

                                                    Sponge.server().scheduler().submit(Task.builder()
                                                            .plugin(plugin.container())
                                                            .delay(Ticks.of(3))
                                                            .execute(() -> ufoLaser.remove()).build());
                                                }).build());
                                }).build());
                        final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                            for (ServerPlayer updatedServerPlayer : updatedServerPlayers) {
                                updatedServerPlayer.spawnParticles(ParticleEffect.builder()
                                        .type(ParticleTypes.LAVA)
                                        .quantity(20)
                                        .build(), serverWorld.highestPositionAt(position.add(radialPosition).toInt()).toDouble().add(0.5, 0.5, 0.5));
                            }
                        });
                    }).build());

        }
    }
}
