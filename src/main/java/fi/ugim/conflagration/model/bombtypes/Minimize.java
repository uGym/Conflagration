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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Set;
import java.util.stream.Collectors;

import static fi.ugim.conflagration.utils.MathUtil.sphere;

public class Minimize extends CustomBomb {

    private final Conflagration plugin;

    public Minimize(Conflagration plugin) {
        this.plugin = plugin;
        this.customModelData = 12;
        this.damage = 0;
        this.radius = 16;
        this.fuseTime = Ticks.of(80);
        this.displayName = MiniMessage.miniMessage().deserialize("<gradient:#a6ffcb:#12d8fa>Miniatyyri TNT");
        this.blockState = BlockState.builder()
                .blockType(BlockTypes.NOTE_BLOCK)
                .add(Keys.INSTRUMENT_TYPE, InstrumentTypes.BIT.get())
                .add(Keys.NOTE_PITCH, NotePitches.D2.get())
                .build();

        initialize();
    }

    @Override
    public void detonate(ServerLocation serverLocation) {

        final ServerWorld serverWorld = serverLocation.world();
        final Vector3d position = serverLocation.position();
        final Set<Vector3i> blockPositions = sphere(position.toInt(), (int) this.radius).stream().filter(blockPosition -> blockPosition.distance(position.toInt()) < this.radius).collect(Collectors.toSet());

        for (Vector3i blockPosition : blockPositions) {
            boolean hasNonAirBlockAround = false;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        if (x != 0 && y != 0 && z != 0) continue;
                        Vector3i neighborPosition = blockPosition.add(x, y, z);
                        if (!blockPositions.contains(neighborPosition) || serverWorld.block(neighborPosition).type() == BlockTypes.AIR.get()) {
                            hasNonAirBlockAround = true;
                            break;
                        }
                    }
                    if (hasNonAirBlockAround) break;
                }
                if (hasNonAirBlockAround) break;
            }

            if (!hasNonAirBlockAround) {
                continue;
            }
            final Vector3i direction = position.toInt().sub(blockPosition);
            final BlockState originalBlockState = serverWorld.block(blockPosition);
            final Entity blockEntity = serverWorld.createEntity(EntityTypes.BLOCK_DISPLAY, blockPosition);
            blockEntity.offer(Keys.BLOCK_STATE, originalBlockState);
            blockEntity.offer(Keys.VIEW_RANGE, 500.0);
            serverWorld.spawnEntity(blockEntity);
            Sponge.server().scheduler().submit(Task.builder()
                    .plugin(this.plugin.container())
                    .delay(Ticks.of(2))
                    .execute(() -> {
                        blockEntity.offer(Keys.TRANSFORM, Transform.of(direction.toDouble().mul(0.9), Vector3d.ZERO, Vector3d.ONE.mul(0.1)));
                        blockEntity.offer(Keys.INTERPOLATION_DURATION, Ticks.of(40));
                        blockEntity.offer(Keys.INTERPOLATION_DELAY, Ticks.of(1));
                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(plugin.container())
                                .delay(Ticks.of(2000))
                                .execute(() -> {
                                    blockEntity.remove();
                                }).build());
                    }).build());
        }

        for (Vector3i blockPosition : blockPositions) {
            serverWorld.setBlock(blockPosition, BlockTypes.AIR.get().defaultState());
        }

        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_DEACTIVATE, Sound.Source.MASTER, 8f, 1f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_DEACTIVATE, Sound.Source.MASTER, 8f, 0.8f), position);
        serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_BEACON_DEACTIVATE, Sound.Source.MASTER, 8f, 1.1f), position);
    }
}
