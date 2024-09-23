package org.example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import org.example.commands.*;

public class Main {
    public static void main(String[] args) {



        // Initialize the server
        MinecraftServer server = MinecraftServer.init();

        //Create an instance(like a world)
        InstanceManager instance = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instance.createInstanceContainer();


        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0,40, Block.GRASS_BLOCK));

        instanceContainer.setChunkLoader(new AnvilLoader("worlds/world"));

        instanceContainer.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        EventNode<PlayerEvent> playerNode = EventNode.type("connection", EventFilter.PLAYER);
        playerNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
            player.setGameMode(GameMode.SURVIVAL);
            player.setPermissionLevel(4);
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10);

        });
        playerNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            player.sendMessage(Component.text("Welcome to the server!", NamedTextColor.GREEN));
            player.damage(DamageType.ARROW, 1f);
        });

        globalEventHandler.addChild(playerNode);
        globalEventHandler.addListener(EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                player.sendMessage(Component.text("You attacked " + event.getTarget().getEntityId()));
            }
            if (event.getTarget() instanceof LivingEntity target && event.getEntity() instanceof LivingEntity entity) {
                target.damage(new Damage(DamageType.GENERIC,event.getEntity(),target,event.getEntity().getPosition(),
                        (float) (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue())));
            }
        });

        MinecraftServer.getSchedulerManager().buildShutdownTask(Main::stopServer);



        registerCommands(new StopCommand(),
                new MenuCommand(),
                new WeaponCommand(),
                new VaultCommand(),
                new GamemodeCommand(),
                new SpawnCommand());


        MojangAuth.init();

        server.start("0.0.0.0", 25565);
    }

    private static void registerCommands(Command... commands) {
        CommandManager commandManager = MinecraftServer.getCommandManager();
        for (Command command : commands) {
            commandManager.register(command);
        }
    }


    public static void stopServer() {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick("Server stopped"));
        MinecraftServer.getInstanceManager().getInstances().forEach(Instance::saveChunksToStorage);
        System.out.println("Server stopped");
        MinecraftServer.stopCleanly();
        Runtime.getRuntime().exit(0);
    }
}