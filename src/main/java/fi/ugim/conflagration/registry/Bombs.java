package fi.ugim.conflagration.registry;

import fi.ugim.conflagration.Conflagration;
import fi.ugim.conflagration.model.Bomb;
import fi.ugim.conflagration.model.bombtypes.*;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryKey;

import java.util.Arrays;
import java.util.List;

public class Bombs {

    public static final DefaultedRegistryReference<Fire> CHICKEN = get("chicken");
    public static final DefaultedRegistryReference<Nuke> GRAVITY = get("gravity");
    public static final DefaultedRegistryReference<Sculk> ICE = get("ice");
    public static final DefaultedRegistryReference<Fire> LIGHTNING = get("lightning");
    public static final DefaultedRegistryReference<Nuke> MAXIMIZE = get("maximize");
    public static final DefaultedRegistryReference<Sculk> MINIMIZE = get("minimize");
    public static final DefaultedRegistryReference<Fire> MULTITNT = get("multitnt");
    public static final DefaultedRegistryReference<Nuke> NAPALM = get("napalm");
    public static final DefaultedRegistryReference<Sculk> NUKE = get("nuke");
    public static final DefaultedRegistryReference<Fire> POOP = get("poop");
    public static final DefaultedRegistryReference<Nuke> PULL = get("pull");
    public static final DefaultedRegistryReference<Sculk> PUSH = get("push");
    public static final DefaultedRegistryReference<Fire> RING = get("ring");
    public static final DefaultedRegistryReference<Nuke> SCULK = get("sculk");
    public static final DefaultedRegistryReference<Nuke> SPIKE = get("spike");
    public static final DefaultedRegistryReference<Nuke> SPIRAL = get("spiral");
    public static final DefaultedRegistryReference<Nuke> ARROW = get("arrow");
    public static final DefaultedRegistryReference<Nuke> SMOKE = get("smoke");
    public static final DefaultedRegistryReference<Nuke> RANDOM_ENTITY = get("random_entity");
    public static final DefaultedRegistryReference<Nuke> TUBU = get("tubu");
    public static final DefaultedRegistryReference<Nuke> CAKE = get("cake");
    public static final DefaultedRegistryReference<Nuke> WANDERING_TRADER = get("wandering_trader");

    public static List<Bomb> all() {
        return ConflagrationRegistryTypes.BOMB.get().stream().toList();
    }

    private static <T extends Bomb> DefaultedRegistryReference<T> get(String id) {
        return RegistryKey.of(ConflagrationRegistryTypes.BOMB, ResourceKey.of(Conflagration.NAMESPACE, id))
                .asDefaultedReference(Sponge::game);
    }

}
