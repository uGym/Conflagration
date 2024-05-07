package fi.ugim.conflagration.model;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;

public interface Bomb {

    int customModelData();

    double damage();
    double radius();

    Ticks fuseTime();
    String name();
    Component displayName();
    BlockState blockState();
    ItemStack itemStack();

    void detonate(ServerLocation serverLocation);

}
