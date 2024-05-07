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
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.*;
import java.util.stream.Collectors;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;
import static fi.ugim.conflagration.utils.MathUtil.sphere;

public class Ice extends CustomBomb {

    private final Conflagration plugin;
    private final Map<Entity, Entity> frozen = new WeakHashMap<>();

    public Ice(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 9;
        this.damage = 0;
        this.radius = 14;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#f093fb:#f5576c>Huurre TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.C_SHARP1.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
        final Set<Vector3i> blockPositions = sphere(position.toInt(), (int) this.radius).stream().filter(blockPosition -> blockPosition.distance(position.toInt()) * Math.random() < this.radius / 2).collect(Collectors.toSet());
        final List<? extends Entity> nearbyEntities = serverWorld.nearbyEntities(position, this.radius).stream().filter(entity -> !(entity instanceof ServerPlayer)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.BLOCK, 5f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_GLASS_BREAK, Sound.Source.BLOCK, 5f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_GLASS_BREAK, Sound.Source.BLOCK, 5f, 0f), position);

        for (Vector3i blockPosition : blockPositions) {
            final BlockState blockState = serverWorld.block(blockPosition);
            if (blockState.get(Keys.IS_REPLACEABLE).orElse(false) && !(blockState.type() == BlockTypes.WATER.get() || blockState.type() == BlockTypes.LAVA.get())) {
                serverWorld.setBlock(blockPosition, BlockTypes.AIR.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
            } else if (blockState.get(Keys.IS_SOLID).orElse(false) || blockState.type() == BlockTypes.WATER.get() || blockState.type() == BlockTypes.LAVA.get()) {
                serverWorld.setBlock(blockPosition, BlockTypes.ICE.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);
            }
        }

        for (Entity victim : nearbyEntities) {
            if (frozen.containsKey(victim)) {
                endFrozen(victim);
                return;
            }
            ServerWorld world = victim.serverLocation().world();
            victim.setLocationAndRotation(victim.serverLocation(), victim.rotation());
            victim.offer(Keys.VELOCITY, new Vector3d(0, 0, 0));
            victim.offer(Keys.WALKING_SPEED, (double) 0);
            victim.offer(Keys.IS_AI_ENABLED, false);
            if (victim.boundingBox().isEmpty()) {
                return;
            }
            var hitbox = victim.boundingBox().get();
            var center = hitbox.center();
            this.frozen.put(victim, world.createEntity(EntityTypes.ITEM_DISPLAY, center));
            final Entity ice = this.frozen.get(victim);
            ice.offer(Keys.ITEM_STACK_SNAPSHOT, ItemStack.of(ItemTypes.ICE).createSnapshot());
            ice.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, new Vector3d(0, victim.rotation().y(), 0), hitbox.size().mul(2)));
            world.spawnEntity(ice);
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.plugin.container())
                    .delay(Ticks.of((long) (100 * Math.random() + 120)))
                    .execute(() -> {
                            if (frozen.containsKey(victim)) {
                                endFrozen(victim);
                            }
                    }).build());
        }

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int i = 0; i < 500; i++) {
                final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random());
                for (ServerPlayer serverPlayer : serverPlayers) {
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.SNOWFLAKE)
                            .velocity(direction)
                            .build(), position);
                    serverPlayer.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.SOUL_FIRE_FLAME)
                            .velocity(direction)
                            .build(), position);
                }
            }
        });

    }

    private void endFrozen(Entity entity) {
        final ServerWorld serverWorld = entity.serverLocation().world();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();
        entity.boundingBox().ifPresent(aabb -> AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (ServerPlayer serverPlayer : serverPlayers) {
                serverPlayer.spawnParticles(ParticleEffect.builder()
                        .type(ParticleTypes.BLOCK)
                        .option(ParticleOptions.BLOCK_STATE, BlockTypes.ICE.get().defaultState())
                        .offset(aabb.size().div(2))
                        .quantity((int) (aabb.size().length() * 30))
                        .build(), aabb.center());
            }
        }));
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_GLASS_BREAK, Sound.Source.BLOCK, 5f, 1f), entity.position());
        entity.offer(Keys.WALKING_SPEED, 0.1);
        entity.offer(Keys.IS_AI_ENABLED, true);
        entity.damage(1000.0, DamageSources.GENERIC);
        this.frozen.remove(entity).remove();
    }
}
