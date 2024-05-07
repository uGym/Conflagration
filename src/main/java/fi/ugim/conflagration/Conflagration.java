package fi.ugim.conflagration;

import com.google.inject.Inject;
import fi.ugim.conflagration.commands.BombCommand;
import fi.ugim.conflagration.data.BombKeys;
import fi.ugim.conflagration.listener.BombPlantingListener;
import fi.ugim.conflagration.listener.DetonationListener;
import fi.ugim.conflagration.listener.FuseListener;
import fi.ugim.conflagration.listener.NoteBlockListener;
import fi.ugim.conflagration.model.Bomb;
import fi.ugim.conflagration.model.bombtypes.*;
import fi.ugim.conflagration.registry.ConflagrationRegistryTypes;
import fi.ugim.conflagration.service.BombService;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Plugin("Conflagration")
public class Conflagration {

    public static final String NAMESPACE = "conflagration";
    public static final ScheduledExecutorService AUDIOVISUAL_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(4);

    @Getter
    private final PluginContainer container;

    private final Logger logger;

    @Getter
    private final BombService bombService;

    @Inject
    Conflagration(final PluginContainer container, final Logger logger) {
        this.container = container;
        this.logger = logger;
        this.bombService = new BombService();
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        this.logger.info("Constructing Conflagration");
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        Sponge.eventManager().registerListeners(this.container, new NoteBlockListener());
        Sponge.eventManager().registerListeners(this.container, new BombPlantingListener());
        Sponge.eventManager().registerListeners(this.container, new DetonationListener());
        Sponge.eventManager().registerListeners(this.container, new FuseListener());
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(container, BombCommand.root(), "bomb");
    }

    @Listener
    public void onRegisterData(final RegisterDataEvent event) {

        event.register(DataRegistration.builder()
                .dataKey(BombKeys.BOMB)
                .provider(DataProvider.mutableBuilder()
                        .key(BombKeys.BOMB)
                        .dataHolder(Entity.class)
                        .get(entity -> this.bombService.bombs().get(entity))
                        .set((entity, bomb) -> this.bombService.bombs().put(entity, bomb))
                        .build())
                .build());
    }

    @Listener
    public void onRegisterRegistry(final RegisterRegistryEvent.GameScoped event) {
        event.register(ResourceKey.of(NAMESPACE, "bomb"), true);
    }

    @Listener
    public void onRegisterValue(final RegisterRegistryValueEvent.GameScoped event) {
        final RegisterRegistryValueEvent.RegistryStep<Bomb> bombs = event.registry(ConflagrationRegistryTypes.BOMB);
        bombs.register(ResourceKey.of(NAMESPACE, "chicken"), new Chicken());
        bombs.register(ResourceKey.of(NAMESPACE, "gravity"), new Gravity(this));
        bombs.register(ResourceKey.of(NAMESPACE, "ice"), new Ice(this));
        bombs.register(ResourceKey.of(NAMESPACE, "lightning"), new Electricity(this));
        bombs.register(ResourceKey.of(NAMESPACE, "maximize"), new Magnify(this));
        bombs.register(ResourceKey.of(NAMESPACE, "minimize"), new Minimize(this));
        bombs.register(ResourceKey.of(NAMESPACE, "multitnt"), new Multi(this));
        bombs.register(ResourceKey.of(NAMESPACE, "napalm"), new Fire());
        bombs.register(ResourceKey.of(NAMESPACE, "nuke"), new Nuke(this));
        bombs.register(ResourceKey.of(NAMESPACE, "poop"), new Poop(this));
        bombs.register(ResourceKey.of(NAMESPACE, "pull"), new Implosion());
        bombs.register(ResourceKey.of(NAMESPACE, "push"), new Explosion());
        bombs.register(ResourceKey.of(NAMESPACE, "ring"), new Ufo(this));
        bombs.register(ResourceKey.of(NAMESPACE, "sculk"), new Sculk(this));
        bombs.register(ResourceKey.of(NAMESPACE, "spike"), new Color(this));
        bombs.register(ResourceKey.of(NAMESPACE, "spiral"), new Spiral(this));
        bombs.register(ResourceKey.of(NAMESPACE, "arrow"), new Arrow());
        bombs.register(ResourceKey.of(NAMESPACE, "smoke"), new Smoke(this));
        bombs.register(ResourceKey.of(NAMESPACE, "random_entity"), new Random());
        bombs.register(ResourceKey.of(NAMESPACE, "tubu"), new Tubu(this));
        bombs.register(ResourceKey.of(NAMESPACE, "wandering_trader"), new WanderingTrader(this));
        bombs.register(ResourceKey.of(NAMESPACE, "cake"), new Cake(this));
    }
}
