package fi.ugim.conflagration.commands;

import fi.ugim.conflagration.data.BombKeys;
import fi.ugim.conflagration.model.Bomb;
import fi.ugim.conflagration.registry.Bombs;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.fused.PrimedTNT;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public class BombCommand {

    public static Command.Parameterized root() {
        return Command.builder()
                .addChild(spawn(), "spawn")
                .addChild(item(), "item")
                .build();
    }

    private static Command.Parameterized spawn() {

        final Parameter.Value<Bomb> parameterBomb = bombParameters();

        return Command.builder()
                .permission("conflagration.admin")
                .addParameter(parameterBomb)
                .executor(context -> {

                    final ServerPlayer serverPlayer = context.cause().last(ServerPlayer.class).orElseThrow();
                    final Bomb bomb = context.one(parameterBomb).orElseThrow();

                    final ServerWorld serverWorld = serverPlayer.world();
                    final Vector3d position = serverPlayer.position();
                    final PrimedTNT entity = serverWorld.createEntity(EntityTypes.TNT, position);

                    entity.offer(Keys.BLOCK_STATE, bomb.blockState());
                    entity.offer(Keys.TICKS_REMAINING, bomb.fuseTime());
                    entity.offer(Keys.VELOCITY, Vector3d.UP.add(Vector3d.createRandomDirection(new Random()).add(Vector3d.UP)).mul(0.05));
                    entity.offer(BombKeys.BOMB, bomb);
                    entity.offer(BombKeys.EXPLOSION_RADIUS, bomb.radius());
                    entity.offer(BombKeys.DAMAGE, bomb.damage());

                    serverWorld.spawnEntity(entity);

                    return CommandResult.success();
                })
                .build();

    }

    private static Command.Parameterized item() {

        final Parameter.Value<Bomb> parameterBomb = bombParameters();

        return Command.builder()
                .permission("conflagration.admin")
                .addParameter(parameterBomb)
                .executor(context -> {

                    final ServerPlayer serverPlayer = context.cause().last(ServerPlayer.class).orElseThrow();
                    final Bomb bomb = context.one(parameterBomb).orElseThrow();

                    serverPlayer.inventory().offer(bomb.itemStack());

                    return CommandResult.success();
                })
                .build();

    }

    public static Parameter.Value<Bomb> bombParameters() {
        return Parameter.builder(Bomb.class)
                .addParser((parameterKey, reader, context) -> {
                    final String str = reader.parseString();
                    return Bombs.all().stream()
                            .filter(bomb -> bomb.name().equals(str))
                            .findFirst();
                })
                .completer((context, currentInput) -> Bombs.all().stream()
                            .filter(bomb -> bomb.name().toLowerCase(Locale.ROOT).startsWith(currentInput.toLowerCase(Locale.ROOT)))
                            .map(ore -> CommandCompletion.of(ore.name()))
                            .collect(Collectors.toList()))
                .key("bomb")
                .optional()
                .build();
    }

}
