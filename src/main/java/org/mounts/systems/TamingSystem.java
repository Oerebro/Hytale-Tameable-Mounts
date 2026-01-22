package org.mounts.systems;

import com.hypixel.hytale.builtin.mounts.NPCMountComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.mounts.components.TameableMountComponent;
import org.mounts.plugin.ChocoboPlugin;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class TamingSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = ChocoboPlugin.getInstance().getLogger();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();




    public TamingSystem(){
        LOGGER.atInfo().log("Init TamingSystem");
    }




    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer){
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        //check if the entity is being ridden while riding is not allowed (i.e. if untamed)
        if(commandBuffer.getComponent(ref, NPCMountComponent.getComponentType())!= null && !RidingHandler.isRidingAllowed(ref, commandBuffer)){
            scheduler.schedule(() ->{
                //throw player off:
                //get the player
                PlayerRef rider = commandBuffer.getComponent(ref, NPCMountComponent.getComponentType()).getOwnerPlayerRef();
                assert rider != null;
                Ref<EntityStore> riderRef = rider.getReference();
                if(rider.isValid()){
                    assert riderRef != null;
                    Player playerComponent = commandBuffer.getComponent(riderRef, Player.getComponentType());
                    assert playerComponent != null;
                    //kinda inelegant way to get the mount id, but no clue how else to get it
                    RidingHandler.dismountNpc(commandBuffer,playerComponent.getMountEntityId());
                }

                //TEMP: Increase the tamingProgress
                Objects.requireNonNull(commandBuffer.getComponent(ref, TameableMountComponent.getComponentType())).addTameProgress(30);
                System.out.println("Taming process: "+commandBuffer.getComponent(ref, TameableMountComponent.getComponentType()).getTamingProgress());
            }, 1, TimeUnit.SECONDS);

        }
    }

    //starts the dismount process (MovementState changing etc.)


    @Nullable
    @Override
    //no clue what SystemGroups are yet
    public SystemGroup<EntityStore> getGroup() {
        return null;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(TameableMountComponent.getComponentType());

    }






}
