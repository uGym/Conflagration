package fi.ugim.conflagration.service;

import fi.ugim.conflagration.model.Bomb;
import lombok.Getter;
import org.spongepowered.api.entity.Entity;

import java.util.Map;
import java.util.WeakHashMap;

@Getter
public class BombService {

    private final Map<Entity, Bomb> bombs = new WeakHashMap<>();

}
