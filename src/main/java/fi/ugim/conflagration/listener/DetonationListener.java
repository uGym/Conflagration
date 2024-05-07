package fi.ugim.conflagration.listener;

import fi.ugim.conflagration.data.BombKeys;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.event.filter.Getter;

public class DetonationListener {

    @Listener
    public void onDetonate(final DetonateExplosiveEvent event, @Getter("explosive") Explosive explosive) {
        explosive.get(BombKeys.BOMB).ifPresent(bomb -> {
            event.setCancelled(true);
            bomb.detonate(explosive.serverLocation().add(0, 0.5, 0));
        });
    }

}
