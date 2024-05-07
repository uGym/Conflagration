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
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.ugim.conflagration.utils.MathUtil.RANDOM;
import static fi.ugim.conflagration.utils.MathUtil.toEulerAngles;

public class Color extends CustomBomb {

    private final Conflagration plugin;
    private final ItemType[] itemTypes = {
            ItemTypes.RED_CONCRETE.get(),
            ItemTypes.ORANGE_CONCRETE.get(),
            ItemTypes.YELLOW_CONCRETE.get(),
            ItemTypes.LIME_CONCRETE.get(),
            ItemTypes.LIGHT_BLUE_CONCRETE.get(),
            ItemTypes.MAGENTA_CONCRETE.get(),
    };

    public Color(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 4;
        this.damage = 0;
        this.radius = 80;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#fbc2eb:#a6c1ee>Sateenkaari TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.A_SHARP2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();

        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 7f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ALLAY_HURT, Sound.Source.MASTER, 7f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_ALLAY_ITEM_GIVEN, Sound.Source.MASTER, 7f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_BLAZE_SHOOT, Sound.Source.MASTER, 7f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GLOW_SQUID_SQUIRT, Sound.Source.MASTER, 7f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_GLOW_SQUID_AMBIENT, Sound.Source.MASTER, 7f, 0f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_AMETHYST_BLOCK_CHIME, Sound.Source.MASTER, 7f, 0f), position);

        for (int i = 0; i < 20; i++) {

            final Vector3d direction = Vector3d.createRandomDirection(RANDOM);
            final Entity entity = serverWorld.createEntity(EntityTypes.ITEM_DISPLAY, position);
            final ItemType itemType = itemTypes[RANDOM.nextInt(itemTypes.length)];
            final Vector3d transformRotation = toEulerAngles(direction);
            entity.offer(Keys.ITEM_STACK_SNAPSHOT, ItemStack.builder().itemType(itemType).build().createSnapshot());
            entity.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, transformRotation));
            entity.offer(Keys.INTERPOLATION_DURATION, Ticks.of(0));
            entity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(0));
            entity.offer(Keys.BLOCK_LIGHT, 1);
            serverWorld.spawnEntity(entity);

            final Set<Vector3i> blockPositions = new HashSet<>();

            AtomicInteger distance = new AtomicInteger(0);
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of(5))
                    .interval(Ticks.of(1))
                    .execute(task -> {
                        if (distance.getAndIncrement() > this.radius / 2) {
                            task.cancel();
                            return;
                        }
                        final Vector3d positiveDirection = position.add(direction.mul(distance.get()));
                        if (!serverWorld.block(positiveDirection.toInt()).get(Keys.IS_SOLID).orElse(false)) {
                            final BlockState state = itemType.block().orElse(BlockTypes.AIR.get()).defaultState();
                            serverWorld.setBlock(positiveDirection.toInt(), state);
                            blockPositions.add(positiveDirection.toInt());
                        }
                        for (Entity victim : serverWorld.nearbyEntities(positiveDirection, 3)) {
                            victim.damage(100, DamageSources.GENERIC);
                        }
                        final Vector3d negativeDirection = position.add(direction.mul(-1 * distance.get()));
                        if (!serverWorld.block(negativeDirection.toInt()).get(Keys.IS_SOLID).orElse(false)) {
                            final BlockState state = itemType.block().orElse(BlockTypes.AIR.get()).defaultState();
                            serverWorld.setBlock(negativeDirection.toInt(), state);
                            blockPositions.add(negativeDirection.toInt());
                        }
                        for (Entity victim : serverWorld.nearbyEntities(negativeDirection, 3)) {
                            victim.damage(100, DamageSources.GENERIC);
                        }
                    }).build());

            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(plugin.container())
                    .delay(Ticks.of((long) (this.radius / 2) + 6))
                    .execute(() -> {
                        for (Vector3i blockPosition : blockPositions) {
                            serverWorld.setBlock(blockPosition, BlockTypes.AIR.get().defaultState());
                            final Entity blockEntity = serverWorld.createEntity(EntityTypes.FALLING_BLOCK, blockPosition.toDouble().add(0.5, 0.5, 0.5));
                            blockEntity.offer(Keys.BLOCK_STATE, itemType.block().orElse(BlockTypes.AIR.get()).defaultState());
                            blockEntity.offer(Keys.CAN_DROP_AS_ITEM, false);
                            serverWorld.spawnEntity(blockEntity);
                        }
                    }).build());

            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.plugin.container())
                    .delay(Ticks.of(2))
                    .execute(() -> {
                        entity.offer(Keys.TRANSFORM, Transform.of(Vector3d.ZERO, transformRotation, Vector3d.UP.mul(this.radius)));
                        entity.offer(Keys.INTERPOLATION_DURATION, Ticks.of((long) this.radius / 2));
                        entity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(0));
                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of((long) this.radius / 2))
                                .execute(() -> entity.remove()).build());
                    }).build());


        }
    }
}
