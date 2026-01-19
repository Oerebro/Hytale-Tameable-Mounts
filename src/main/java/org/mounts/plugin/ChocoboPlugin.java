package org.mounts.plugin;

import com.hypixel.hytale.builtin.mounts.NPCMountComponent;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import org.mounts.command.ExampleCommand;
import org.mounts.components.TameableMountComponent;
import org.mounts.event.RegisterPlayer;
import org.mounts.systems.MountInitSystem;
import org.mounts.systems.SaveEntityDataSystem;
import org.mounts.systems.TamingSystem;
import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class ChocoboPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Player player;
    private static World world;
    private static ChocoboPlugin instance;
    private ComponentType<EntityStore, TameableMountComponent> tameableMountComponentComponentType;
    private ComponentType<EntityStore, NPCMountComponent> mountComponentType;
    private ResourceType<EntityStore, MountInitSystem.MountInitQueue> mountInitQueueResourceType;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ChocoboPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup(){
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        //components
        this.tameableMountComponentComponentType = this.getEntityStoreRegistry().registerComponent(TameableMountComponent.class, "Tameable", TameableMountComponent.CODEC);
        //resources
        this.mountInitQueueResourceType = this.getEntityStoreRegistry().registerResource(MountInitSystem.MountInitQueue.class, MountInitSystem.MountInitQueue::new);

        //events
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, RegisterPlayer::onPlayerReady);
        //commands
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));
    }

    //set a player reference and their world if none are set
    public static void registerPlayer(Player player2){
        if(hasNoPlayer()){
            player = player2;
            world = player.getWorld();
        }else{
            LOGGER.atInfo().log("Already has a player registered.");
        }

    }

    @Override
    public void start(){
        //systems
        this.getEntityStoreRegistry().registerSystem(new MountInitSystem(mountInitQueueResourceType));
        this.getEntityStoreRegistry().registerSystem(new MountInitSystem.OnAdd());
        this.getEntityStoreRegistry().registerSystem(new TamingSystem());
    }

    public ResourceType<EntityStore, MountInitSystem.MountInitQueue> getMountInitQueueResourceType() {
        return this.mountInitQueueResourceType;
    }

    private static boolean hasNoPlayer(){
        return (player==null);
    }

    public static World getWorld(){
        return world;
    }

    public static Ref<EntityStore> getPlayerRef(){
        return player.getReference();
    }

    public static ChocoboPlugin getInstance(){
        return instance;
    }

    public static HytaleLogger getHytaleLogger(){
        return LOGGER;
    }

    public ComponentType<EntityStore, TameableMountComponent> getTameableMountComponentComponentType(){
        return this.tameableMountComponentComponentType;
    }

    public static ComponentType<EntityStore,NPCMountComponent> getNPCMountComponentType(){
        return instance.getNPCMountComponent();
    }

    public ComponentType<EntityStore,NPCMountComponent> getNPCMountComponent(){
        return this.mountComponentType;
    }


}