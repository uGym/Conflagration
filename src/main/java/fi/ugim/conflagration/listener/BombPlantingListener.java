package fi.ugim.conflagration.listener;

import fi.ugim.conflagration.model.Bomb;
import fi.ugim.conflagration.registry.Bombs;
import net.kyori.adventure.sound.Sound;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class BombPlantingListener {

    @IsCancelled(Tristate.FALSE)
    @Listener(order = Order.POST)
    public void onInteract(InteractBlockEvent.Secondary event, @Last ServerPlayer serverPlayer) {

        final HandType hand = event.context().get(EventContextKeys.USED_HAND).orElse(HandTypes.OFF_HAND.get());
        final ItemStack itemStack = serverPlayer.itemInHand(hand);

        if (hand.equals(HandTypes.OFF_HAND.get())) {
            return;
        }

        itemStack.get(Keys.CUSTOM_MODEL_DATA).ifPresent(customModelData -> {

            final Optional<Bomb> optionalBomb = Bombs.all().stream()
                .filter(registryReference -> registryReference.customModelData() == customModelData)
                .findFirst();

            optionalBomb.ifPresent(bomb -> {
                event.setCancelled(true);
                final BlockSnapshot block = event.block();
                final Direction direction = event.targetSide();
                if (direction == Direction.NONE) {
                    return;
                }

                Vector3i position;
                if (block.get(Keys.IS_REPLACEABLE).orElse(false)) {
                    position = block.position();
                } else {
                    position = block.position().add(direction.asBlockOffset());
                }
                final ServerWorld serverWorld = serverPlayer.world();
                if (serverPlayer.gameMode().get() != GameModes.CREATIVE.get()) {
                    serverPlayer.setItemInHand(hand, itemStack.quantity() == 1 ? ItemStack.empty() : decreaseByOne(itemStack));
                }

                event.setUseItemResult(Tristate.TRUE);
                serverWorld.playSound(Sound.sound(SoundTypes.BLOCK_STONE_PLACE, Sound.Source.BLOCK, 1f, 1f), position.toDouble().add(0.5, 0.5, 0.5));
                final ClientboundAnimatePacket swingPacket = new ClientboundAnimatePacket((Entity) serverPlayer, 0);
                ((net.minecraft.server.level.ServerPlayer) serverPlayer).connection.send(swingPacket);

                serverWorld.setBlock(position, bomb.blockState(), BlockChangeFlags.NONE
                    .withNotifyClients(true)
                    .withNotifyObservers(true)
                    .withLightingUpdates(true)
                );
            });
        });
    }

    private ItemStack decreaseByOne(ItemStack stack) {
        stack.setQuantity(stack.quantity() - 1);
        return stack;
    }

}
