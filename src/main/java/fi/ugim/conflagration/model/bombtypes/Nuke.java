package fi.ugim.conflagration.model.bombtypes;

import fi.ugim.conflagration.Conflagration;
import fi.ugim.conflagration.model.CustomBomb;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
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
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;
import static fi.ugim.conflagration.utils.MathUtil.rotateY;

public class Nuke extends CustomBomb {

    private final Conflagration plugin;

    public Nuke(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 14;
        this.damage = 0;
        this.radius = 200;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#f6d365:#fda085>Ydinpommi");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.D_SHARP2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d center = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_LIGHTNING_BOLT_IMPACT, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WITHER_SHOOT, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_DRAGON_FIREBALL_EXPLODE, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WITHER_BREAK_BLOCK, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_CREEPER_DEATH, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_LIGHTNING_BOLT_THUNDER, Sound.Source.PLAYER, 10, 0), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ENDER_DRAGON_GROWL, Sound.Source.PLAYER, 10, 0f), center);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_WITHER_DEATH, Sound.Source.PLAYER, 10, 0f), center);

        serverWorld.nearbyEntities(center, this.radius).forEach(entity -> {
            entity.damage(6, DamageSource.builder().type(DamageTypes.EXPLOSION).build());
            entity.offer(Keys.VELOCITY, new Vector3d(0, 1, 0).add(entity.position().sub(center).normalize()));
        });

        int layerWidth = 2;
        List<Set<Vector3i>> layers = new ArrayList<>();
        for (int i = 0; i <= radius; i += layerWidth) {
            layers.add(new HashSet<>());
        }
        for (int x = (int) -radius; x <= radius; x++) {
            for (int z = (int) -radius; z <= radius; z++) {
                var position = center.toInt().add(new Vector3i(x, 0, z));
                double distance = position.toDouble().distance(center.toDouble());
                if (distance <= radius) {
                    int layer = (int) Math.floor(distance / layerWidth) * layerWidth;
                    if (layer < layers.size()) {
                        layers.get(layer).add(position);
                    }
                }
            }
        }

        for (int i = 0; i < 400; i++) {

            final Vector3d direction = Vector3d.createRandomDirection(RANDOM).normalize().mul(10 + 20 * Math.random());

            final Entity ringEntity = serverWorld.createEntity(EntityTypes.BLOCK_DISPLAY, center);
            ringEntity.offer(Keys.BLOCK_STATE, BlockTypes.FIRE.get().defaultState());
            ringEntity.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, Vector3d.ZERO, Vector3d.ONE.mul(3)));
            ringEntity.offer(Keys.VIEW_RANGE, 500.0);
            ringEntity.offer(Keys.BLOCK_LIGHT, 1);
            serverWorld.spawnEntity(ringEntity);

            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of(2))
                    .execute(() -> {
                        ringEntity.offer(Keys.TRANSFORM, Transform.of(direction, Vector3d.createRandomDirection(RANDOM).mul(360), Vector3d.ZERO));
                        ringEntity.offer(Keys.INTERPOLATION_DURATION, Ticks.of(20));
                        ringEntity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(1));
                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(20))
                                .execute(() -> ringEntity.remove()).build());
                    }).build());

        }

        for (int i = 0; i < 100; i++) {

            Vector3d direction = Vector3d.createRandomDirection(RANDOM);
            direction = new Vector3d(direction.x(), 0, direction.z()).normalize().mul(120).add(0, (Math.random() - 0.5) * 20, 0);

            final Entity ringEntity = serverWorld.createEntity(EntityTypes.BLOCK_DISPLAY, center);
            ringEntity.offer(Keys.BLOCK_STATE, BlockTypes.FIRE.get().defaultState());
            ringEntity.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, Vector3d.ZERO, Vector3d.ONE.mul(3)));
            ringEntity.offer(Keys.BLOCK_LIGHT, 1);
            serverWorld.spawnEntity(ringEntity);

            Vector3d finalDirection = direction;
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of(2))
                    .execute(() -> {
                        ringEntity.offer(Keys.TRANSFORM, Transform.of(finalDirection, Vector3d.createRandomDirection(RANDOM).mul(360), Vector3d.ZERO));
                        ringEntity.offer(Keys.INTERPOLATION_DURATION, Ticks.of(20));
                        ringEntity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(1));
                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(20))
                                .execute(() -> {
                                    ringEntity.remove();
                                }).build());
                    }).build());

        }

        Set<BlockType> blacklistedBlocks = new HashSet<>();
        blacklistedBlocks.add(BlockTypes.OAK_WOOD.get());
        blacklistedBlocks.add(BlockTypes.OAK_LEAVES.get());
        blacklistedBlocks.add(BlockTypes.OAK_LOG.get());
        blacklistedBlocks.add(BlockTypes.AIR.get());

        AtomicInteger currentLayer = new AtomicInteger(0);
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(this.plugin.container())
                .interval(Ticks.of(1))
                .execute(task -> {
                    int layerIndex = currentLayer.get();
                    if (layerIndex >= layers.size()) {
                        task.cancel();
                        return;
                    }
                    Set<Vector3i> positions = layers.get(layerIndex);
                    for (Vector3i position : positions) {
                        double distance = position.toDouble().distance(center.toDouble());
                        double fractionFromCenter = distance / radius;
                        int depth = (int) (9 - fractionFromCenter * 14);
                        destroyBlocks(serverWorld, position, center, radius, depth, blacklistedBlocks, this.plugin.container());
                    }
                    currentLayer.incrementAndGet();
                }).build());

        var groundZero = center;
        while (!serverWorld.block(groundZero.toInt()).get(Keys.IS_SOLID).orElseThrow()) {
            groundZero = groundZero.sub(0, 0.1, 0);
        }
        groundZero = groundZero.add(0, 3, 0);

        final Vector3d finalGroundZero = groundZero;
        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int i = 0; i < 4000; i++) {
                var yOff = 0.2 * Math.random();
                var direction = rotateY(new Vector3d(2, 0, 0), Math.random() * Math.PI * 2).mul(1 + Math.random() + (24 * Math.abs(yOff - 0.12)) *  (24 * Math.abs(yOff - 0.12)));
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                        .type(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)
                        .velocity(new Vector3d(0, yOff, 0))
                        .build(), finalGroundZero.add(direction).add(0, 0.5, 0)
                    );
                }
            }
            for (int i = 0; i < 8000; i++) {
                var yOff = 12 * (Math.random() - 0.5);
                var direction = rotateY(new Vector3d(1, yOff, 0), Math.random() * Math.PI * 2).mul(Math.random());
                var scale = 0.1 * (1 - yOff * yOff / 64);
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                        .type(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)
                        .velocity(new Vector3d(0, 0.2, 0).add(new Vector3d(direction.x(), 0, direction.z()).mul(scale)))
                        .build(), finalGroundZero.add(direction).sub(0, 1, 0)
                    );
                }
            }
        });

        AtomicInteger c = new AtomicInteger(0);
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(this.plugin.container())
                .interval(Ticks.of(1))
                .execute(task -> {
                    c.getAndIncrement();
                    if (c.get() > 4) {
                        task.cancel();
                        return;
                    }
                    for (int i = 0; i < 500 * c.get(); i++) {
                        var direction = rotateY(new Vector3d(5 * c.get(), 1 * (Math.random() - 0.5), 0), Math.random() * Math.PI * 2).mul(1 * Math.random());
                        var pos = center.add(direction).add(0, 0.5, 0);
                        while (!serverWorld.block(pos.toInt()).get(Keys.IS_SOLID).orElseThrow()) {
                            pos = pos.sub(0, 0.1, 0);
                        }
                        pos = pos.add(0, 0.2, 0);
                        final Vector3d finalPos = pos;
                        final Collection<ServerPlayer> updatedServerPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
                        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
                            for (ServerPlayer serverPlayer : updatedServerPlayers) {
                                serverPlayer.spawnParticles(ParticleEffect.builder()
                                    .type(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)
                                    .velocity(new Vector3d(0, 0.001 * c.get(), 0))
                                    .build(), finalPos
                                );
                            }
                        });
                    }
                }).build());
    }

    private void destroyBlocks(ServerWorld world, Vector3i position, Vector3d center, double radius, int depth, Set<BlockType> blacklistedBlocks, PluginContainer pluginContainer) {
        var highestSolidPosition = world.highestPositionAt(position).sub(0, 1, 0);
        int realDepth = depth;
        var progress = position.distance(center.toInt()) / radius;
        final Vector3d direction = position.toDouble().add(0.5, 0.5, 0.5).sub(center).normalize().add(0, 0.4, 0).mul((1 - progress) * 40 - (10 + 10 * Math.random()));
        for (int y = 0; y < realDepth; y++) {
            BlockState chosenBlock = world.block(highestSolidPosition.sub(0, y, 0));
            BlockType blockType = chosenBlock.type();
            if (blacklistedBlocks.contains(blockType)) {
                realDepth++;
            }
            if (chosenBlock.get(Keys.IS_SOLID).orElse(false)) {
                int finalY = y;
                blockType.item().ifPresent(itemType -> {
                    world.setBlock(highestSolidPosition.sub(0, finalY, 0), BlockState.builder().blockType(BlockTypes.AIR).build());

                    if (Math.random() < 0.1) {
                        Entity block = world.createEntity(EntityTypes.ITEM_DISPLAY, highestSolidPosition);
                        block.offer(Keys.ITEM_STACK_SNAPSHOT, ItemStack.builder().itemType(itemType).build().createSnapshot());

                        world.spawnEntity(block);

                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(2))
                                .execute(() -> {
                                        final Vector3d rot = Vector3d.createRandomDirection(RANDOM).mul(360);
                                        block.offer(Keys.TRANSFORM, Transform.of(direction, rot));
                                        block.offer(Keys.INTERPOLATION_DURATION, Ticks.of(20));
                                        block.offer(Keys.INTERPOLATION_DELAY, Ticks.of(1));

                                        Sponge.server().scheduler().submit(Task.builder()
                                                .plugin(pluginContainer)
                                                .delay(Ticks.of(20))
                                                .execute(() -> {
                                                        block.offer(Keys.TRANSFORM, Transform.of(direction.mul(2), rot, Vector3d.ZERO));
                                                        block.offer(Keys.INTERPOLATION_DURATION, Ticks.of(10));
                                                        block.offer(Keys.INTERPOLATION_DELAY, Ticks.of(1));
                                                        Sponge.server().scheduler().submit(Task.builder()
                                                                .plugin(pluginContainer)
                                                                .delay(Ticks.of(10))
                                                                .execute(() -> {
                                                                        block.remove();
                                                                }).build());
                                                }).build());
                                }).build());
                    }
                });
            }
        }
        if (realDepth > 0) {
            var chance = 0.1 * (1 - progress);
            if (Math.random() < chance) {
                world.setBlock(highestSolidPosition.sub(0, realDepth - 1, 0), BlockState.builder().blockType(BlockTypes.FIRE).build());
            }
        }
        if (realDepth > 0) {
            var chance = 2 * Math.pow((1 - progress), 16);
            if (Math.random() < chance) {
                world.setBlock(highestSolidPosition.sub(0, realDepth, 0), BlockState.builder().blockType(BlockTypes.MAGMA_BLOCK).build());
            }
        }
        if (realDepth > 0) {
            var chance = 2 * Math.pow((1 - progress), 28);
            if (Math.random() < chance) {
                world.setBlock(highestSolidPosition.sub(0, realDepth, 0), BlockState.builder().blockType(BlockTypes.LAVA).build());
            }
        }
    }
}
