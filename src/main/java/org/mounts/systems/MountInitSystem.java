package org.mounts.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.mounts.components.TameableMountComponent;
import org.mounts.plugin.ChocoboPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Collectors;

public class MountInitSystem extends TickingSystem<EntityStore> {
    //Define all mount role names here
    public enum MountRole {
        CHOCOBO,
        PLUGIN_MOUNT;

        private static final Set<String> NAMES =
                Arrays.stream(values())
                        .map(Enum::name)
                        .collect(Collectors.toUnmodifiableSet());

        public static boolean contains(String roleName) {
            return roleName != null && NAMES.contains(roleName.toUpperCase());
        }
    }

     private final ResourceType<EntityStore, MountInitQueue> mountInitQueueResourceType;

    public MountInitSystem(
            @Nonnull ResourceType<EntityStore, MountInitQueue> mountInitQueueResourceType

    ) {
        this.mountInitQueueResourceType = mountInitQueueResourceType;

    }

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store){
            //get the initialization request queue
            MountInitQueue roleChangeQueueResource = store.getResource(this.mountInitQueueResourceType);
            Deque<MountInitRequest> requests = roleChangeQueueResource.requests;
            while (!requests.isEmpty()) {
                MountInitRequest request = requests.poll();
                if (!request.reference().isValid()) continue;
                initializeMount(request.reference(),store);
            }
    }

    private static void initializeMount(Ref<EntityStore> ref, Store<EntityStore> store){
        //TameableMountComponent mountComponent = new TameableMountComponent();
        //get entity from the store
        //Holder<EntityStore> holder = store.removeEntity(ref,RemoveReason.UNLOAD);
        //remove components
        //holder.tryRemoveComponent(TameableMountComponent.getComponentType());
        System.out.println("initializeMount();");
        store.ensureComponent(ref,TameableMountComponent.getComponentType());

        //changeAttachments(holder);

        //add back components
        //holder.addComponent(TameableMountComponent.getComponentType(),mountComponent);

        //return entity to store
        //ref = store.addEntity(holder,AddReason.LOAD);
    }

    private static void changeAttachments(Holder<EntityStore> holder){
        ModelComponent modelComponent = holder.getComponent(ModelComponent.getComponentType());
        if(modelComponent == null) return;
        holder.tryRemoveComponent(ModelComponent.getComponentType());
        Model oldModel = modelComponent.getModel();

        Model newModel = new Model(
                oldModel.getModelAssetId(),
                oldModel.getScale(),
                oldModel.getRandomAttachmentIds(),
                getNewAttachments(oldModel.getGradientSet(),oldModel.getGradientId()),
                oldModel.getBoundingBox(),
                oldModel.getModel(),
                oldModel.getTexture(),
                oldModel.getGradientSet(),
                oldModel.getGradientId(),
                oldModel.getEyeHeight(),
                oldModel.getCrouchOffset(),
                oldModel.getAnimationSetMap(),
                oldModel.getCamera(),
                oldModel.getLight(),
                oldModel.getParticles(),
                oldModel.getTrails(),
                oldModel.getPhysicsValues(),
                oldModel.getDetailBoxes(),
                oldModel.getPhobia(),
                oldModel.getPhobiaModelAssetId()
        );

        holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(newModel));
    }

    private static ModelAttachment[] getNewAttachments(String gradientSet, String gradientId){
        return new ModelAttachment[]  {
            new ModelAttachment(
                    "Saddle",
                    "Common_Barding",
                    gradientSet,
                    gradientId,
                    1.0
            )
        };
    }

    public static void requestMountInit(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store
    ) {
        MountInitQueue mountInitResource = store.getResource(ChocoboPlugin.getInstance().getMountInitQueueResourceType());
        Deque<MountInitRequest> queue = mountInitResource.requests;
        queue.add(new MountInitRequest(ref));
    }

    public ResourceType<EntityStore, MountInitQueue> getMountInitQueueResourceType(){
        return this.mountInitQueueResourceType;
    }

    public static class MountInitQueue implements Resource<EntityStore> {
        @Nonnull
        private final Deque<MountInitRequest> requests = new ArrayDeque<>();

        public MountInitQueue() {
        }

        @Nonnull
        @Override
        public Resource<EntityStore> clone() {
            MountInitQueue mountInitQueue = new MountInitQueue();
            mountInitQueue.requests.addAll(this.requests);
            return mountInitQueue;
        }
    }

    private record MountInitRequest(@Nonnull Ref<EntityStore> reference) {
    }

    //adds TameableMountComponent to NPCMount entities when they are added
    public static class OnAdd extends RefSystem<EntityStore> {

        public OnAdd(){
            System.out.println("Test OnAdd");
        }

        @Override
        public Query<EntityStore> getQuery() {
            return NPCEntity.getComponentType();
        }

        @Override
        public void onEntityAdded(
                @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
        ) {
            if(commandBuffer.getComponent(ref,TameableMountComponent.getComponentType()) != null) return;

            if(isMount(ref,commandBuffer)){
                System.out.println("Mount found.");
                MountInitSystem.requestMountInit(ref,store);
            }
        }

        @Override
        public void onEntityRemove(
                @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
        ) {
        }

        private static boolean isMount(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
            NPCEntity npc = commandBuffer.getComponent(ref, NPCEntity.getComponentType());
            return npc != null && MountRole.contains(npc.getRoleName());
        }
    }
}
