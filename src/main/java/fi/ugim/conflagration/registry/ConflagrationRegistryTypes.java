package fi.ugim.conflagration.registry;

import fi.ugim.conflagration.Conflagration;
import fi.ugim.conflagration.model.Bomb;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;

public class ConflagrationRegistryTypes {

    public static final DefaultedRegistryType<Bomb> BOMB = of("bomb");

    private static <T> DefaultedRegistryType<T> of(String id) {
        return RegistryType.of(RegistryRoots.SPONGE, ResourceKey.of(Conflagration.NAMESPACE, id)).asDefaultedType(Sponge::game);
    }

}
