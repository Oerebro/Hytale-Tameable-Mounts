package org.mounts.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.mounts.components.TameableMountComponent;
import org.mounts.plugin.ChocoboPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;

public class MountInitSystem extends TickingSystem<EntityStore> {
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
                System.out.println("Mount Init Request processed.");
                TamingSystem.loadOrCreateMount(request.reference(),store);
            }
    }

    public static void requestMountInit(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store
    ) {
        System.out.println("Mount Init requested.");
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
}
