package fi.ugim.conflagration.model.bombtypes;

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
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static fi.ugim.conflagration.Conflagration.AUDIOVISUAL_EXECUTOR_SERVICE;
import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class Random extends CustomBomb {

    private final EntityType<?>[] projectiles = {
            EntityTypes.DRAGON_FIREBALL.get(),
            EntityTypes.TRIDENT.get(),
            EntityTypes.FIREBALL.get(),
            EntityTypes.EGG.get(),
            EntityTypes.SNOWBALL.get(),
            EntityTypes.TNT.get(),
            EntityTypes.EXPERIENCE_BOTTLE.get(),
            EntityTypes.LLAMA_SPIT.get(),
            EntityTypes.WITHER_SKULL.get()
    };

    private final ParticleType[] particleTypes = {
            ParticleTypes.FLAME.get(),
            ParticleTypes.SNOWFLAKE.get(),
            ParticleTypes.SOUL_FIRE_FLAME.get(),
            ParticleTypes.SOUL.get(),
            ParticleTypes.SCULK_SOUL.get(),
            ParticleTypes.CLOUD.get(),
            ParticleTypes.POOF.get(),
            ParticleTypes.LARGE_SMOKE.get(),
            ParticleTypes.END_ROD.get(),
            ParticleTypes.SQUID_INK.get(),
            ParticleTypes.GLOW_SQUID_INK.get(),
            ParticleTypes.TOTEM_OF_UNDYING.get()
    };

    public Random() {
        this.customModelData = 16;
        this.damage = 0;
        this.radius = 0;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#c2e9fb:#a1c4fd>Randomi TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.E2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Collection<ServerPlayer> serverPlayers = Sponge.server().onlinePlayers().stream().filter(serverPlayer -> serverPlayer.serverLocation().world().equals(serverWorld)).toList();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 5f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ALLAY_HURT, Sound.Source.MASTER, 5f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_EVOKER_PREPARE_SUMMON, Sound.Source.MASTER, 5f, 2f), position);

        AUDIOVISUAL_EXECUTOR_SERVICE.submit(() -> {
            for (int i = 0; i < 3; i++) {
                final ParticleType particleType = particleTypes[RANDOM.nextInt(particleTypes.length)];
                for (int j = 0; j < 300; j++) {
                    final Vector3d direction = Vector3d.createRandomDirection(RANDOM).mul(Math.random() * 2);
                    for (ServerPlayer serverPlayer : serverPlayers) {
                        serverPlayer.spawnParticles(ParticleEffect.builder()
                                .type(particleType)
                                .velocity(direction)
                                .build(), position);
                    }
                }
            }
        });

        if (Math.random() < 0.4) {
            final EntityType<?> entityType = projectiles[RANDOM.nextInt(projectiles.length)];
            for (int i = 0; i < 400; i++) {
                final Vector3d velocity = Vector3d.createRandomDirection(RANDOM).add(0, 0.8, 0).mul(Math.random() * 2 + 1);
                final Entity entity = serverWorld.createEntity(entityType, position);
                entity.offer(Keys.VELOCITY, velocity);
                serverWorld.spawnEntity(entity);
            }
            return;
        }

        if (Math.random() < 0.5) {
            final List<BlockType> blockTypes = new ArrayList<>(BlockTypes.registry().stream().toList());
            Collections.shuffle(blockTypes);
            final BlockState block = BlockState.builder().blockType(blockTypes.get(0)).build();
            for (int i = 0; i < 400; i++) {
                final Vector3d velocity = Vector3d.createRandomDirection(RANDOM).add(0, 0.8, 0).mul(Math.random() * 2 + 1);
                final Entity entity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, position);
                entity.offer(Keys.BLOCK_STATE, block);
                entity.offer(Keys.VELOCITY, velocity);
                serverWorld.spawnEntity(entity);
            }
            return;
        }

        final List<EntityType<?>> allCreatures = new ArrayList<>(EntityTypes.registry().stream().toList());
        Collections.shuffle(allCreatures);
        final EntityType<?> entityType = allCreatures.get(0);
        for (int i = 0; i < 100; i++) {
            final Vector3d velocity = Vector3d.createRandomDirection(RANDOM).add(0, 0.8, 0).mul(Math.random() * 2 + 1);
            final Entity entity = serverWorld.createEntity(entityType, position);
            entity.offer(Keys.VELOCITY, velocity);
            serverWorld.spawnEntity(entity);
        }

    }
}
