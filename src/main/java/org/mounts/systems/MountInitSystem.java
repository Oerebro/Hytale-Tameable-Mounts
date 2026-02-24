package org.mounts.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.ColorLight;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static class MOUNT_COLORS {

        private static final Random RANDOM = new Random();
        private static final Map<String, Integer> colors = new LinkedHashMap<>();
        private static int[] cumulativeWeights;
        private static List<String> colorList = new ArrayList<>();
        private static int totalWeight;

        static {
            try (InputStream in = MOUNT_COLORS.class.getClassLoader().getResourceAsStream("MountColorRarity.config")) {
                if (in == null) {
                    ChocoboPlugin.getHytaleLogger().atSevere().log("Missing config for mount colors!");
                } else {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty() || line.startsWith("#")) continue;
                            String[] parts = line.split(",");
                            if (parts.length != 2) continue;
                            String name = parts[0].trim();
                            int weight = Integer.parseInt(parts[1].trim());
                            colors.put(name, weight);
                        }
                        rebuildWeights();
                    }
                }
            } catch (IOException e) {
                ChocoboPlugin.getHytaleLogger().atSevere().log("Error reading mount color config: " + e.getMessage());
            }
        }

        private static void rebuildWeights() {
            colorList = new ArrayList<>(colors.keySet());
            cumulativeWeights = new int[colorList.size()];
            int sum = 0;
            for (int i = 0; i < colorList.size(); i++) {
                sum += colors.get(colorList.get(i));
                cumulativeWeights[i] = sum;
            }
            totalWeight = sum;
        }

        public static String getRandomColor() {
            if (colorList.isEmpty()) return null;
            int r = RANDOM.nextInt(totalWeight);
            int index = Arrays.binarySearch(cumulativeWeights, r);
            if (index < 0) index = -index - 1;
            return colorList.get(index);
        }

        public static Set<String> getAllColors() {
            return colors.keySet();
        }

        public static int getWeight(String color) {
            return colors.getOrDefault(color, 0);
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
        //this one doesnt work
        //setRandomColor(ref,store);
        decideColor(ref,store);

        //set attachment to empty when spawning
        setModelAttachment(ref,"Barding","Empty",store);
        setModelAttachment(ref,"Saddle","Empty",store);

    }

    public static void decideColor(Ref<EntityStore> ref,Store<EntityStore> store){
        NPCEntity npc = store.getComponent(ref,NPCEntity.getComponentType());
        String roleName = npc.getRoleName();

        String color = MOUNT_COLORS.getRandomColor();

        ModelAsset asset = ModelAsset.getAssetMap().getAsset(roleName+"_"+color);
        if(asset == null){return;}

        npc.setAppearance(ref,asset,store);

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

    //doesnt work, but I left it in in case I figure out a way to do it
    /*
    public static Model createScaledTintedModel(
            @Nonnull ModelAsset modelAsset, float scale, @Nullable Map<String, String> randomAttachmentIds, @Nullable Box overrideBoundingBox, boolean staticModel, String gradientSet, String gradientId
    ) {
        Objects.requireNonNull(modelAsset, "ModelAsset can't be null");
        if (scale <= 0.0F) {
            throw new IllegalArgumentException("Scale must be greater than 0");
        } else {
            Box boundingBox = overrideBoundingBox != null ? overrideBoundingBox : modelAsset.getBoundingBox();
            Map<String, DetailBox[]> detailBoxes = modelAsset.getDetailBoxes();
            float eyeHeight = modelAsset.getEyeHeight();
            float crouchOffset = modelAsset.getCrouchOffset();
            CameraSettings camera = modelAsset.getCamera();
            PhysicsValues physicsValues = modelAsset.getPhysicsValues();
            ModelParticle[] particles = modelAsset.getParticles();
            ModelTrail[] trails = modelAsset.getTrails();
            if (scale != 1.0F) {
                boundingBox = boundingBox.clone().scale(scale);
                if (detailBoxes != null) {
                    HashMap<String, DetailBox[]> scaledDetailBoxes = new HashMap<>(detailBoxes.size());

                    for (Map.Entry<String, DetailBox[]> entry : detailBoxes.entrySet()) {
                        scaledDetailBoxes.put(entry.getKey(), Arrays.stream(entry.getValue()).map(v -> v.scaled(scale)).toArray(DetailBox[]::new));
                    }

                    detailBoxes = scaledDetailBoxes;
                }

                eyeHeight *= scale;
                crouchOffset *= scale;
                if (camera != null) {
                    camera = camera.clone().scale(scale);
                }

                if (physicsValues != null) {
                    physicsValues = new PhysicsValues(physicsValues);
                    physicsValues.scale(scale);
                }

                if (particles != null) {
                    ModelParticle[] scaledParticules = new ModelParticle[particles.length];

                    for (int i = 0; i < particles.length; i++) {
                        scaledParticules[i] = particles[i].clone().scale(scale);
                    }

                    particles = scaledParticules;
                }

                if (trails != null) {
                    ModelTrail[] scaledTrails = new ModelTrail[trails.length];

                    for (int i = 0; i < trails.length; i++) {
                        ModelTrail trail = trails[i];
                        ModelTrail scaledTrail = new ModelTrail(trail);
                        if (trail.positionOffset != null) {
                            scaledTrail.positionOffset = new Vector3f();
                            scaledTrail.positionOffset.x = trail.positionOffset.x * scale;
                            scaledTrail.positionOffset.y = trail.positionOffset.y * scale;
                            scaledTrail.positionOffset.z = trail.positionOffset.z * scale;
                        }

                        scaledTrails[i] = scaledTrail;
                    }

                    trails = scaledTrails;
                }
            }


            ModelAttachment[] attachments = modelAsset.getAttachments(randomAttachmentIds);
            Map<String, ModelAsset.AnimationSet> animationSetMap = staticModel ? null : modelAsset.getAnimationSetMap();
            return new Model(
                    modelAsset.getId(),
                    scale,
                    randomAttachmentIds,
                    attachments,
                    boundingBox,
                    modelAsset.getModel(),
                    modelAsset.getTexture(),
                    gradientSet,
                    gradientId,
                    eyeHeight,
                    crouchOffset,
                    animationSetMap,
                    camera,
                    new ColorLight((byte)0,(byte)0,(byte)0,(byte)0),
                    particles,
                    trails,
                    physicsValues,
                    detailBoxes,
                    modelAsset.getPhobia(),
                    modelAsset.getPhobiaModelAssetId()
            );
        }
    }


    //doesnt work, but I left it in in case I figure out a way to do it

    public static void setRandomColor(Ref<EntityStore> ref, Store<EntityStore> store){
        NPCEntity npc = store.getComponent(ref,NPCEntity.getComponentType());
        Role role = npc.getRole();
        String color = MOUNT_COLORS.getRandomColor();
        //String set = MOUNT_COLORS.getSet();
        String set = "Fantasy_Cotton_Dark";
        color = "Black";

        Model model = store.getComponent(ref,ModelComponent.getComponentType()).getModel();
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(model.getModelAssetId());
        modelAsset = ModelAsset.getAssetMap().getAsset("Chocobo_Black");
        assert modelAsset != null;
        Model newModel = createScaledTintedModel(
                modelAsset,
                model.getScale(),
                modelAsset.generateRandomAttachmentIds(),
                null,
                false,
                set,
                color
        );

        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        System.out.println("Color should be: "+color);
    }
    */
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
