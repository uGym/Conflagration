package fi.ugim.conflagration.data;

import fi.ugim.conflagration.Conflagration;
import fi.ugim.conflagration.model.Bomb;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

public class BombKeys {

    public static final Key<Value<Bomb>> BOMB = Key.from(ResourceKey.of(Conflagration.NAMESPACE, "bomb"), Bomb.class);
    public static final Key<Value<Double>> EXPLOSION_RADIUS = Key.from(ResourceKey.of(Conflagration.NAMESPACE, "explosion_radius"), Double.class);
    public static final Key<Value<Double>> DAMAGE = Key.from(ResourceKey.of(Conflagration.NAMESPACE, "damage"), Double.class);

}
