package fi.ugim.conflagration.listener;

import fi.ugim.conflagration.data.BombKeys;
import fi.ugim.conflagration.model.Bomb;
import fi.ugim.conflagration.registry.Bombs;
import net.kyori.adventure.sound.Sound;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.fused.PrimedTNT;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

import static fi.ugim.conflagration.utils.MathUtil.RANDOM;

public class FuseListener {

    @IsCancelled(Tristate.FALSE)
    @Listener(order = Order.LAST)
    public void onInteract(InteractBlockEvent.Secondary event, @Last ServerPlayer serverPlayer) {

        if (event.context().get(EventContextKeys.USED_HAND).orElse(HandTypes.OFF_HAND.get()).equals(HandTypes.OFF_HAND.get())) {
            return;
        }

        final BlockSnapshot blockSnapshot = event.block();
        final BlockState blockState = blockSnapshot.state();
        final Optional<Bomb> optionalBomb = Bombs.all().stream()
            .filter(bomb -> bomb.blockState().equals(blockState))
            .findFirst();

        optionalBomb.ifPresent(bomb -> {

            if (serverPlayer.equipped(EquipmentTypes.MAIN_HAND).orElse(ItemStack.empty()).type() != ItemTypes.FLINT_AND_STEEL.get()) {
                if (!serverPlayer.get(Keys.IS_SNEAKING).orElse(false)) {
                    event.setCancelled(true);
                }
                return;
            }

            event.setCancelled(true);

            final Ticks fuseTime = bomb.fuseTime();
            final ServerWorld serverWorld = serverPlayer.world();
            final Vector3d position = blockSnapshot.position().toDouble().add(0.5, 0, 0.5);
            final PrimedTNT entity = serverWorld.createEntity(EntityTypes.TNT, position);

            Vector3d velocity = Vector3d.createRandomDirection(RANDOM);
            velocity = new Vector3d(velocity.x(), 0, velocity.z()).mul(0.05);
            velocity = velocity.add(Vector3d.UP.mul(0.2));

            serverWorld.setBlock(blockSnapshot.position(), BlockTypes.AIR.get().defaultState(), BlockChangeFlags.NOTIFY_CLIENTS);

            entity.offer(Keys.BLOCK_STATE, bomb.blockState());
            entity.offer(Keys.TICKS_REMAINING, fuseTime);
            entity.offer(Keys.VELOCITY, velocity);
            entity.offer(BombKeys.BOMB, bomb);

            serverWorld.playSound(Sound.sound(SoundTypes.ITEM_FLINTANDSTEEL_USE, Sound.Source.BLOCK, 1f, 1f), position.toDouble().add(0.5, 0.5, 0.5));
            serverWorld.playSound(Sound.sound(SoundTypes.ENTITY_TNT_PRIMED, Sound.Source.BLOCK, 1f, 1f), position.toDouble().add(0.5, 0.5, 0.5));

            event.setUseItemResult(Tristate.TRUE);

            serverWorld.spawnEntity(entity);
        });
    }

}
