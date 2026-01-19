package org.mounts.systems;

import com.hypixel.hytale.builtin.mounts.NPCMountComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interaction.DismountNPC;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import org.mounts.components.TameableMountComponent;

import javax.annotation.Nonnull;
import java.util.Objects;

/*
    Just various logic I copied from NPCMountSystem/MountPlugin and changed to use commandBuffer to be System safe
    Some additional logic is included
    This class is used by TamingSystem
 */
public class RidingHandler {
    public static void dismountNpc(@Nonnull CommandBuffer<EntityStore> commandBuffer, int mountEntityId) {
        Ref<EntityStore> entityReference = commandBuffer.getExternalData().getRefFromNetworkId(mountEntityId);
        if (entityReference != null && entityReference.isValid()) {
            NPCMountComponent mountComponent = commandBuffer.getComponent(entityReference, NPCMountComponent.getComponentType());

            assert mountComponent != null;
            resetOriginalMountRole(entityReference, commandBuffer, mountComponent);
            PlayerRef ownerPlayerRef = mountComponent.getOwnerPlayerRef();
            if (ownerPlayerRef != null) {
                resetOriginalPlayerMovementSettings(ownerPlayerRef, commandBuffer);
            }
        }
    }

    //I THINK this just safely removes the NPCMountComponent? I am not totally sure
    private static void resetOriginalMountRole(
            @Nonnull Ref<EntityStore> entityReference, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull NPCMountComponent mountComponent
    ) {
        NPCEntity npcComponent = commandBuffer.getComponent(entityReference, NPCEntity.getComponentType());
        assert npcComponent != null;

        //SaveEntityDataSystem.saveThisMount(entityReference,commandBuffer);
        //commandBuffer.removeComponent(entityReference,TameableMountComponent.getComponentType());

        RoleChangeSystem.requestRoleChange(entityReference, npcComponent.getRole(), mountComponent.getOriginalRoleIndex(), false, "Idle", null, commandBuffer);
        commandBuffer.removeComponent(entityReference, NPCMountComponent.getComponentType());
    }

    public static void resetOriginalPlayerMovementSettings(@Nonnull PlayerRef playerRef, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> reference = playerRef.getReference();
        if (reference != null) {
            playerRef.getPacketHandler().write(new DismountNPC());
            MovementManager movementManagerComponent = commandBuffer.getComponent(reference, MovementManager.getComponentType());

            assert movementManagerComponent != null;

            movementManagerComponent.resetDefaultsAndUpdate(reference, commandBuffer);
        }
    }

    //check if mount is tamed
    public static boolean isRidingAllowed(Ref<EntityStore> ref,CommandBuffer<EntityStore> commandBuffer){
        if(commandBuffer.getComponent(ref, TameableMountComponent.getComponentType()) == null){
            return false;
        }
        return (Objects.requireNonNull(commandBuffer.getComponent(ref, TameableMountComponent.getComponentType())).isTame());
    }


}
