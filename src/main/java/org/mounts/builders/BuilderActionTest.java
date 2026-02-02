package org.mounts.builders;

import com.google.gson.JsonElement;

import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.mounts.actions.ActionTest;

import javax.annotation.Nonnull;

public class BuilderActionTest extends BuilderActionBase {
    protected final StringHolder content = new StringHolder();

    public BuilderActionTest() {
        System.out.println("BuilderActionTest");
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Enable the player to Test the entity";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return this.getShortDescription();
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }


    public String getContent(@Nonnull BuilderSupport support) {
        return this.content.get(support.getExecutionContext());
    }

    @Nonnull
    public ActionTest build(@Nonnull BuilderSupport builderSupport) {
        return new ActionTest(this, builderSupport);
    }

    @Override
    public Builder<Action> readConfig(@Nonnull JsonElement data) {
        this.requireString(data, "Content", this.content, null, BuilderDescriptorState.Stable, "The MovementConfig to use for this mount", null);
        System.out.println("Test Test");
        return super.readConfig(data);
    }

}
