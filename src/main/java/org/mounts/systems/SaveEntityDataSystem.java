    package org.mounts.systems;

    import com.hypixel.hytale.codec.Codec;
    import com.hypixel.hytale.codec.KeyedCodec;
    import com.hypixel.hytale.codec.builder.BuilderCodec;
    import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
    import com.hypixel.hytale.component.*;
    import com.hypixel.hytale.component.query.Query;
    import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
    import com.hypixel.hytale.logger.HytaleLogger;
    import com.hypixel.hytale.server.core.entity.UUIDComponent;
    import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
    import com.hypixel.hytale.server.core.universe.Universe;
    import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
    import com.hypixel.hytale.server.core.util.BsonUtil;
    import org.bson.BsonArray;
    import org.bson.BsonDocument;
    import org.checkerframework.checker.nullness.compatqual.NullableDecl;
    import org.mounts.components.TameableMountComponent;
    import org.mounts.plugin.ChocoboPlugin;

    import javax.annotation.Nonnull;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.Map;
    import java.util.Objects;
    import java.util.UUID;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.atomic.AtomicBoolean;
    import java.util.concurrent.locks.ReentrantLock;
    import java.util.logging.Level;

    /*
        This System handles getting and writing the data of mounts
        Implementation is straight up stolen from the way warps are loaded, lol
     */

    public class SaveEntityDataSystem extends DelayedEntitySystem<EntityStore> {
        @Nonnull
        private final ReentrantLock saveLock = new ReentrantLock();
        @Nonnull
        private final AtomicBoolean postSaveRedo = new AtomicBoolean(false);
        @Nonnull
        private static final AtomicBoolean loaded = new AtomicBoolean();
        public static final Codec<Mount> CODEC = Mount.CODEC;
        public static final ArrayCodec<Mount> ARRAY_CODEC = new ArrayCodec<>(CODEC, Mount[]::new);
        private static final HytaleLogger LOGGER = ChocoboPlugin.getHytaleLogger();
        private static boolean wasTrimmed = false;

        @Nonnull
        private static Map<String, Mount> mounts = new ConcurrentHashMap<>();

        @Nonnull
        private static final Map<String, Mount> loadedMounts = new ConcurrentHashMap<>();

        public SaveEntityDataSystem() {
            super(60);
        }

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            SaveEntityDataSystem.saveThisMount(ref, commandBuffer);
        }

        public static void saveThisMount(Ref<EntityStore> ref,@Nonnull ComponentAccessor<EntityStore> commandBuffer){
            System.out.println("Saved mount.");
            /*TameableMountComponent component = commandBuffer.getComponent(ref, TameableMountComponent.getComponentType());
            if (component == null) {
                return;
            }

            //encode component
            MountMetadata metadata = MountMetadata.fromComponent(component);
            //get uuid as map key
            String key = commandBuffer.getComponent(ref,UUIDComponent.getComponentType()).getUuid().toString();

            mounts.compute(key, (k, existingMount) -> {
                if (existingMount == null) {
                    //if its not already in list, create new instance
                    PersistentRef persistentRef = new PersistentRef(); persistentRef.setEntity(ref, Objects.requireNonNull(commandBuffer.getComponent(ref, UUIDComponent.getComponentType())).getUuid());

                    Mount newMount = new Mount(metadata, persistentRef);
                    newMount.setDeployedToWorld(true);
                    return newMount;
                } else {
                    //update existing mount metadata
                    existingMount.setDeployedToWorld(true);
                    existingMount.metadata = metadata;
                    return existingMount;
                }
            });*/
        }

        public boolean isKnownMount(UUIDComponent component){
            return (mounts.containsKey(component.getUuid().toString()) || loadedMounts.containsKey(component.getUuid().toString()));
        }


        public static void loadMounts() {
            /*BsonDocument document = null;
            Path universePath = Universe.get().getPath();
            Path oldPath = universePath.resolve("mounts.bson");
            Path path = universePath.resolve("mounts.json");
            if (Files.exists(oldPath) && !Files.exists(path)) {
                try {
                    Files.move(oldPath, path);
                } catch (IOException var10) {
                }
            }

            if (Files.exists(path)) {
                document = BsonUtil.readDocument(path).join();
            }

            if (document != null) {
                BsonArray bsonMounts = document.containsKey("Mounts") ? document.getArray("Mounts") : document.getArray("mounts");
                mounts.clear();

                for (Mount mount : Objects.requireNonNull(ARRAY_CODEC.decode(bsonMounts))) {
                    assert mount.getPersistentRef() != null;
                    //add the mount into the map
                    UUID uuid = mount.getPersistentRef().getUuid(); // get the actual UUID
                    loadedMounts.put(uuid.toString().toLowerCase(), mount);
                }

                LOGGER.at(Level.INFO).log("Loaded %d mounts", bsonMounts.size());
            } else {
                LOGGER.at(Level.INFO).log("Loaded 0 mounts (No mounts.json found)");
            }

            loaded.set(true);
             */
        }

        private void saveMounts0() {
            Mount[] array = mounts.values().toArray(Mount[]::new);
            BsonDocument document = new BsonDocument("Mounts", ARRAY_CODEC.encode(array));
            Path path = Universe.get().getPath().resolve("mounts.json");
            BsonUtil.writeDocument(path, document).join();
            LOGGER.at(Level.INFO).log("Saved %d mounts to mounts.json", array.length);
        }

        public void saveMounts() {
            if (this.saveLock.tryLock()) {
                try {
                    this.saveMounts0();
                } catch (Throwable var5) {
                    LOGGER.at(Level.SEVERE).withCause(var5).log("Failed to save entity.:");
                } finally {
                    this.saveLock.unlock();
                }

                if (this.postSaveRedo.getAndSet(false)) {
                    this.saveMounts();
                }
            } else {
                this.postSaveRedo.set(true);
            }
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return Query.and(TameableMountComponent.getComponentType());
        }

        public boolean areMountsLoaded() {
            return this.loaded.get();
        }

        public static TameableMountComponent loadMountData(UUIDComponent uuidComponent){
            System.out.println("Loading mount data.");
            String uuid = uuidComponent.getUuid().toString();
            Mount data = loadedMounts.get(uuid);
            if(data == null){
                data = mounts.get(uuid);
            }
            MountMetadata metaData = data.getMetadata();
            data.markLoaded();
            return new TameableMountComponent(metaData.isTame, metaData.tamingProgress);
        }

        //Encoders for the mount data
        public static class Mount {
            public static final BuilderCodec<SaveEntityDataSystem.Mount> CODEC = BuilderCodec.builder(SaveEntityDataSystem.Mount.class, SaveEntityDataSystem.Mount::new)
                    .append(new KeyedCodec<>("Metadata", MountMetadata.CODEC), (mount, meta) -> mount.metadata = meta, mount -> mount.metadata)
                    .add()
                    .append(
                            new KeyedCodec<>("PersistentRef", PersistentRef.CODEC), (mount, persistentRef) -> mount.persistentRef = persistentRef, mount -> mount.persistentRef
                    )
                    .add()
                    .append(
                            new KeyedCodec<>("DeployedToWorld", Codec.BOOLEAN), (mount, deployedToWorld) -> mount.deployedToWorld = deployedToWorld, mount -> mount.deployedToWorld
                    )
                    .add()
                    .build();

            //metadata contains things like taming progress
            protected MountMetadata metadata;
            @NullableDecl
            protected PersistentRef persistentRef;
            protected boolean deployedToWorld;

            private boolean wasLoaded = false;

            public void markLoaded(){
                this.wasLoaded = true;
            }

            public Mount() {
            }

            public Mount(MountMetadata metadata, @NullableDecl PersistentRef persistentRef) {
                this.metadata = metadata;
                this.persistentRef = persistentRef;
            }

            public MountMetadata getMetadata() {
                return this.metadata;
            }

            @NullableDecl
            public PersistentRef getPersistentRef() {
                return this.persistentRef;
            }

            public void setPersistentRef(@NullableDecl PersistentRef persistentRef) {
                this.persistentRef = persistentRef;
            }

            public boolean getDeployedToWorld() {
                return this.deployedToWorld;
            }

            public void setDeployedToWorld(boolean deployedToWorld) {
                this.deployedToWorld = deployedToWorld;
            }

        }

        public static class MountMetadata {
            public static final String KEY = "CapturedEntity";
            public static final BuilderCodec<MountMetadata> CODEC = BuilderCodec.builder(MountMetadata.class, MountMetadata::new)
                    .appendInherited(
                            new KeyedCodec<>("IsTame", Codec.BOOLEAN), (meta, s) -> meta.isTame = s, meta -> meta.isTame, (meta, parent) -> meta.isTame = parent.isTame
                    )
                    .add()
                    .appendInherited(
                            new KeyedCodec<>("TamingProgress", Codec.INTEGER),
                            (meta, s) -> meta.tamingProgress = s,
                            meta -> meta.tamingProgress,
                            (meta, parent) -> meta.tamingProgress = parent.tamingProgress
                    )
                    .add()
                    .build();

            public static final KeyedCodec<MountMetadata> KEYED_CODEC = new KeyedCodec<>("TameableMount", CODEC);
            private boolean isTame;
            private int tamingProgress;

            public MountMetadata() {
            }

            public static MountMetadata fromComponent(TameableMountComponent component) {
                MountMetadata meta = new MountMetadata();
                meta.isTame = component.isTame();
                meta.tamingProgress = component.getTamingProgress();

                return meta;
            }



        }
    }

