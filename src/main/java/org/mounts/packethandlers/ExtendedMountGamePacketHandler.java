package org.mounts.packethandlers;

import com.hypixel.hytale.builtin.mounts.MountGamePacketHandler;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.DismountNPC;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.gamemode.GameModeType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtendedMountGamePacketHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();


    public static void registerPacketCounters() {
        PacketAdapters.registerInbound((PacketHandler packetHandler, Packet packet) -> {
            if(packet instanceof DismountNPC){
                System.out.println("Dismount");
                PlayerRef playerRef = ((GamePacketHandler)packetHandler).getPlayerRef();
                Ref<EntityStore> ref = playerRef.getReference();

                if (ref != null && ref.isValid()) {
                    Store<EntityStore> store = ref.getStore();
                    EntityStore entityStore = store.getExternalData();
                    World world = entityStore.getWorld();
                    world.execute(() -> {
                        GameModeType gameModeType = GameModeType.fromGameMode(GameMode.Adventure);
                        InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
                        if (interactionManagerComponent != null) {
                            String interactions = gameModeType.getInteractionsOnEnter();
                            if (interactions != null) {
                                InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, InteractionType.GameModeSwap, store);
                                RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(interactions);
                                if (rootInteraction != null) {
                                    InteractionChain chain = interactionManagerComponent.initChain(InteractionType.Primary, context, rootInteraction, true);
                                    interactionManagerComponent.queueExecuteChain(chain);
                                }
                            }
                        }
                    });
                }
            }
        });


    }
}
