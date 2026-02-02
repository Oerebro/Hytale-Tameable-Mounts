package org.mounts.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.*;
import com.hypixel.hytale.server.core.asset.type.model.config.camera.CameraSettings;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import org.mounts.components.TameableMountComponent;
import org.mounts.plugin.ChocoboPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MountInitSystem extends TickingSystem<EntityStore> {
    //Define all mount role names here
    public enum MountRole {
        CHOCOBO,
        HORSE,
        PLUGIN_MOUNT;

        private static final Set<String> NAMES =
                Arrays.stream(values())
                        .map(Enum::name)
                        .collect(Collectors.toUnmodifiableSet());

        public static boolean contains(String roleName) {
            return roleName != null && NAMES.contains(roleName.toUpperCase().replace("_TAMED",""));
        }
    }

    public enum Dark_Fantasy_Cotton {
        Black(1),
        White(1),
        Lime(1),
        Turquoise(1),
        Pink(1),
        Orange(1),
        Blue(3),
        Brown(3),
        Purple(3),
        Red(7),
        Green(7),
        Yellow(70);

        private final int weight; // higher = more likely

        Dark_Fantasy_Cotton(int weight) {
            this.weight = weight;
        }

        private static final Random RANDOM = new Random();
        private static final int[] CUMULATIVE_WEIGHTS;
        private static final Dark_Fantasy_Cotton[] VALUES = values();
        private static final int TOTAL_WEIGHT;

        static {
            CUMULATIVE_WEIGHTS = new int[VALUES.length];
            int sum = 0;
            for (int i = 0; i < VALUES.length; i++) {
                sum += VALUES[i].weight;
                CUMULATIVE_WEIGHTS[i] = sum;
            }
            TOTAL_WEIGHT = sum;
        }

        public static String getSet(){
            return "Dark_Fantasy_Cotton";
        }

        public static String getRandomColor() {
            int r = RANDOM.nextInt(TOTAL_WEIGHT);
            int index = Arrays.binarySearch(CUMULATIVE_WEIGHTS, r);
            if (index < 0) {
                index = -index - 1;
            }
            return VALUES[index].name();
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
        System.out.println("Init mount");
        if(store.getComponent(ref,TameableMountComponent.getComponentType()) != null){
            return;
        }

        TameableMountComponent mountComponent = new TameableMountComponent();
        //get entity from the store
        Holder<EntityStore> holder = store.removeEntity(ref,RemoveReason.UNLOAD);
        //remove components
        holder.tryRemoveComponent(TameableMountComponent.getComponentType());
        //add back components
        holder.addComponent(TameableMountComponent.getComponentType(),mountComponent);
        ref = store.addEntity(holder,AddReason.LOAD);

        //decide a random color from the set
        setRandomColor(ref,store);

        //set attachment to default when spawning
        setModelAttachment(ref,"Barding","Default",store);

    }



    public static void setModelAttachment(
            @Nonnull Ref<EntityStore> ref, @Nonnull String slot, @Nullable String attachment, @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        if (slot.isEmpty()) {
            throw new IllegalArgumentException("Slot must be specified!");
        } else {
            System.out.println("Set attachment");
            ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
            assert modelComponent != null;
            NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());
            assert npcComponent != null;

            Model model = modelComponent.getModel();
            float scale = model.getScale();
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(model.getModelAssetId());
            Map<String, String> randomAttachments = model.getRandomAttachmentIds() != null ? new HashMap<>(model.getRandomAttachmentIds()) : new HashMap<>();
            if (attachment != null && !attachment.isEmpty()) {
                randomAttachments.put(slot, attachment);
            } else {
                randomAttachments.remove(slot);
            }

            model = Model.createScaledModel(modelAsset, scale, randomAttachments);
            componentAccessor.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
            Role role = npcComponent.getRole();
            if (role != null) {
                role.updateMotionControllers(ref, model, model.getBoundingBox(), componentAccessor);
            }

            TameableMountComponent mountComponent = componentAccessor.getComponent(ref, TameableMountComponent.getComponentType());
            assert mountComponent != null;
            mountComponent.setAttachment(slot,attachment);

        }
    }

    public static void setRandomColor(Ref<EntityStore> ref, Store<EntityStore> store){
        NPCEntity npc = store.getComponent(ref,NPCEntity.getComponentType());
        Role role = npc.getRole();
        //String color = Dark_Fantasy_Cotton.getRandomColor();
        String color = "Blue";

        Model asset = store.getComponent(ref,ModelComponent.getComponentType()).getModel();
        Model newAsset = new Model(
                asset.getModelAssetId(),
                asset.getScale(),
                asset.getRandomAttachmentIds(),
                asset.getAttachments(),
                asset.getBoundingBox(),
                asset.getModel(),
                asset.getTexture(),
                "Dark_Fantasy_Cotton",
                "Blue",
                asset.getEyeHeight(),
                asset.getCrouchOffset(),
                asset.getAnimationSetMap(),
                asset.getCamera(),
                asset.getLight(),
                asset.getParticles(),
                asset.getTrails(),
                asset.getPhysicsValues(),
                asset.getDetailBoxes(),
                asset.getPhobia(),
                asset.getPhobiaModelAssetId()
        );

        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newAsset));

        System.out.println("Color should be: "+color);
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
        @Nonnull
        private final ComponentType<EntityStore, InteractionManager> interactionComponentType = InteractionModule.get().getInteractionManagerComponent();

        public OnAdd(){
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
                System.out.println("Found mount");
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
