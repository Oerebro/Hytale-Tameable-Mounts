package org.mounts.actions;


import com.google.gson.JsonElement;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.mounts.builders.BuilderActionInitMount;
import org.mounts.systems.MountInitSystem;

import javax.annotation.Nonnull;


public class ActionInitMount extends ActionBase {

    public ActionInitMount(@Nonnull BuilderActionInitMount builderActionInitMount, @Nonnull BuilderSupport builderSupport) {
        super(builderActionInitMount);

    }


    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, role, sensorInfo, dt, store);
        MountInitSystem.requestMountInit(ref, store);
        System.out.println("Init a mount: "+store.getComponent(ref, NPCEntity.getComponentType()).getRoleName());
        return true;
    }


}
