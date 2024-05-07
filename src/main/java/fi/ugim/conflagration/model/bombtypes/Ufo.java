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
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.horizontalDisk;
import static fi.ugim.conflagration.utils.MathUtil.rotateY;

public class Ufo extends CustomBomb {

    private final Conflagration plugin;

    public Ufo(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 21;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#ee9ca7:#ffdde1>Ufo TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.F_SHARP2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_EVOKER_CAST_SPELL, Sound.Source.PLAYER, 8, 0.5f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_EVOKER_PREPARE_SUMMON, Sound.Source.PLAYER, 8, 0.5f), position);

        final Vector3d radius = Vector3d.UNIT_X;

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.plugin.container())
                    .delay(Ticks.of(5 * finalI))
                    .execute(() -> {
                        for (double theta = 0; theta < Math.PI * 2; theta += Math.PI * 0.05) {

                            final Vector3d radialPosition = rotateY(radius.mul(20 * finalI), theta);
                            final Vector3d impactPosition = serverWorld.highestPositionAt(position.add(radialPosition).toInt()).toDouble().add(0.5, 0.5, 0.5);
                            final Entity ufoLaser = serverWorld.createEntity(EntityTypes.ITEM_DISPLAY, impactPosition);

                            ufoLaser.offer(Keys.ITEM_STACK_SNAPSHOT, ItemStack.builder().itemType(ItemTypes.SOUL_CAMPFIRE).build().createSnapshot());
                            ufoLaser.offer(Keys.BLOCK_LIGHT, 1);
                            ufoLaser.offer(Keys.VIEW_RANGE, 500.0);
                            ufoLaser.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, Vector3d.ZERO, Vector3d.UP.mul(600)));
                            ufoLaser.offer(Keys.INTERPOLATION_DURATION, Ticks.of(0));
                            ufoLaser.offer(Keys.INTERPOLATION_DELAY, Ticks.of(0));
                            serverWorld.spawnEntity(ufoLaser);

                            Sponge.server().scheduler().submit(Task.builder()
                                    .plugin(plugin.container())
                                    .delay(Ticks.of(2))
                                    .execute(() -> {

                                            ufoLaser.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, Vector3d.UP.mul(360), new Vector3d(4, 600, 4)));
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
                                                                serverWorld.setBlock(firePosition, BlockTypes.SOUL_FIRE.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
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
                                for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                    serverPlayer.spawnParticles(ParticleEffect.builder()
                                            .type(ParticleTypes.SONIC_BOOM)
                                            .build(), serverWorld.highestPositionAt(position.add(radialPosition).toInt()).toDouble().add(0.5, 0.5, 0.5));
                                }
                            });

                        }
                    }).build());
        }
    }
}
