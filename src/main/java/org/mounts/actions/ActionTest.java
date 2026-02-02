package org.mounts.actions;

import com.google.gson.JsonElement;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.mounts.builders.BuilderActionTest;
import javax.annotation.Nonnull;


public class ActionTest extends ActionBase {
    private String content;

    public ActionTest(@Nonnull BuilderActionTest builderActionTest, @Nonnull BuilderSupport builderSupport) {
        super(builderActionTest);
        this.content = builderActionTest.getContent(builderSupport);
    }


    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, role, sensorInfo, dt, store);
        System.out.println("Action "+this.content);
        return true;
    }


}
